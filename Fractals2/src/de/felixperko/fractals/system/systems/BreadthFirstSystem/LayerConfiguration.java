package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.ColorContainer;
import de.felixperko.fractals.util.NumberUtil;

//debug image imports
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import javax.imageio.ImageIO;

/**
 * Manages layers for a system.
 * Tries to generate and distribute offsets for even sampling (might be a bit buggy atm).
 * prepare() has to be called before getOffsets()
 */

public class LayerConfiguration implements Serializable{
	
	static CategoryLogger log = new CategoryLogger("systems/bf/LayerConfiguraton", new ColorContainer(1f, 1f, 0f));
	static long preparedTimeout = (long)(1./NumberUtil.NS_TO_MS);
	
	public static void main(String[] args) {
		List<BreadthFirstLayer> list = new ArrayList<>();
		
		list.add(new BreadthFirstLayer().with_samples(1));
		list.add(new BreadthFirstLayer().with_samples(2));
		list.add(new BreadthFirstLayer().with_samples(7));
//		list.add(new BreadthFirstLayer().with_samples(40));
		
		LayerConfiguration layerConfiguration = new LayerConfiguration(list, 0.05, 20, 42);
		layerConfiguration.setDebug(true);
		layerConfiguration.prepare(new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class));
	}
	
	private static final long serialVersionUID = -5299508995009860459L;

	boolean debug = false;
	
	transient ComplexNumber[][] offsets;
	NumberFactory numberFactory;
	transient boolean prepared = false;
	
	
//	Color[] layerColors = new Color[] {new Color(1f, 0, 0), new Color(0f, 1f, 0f), new Color(0.0f,1f,1f), new Color(1f, 0f, 1f)};
	
	List<BreadthFirstLayer> layers;
	double simStep;
	int simCount;
	long seed;

	public LayerConfiguration(List<BreadthFirstLayer> layers, double simStep, int simCount, long seed) {
		this.layers = layers;
		this.simStep = simStep;
		this.simCount = simCount;
		this.seed = seed;
	}
	
	public synchronized ComplexNumber[] getOffsets(int layerId) {
		if (!prepared){
			long waitingSince = System.nanoTime();
			long time = System.nanoTime();
			while (!prepared){
				time = System.nanoTime();
				if (time-waitingSince > preparedTimeout)
					break;
			}
			log.log("waited for prepartion for "+NumberUtil.getTimeInS(time-waitingSince, 6));
		}
		if (!prepared)
			throw new IllegalStateException("LayerConfiguration has to be prepared first (prepare()).");
		return offsets[layerId];
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public synchronized void prepare(NumberFactory numberFactory) {
		this.numberFactory = numberFactory;
		for (int i = 0; i < layers.size() ; i++) {
			layers.get(i).setId(i);
		}
		offsets = new ComplexNumber[layers.size()][];
		double[][] temp = new double[layers.size()][];
		
		int totalSamples = 0;
		for (BreadthFirstLayer layer : layers) {
			if (layer.getSampleCount() > totalSamples)
				totalSamples = layer.getSampleCount();
		}
		
		for (int l = 0 ; l < layers.size() ; l++) {
			generatePoints(layers, temp, l);
		
			int lower = l-1;
			if (l > 0)
				distributePoints(layers, simStep, simCount, temp, totalSamples, lower, l);
		}
		
		convertToComplex(layers, numberFactory, temp);
		prepared = true;
	}

	private void convertToComplex(List<BreadthFirstLayer> layers, NumberFactory numberFactory, double[][] temp) {
		for (int l = 0 ; l < layers.size() ; l++) {
			double[] points = temp[l];
			ComplexNumber[] compPoints = new ComplexNumber[points.length/2];
			for (int i = 0 ; i < points.length ; i+=2)
				compPoints[i/2] = numberFactory.createComplexNumber(points[i], points[i+1]);
			offsets[l] = compPoints;
		}
	}

	private void distributePoints(List<BreadthFirstLayer> layers, double simStep, int simCount, double[][] temp,
			int totalSamples, int minLayer, int maxLayer) {
		int activeSamples = 0;
		for (int l = 0 ; l <= maxLayer ; l++)
			activeSamples += layers.get(0).getSampleCount();
		for (int step = 0 ; step < simCount ; step++) {
			double lowestDist = 1;
			double totalDist = 0;
			for (int l = 0 ; l <= maxLayer ; l++) {
				double[] points = temp[l];
				for (int i = 0 ; i < points.length ; i += 2) {
					double x = points[i];
					double y = points[i+1];
					
					double moveX = 0;
					double moveY = 0;
					for (int l2 = 0 ; l2 <= maxLayer ; l2++) {
						for (int j = 0 ; j < temp[l2].length ; j += 2) {
							double x2 = temp[l2][j];
							double y2 = temp[l2][j+1];
							
							double dx = x2-x;
							double dy = y2-y;
							double signX = -dx/Math.abs(dx);
							double signY = -dy/Math.abs(dy);
							if (dx == 0 && dy == 0)
								continue;
							if (dx > 0.5) {
								dx = +1-dx;
								signX = -signX;
							}
							if (dx < -0.5) {
								dx = -1+dx;
								signX = -signX;
							}
							if (dy > 0.5) {
								dy = +1-dy;
								signY = -signY;
							}
							if (dy < -0.5) {
								dy = -1+dy;
								signY = -signY;
							}
//							dx *= signX;
//							dy *= signY;
							
							double dist = dx*dx + dy*dy;
							totalDist += dist;
							if (dist < lowestDist)
								lowestDist = dist;
							if (dist < 0.01)
								dist = 0.01;
							double mult = l2 <= l ? 1 : 0.1;
							mult *= 1-(activeSamples/((double)totalSamples));
							mult *= 1-(step/(double)simCount);
							moveX += (signX*simStep)*mult/dist;
							moveY += (signY*simStep)*mult/dist;
						}
					}
					moveX = clamp(moveX, -0.05, 0.05);
					moveY = clamp(moveY, -0.05, 0.05);
					double newX = (x + moveX)%1;
					double newY = (y + moveY)%1;
//					double newY = (y + moveY + (Math.random()*0.05-0.025))%1;
					if (newX < 0)
						newX = 1+newX;
					if (newY < 0)
						newY = 1+newY;
					points[i] = newX;
					points[i+1] = newY;
					continue;
				}
				if (debug)
					System.out.println((lowestDist+"").replace('.', ','));
			}
			
//			if (debug)
//				testDraw(temp, maxLayer, step);
		}
	}

	private void generatePoints(List<BreadthFirstLayer> layers, double[][] temp, int layerId) {
		BreadthFirstLayer layer = layers.get(layerId);
		int sampleCount = layer.getSampleCount();
		int accu = 0;
		for (int l2 = 0 ; l2 < layerId ; l2++) {
			int diff = layers.get(l2).getSampleCount() - accu;
			accu += diff;
			sampleCount -= diff;
		}
		int len = sampleCount*2;
		
		Random random = new Random(seed);
		
		//generate random points
		double[] points = new double[len];
		if (layerId == 0) {
			points[0] = 0.5;
			points[1] = 0.5;
			for (int i = 2 ; i < len ; i++) {
				points[i] = random.nextDouble();
			}
		} else {
			for (int i = 0 ; i < len ; i++) {
				points[i] = random.nextDouble();
			}
		}
		temp[layerId] = points;
	}

//	private void testDraw(double[][] temp, int layer, int step) {
//		//draw test picture
//		int width = 200;
//		int height = 200;
//		double minDist = 2./width;
//		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		for (int x = 0 ; x < width ; x++) {
//			for (int y = 0 ; y < height ; y++) {
//				Color color = null;
//				double relX = x/(double)width;
//				double relY = y/(double)height;
//				pointsearch:
//				for (int l2 = 0 ; l2 < temp.length ; l2++) {
//					double[] points2 = temp[l2];
//					if (points2 == null)
//						continue;
//					for (int i = 0 ; i < points2.length ; i+=2) {
//						double x2 = points2[i];
//						double y2 = points2[i+1];
//						double dx = x2-relX;
//						double dy = y2-relY;
//						if (Math.sqrt(dx*dx+dy*dy) < minDist) {
//							color = layerColors[l2];
//							break pointsearch;
//						}
//					}
//				}
//				if (color == null)
//					color = new Color(0, 0, 0, 1);
//				image.setRGB(x, y, color.getRGB());
//			}
//		}
//		try {
//			ImageIO.write(image, "png", new File(layer+"-"+step+".png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private double clamp(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}

	public List<BreadthFirstLayer> getLayers() {
		return layers;
	}

	public void setLayers(List<BreadthFirstLayer> layers, boolean prepare) {
		this.prepared = false;
		this.layers = layers;
		this.offsets = null;
		if (prepare)
			prepare(numberFactory);
	}

	public boolean isPrepared() {
		return prepared;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null || !(arg0 instanceof LayerConfiguration))
			return false;
		LayerConfiguration other = (LayerConfiguration) arg0;
		if (layers.size() != other.layers.size())
			return false;
		for (int i = 0 ; i < layers.size() ; i++) {
			if (!layers.get(0).equals(other.layers.get(0)))
				return false;
		}
		return true;
	}
	
	public NumberFactory getNumberFactory() {
		return numberFactory;
	}

	
	public Layer getLayer(int layerId) {
		return layers.get(layerId);
	}
}
