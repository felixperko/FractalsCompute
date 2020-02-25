package de.felixperko.fractals.system;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.util.NumberUtil;
import de.felixperko.fractals.util.serialization.jackson.JsonAbstractTypedObject;
import de.felixperko.fractals.util.serialization.jackson.JsonObjectDeserializer;

/**
 * Manages layers for a system.
 * Tries to generate and distribute offsets for even sampling (buggy atm).
 * prepare() has to be called before getOffsets()
 */

public class LayerConfiguration extends JsonAbstractTypedObject implements Serializable{
	
	private static final Logger LOG = LoggerFactory.getLogger(LayerConfiguration.class);
	static long preparedTimeout = (long)(1./NumberUtil.NS_TO_MS);
	
//	public static void main(String[] args) {
//		List<Layer> list = new ArrayList<>();
//		
//		list.add(new BreadthFirstLayer().with_samples(1));
//		list.add(new BreadthFirstLayer().with_samples(9));
//		list.add(new BreadthFirstLayer().with_samples(25));
//		list.add(new BreadthFirstLayer().with_samples(100));
//		
//		LayerConfiguration layerConfiguration = new LayerConfiguration(list, 0.025, 20, 42);
//		layerConfiguration.setDebug(true);
//		layerConfiguration.prepare(new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class));
//	}
	
	private static final long serialVersionUID = -5299508995009860459L;
	public static final String TYPE_NAME = "layerConfig";

	@JsonIgnore
	boolean debug = false;

	@JsonIgnore
	transient ComplexNumber[] offsets;
	@JsonIgnore
	NumberFactory numberFactory;
	@JsonIgnore
	transient boolean prepared = false;
	
	transient int[] firstSampleOfLayer;
	transient int[] offsetIndexForSample;
	
	
//	static Color[] layerColors = new Color[] {new Color(1f, 0, 0), new Color(0f, 1f, 0f), new Color(0.0f,1f,1f), new Color(1f, 0f, 1f), new Color(1f, 1f, 0f)};

	@JsonDeserialize(using = JsonObjectDeserializer.class)
	List<Layer> layers;
	double simStep;
	int simCount;
	long seed;
	
	public LayerConfiguration(List<Layer> layers, double simStep, int simCount, long seed) {
		super(TYPE_NAME);
		this.layers = layers;
		this.simStep = simStep;
		this.simCount = simCount;
		this.seed = seed;
	}
	
	public LayerConfiguration(String typename, List<Layer> layers, double simStep, int simCount, long seed) {
		super(typename);
		this.layers = layers;
		this.simStep = simStep;
		this.simCount = simCount;
		this.seed = seed;
	}
	
	public LayerConfiguration(String typename) {
		super(typename);
	}
	
	public LayerConfiguration() {
		super(TYPE_NAME);
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public synchronized void prepare(NumberFactory numberFactory) {
		prepareLayerSampleLookupTables();
		
		this.numberFactory = numberFactory;
		for (int i = 0; i < layers.size() ; i++) {
			layers.get(i).setId(i);
		}
		
		int totalSamples = 0;
		for (Layer layer : layers) {
			if (layer.getSampleCount() > totalSamples)
				totalSamples = layer.getSampleCount();
		}
		offsets = new ComplexNumber[totalSamples];
		double[][] temp = new double[layers.size()][];
		
		for (int l = 0 ; l < layers.size() ; l++) {
			generatePoints(layers, temp, l);
		
			int lower = l-1;
			if (l > 0)
				distributePoints(layers, simStep, simCount, temp, totalSamples, lower, l);
		}
		
		convertToComplex(layers, numberFactory, temp);
		prepared = true;
	}
	
	private void prepareLayerSampleLookupTables() {
		this.firstSampleOfLayer = new int[layers.size()];
		int counter = 0;
		int totalSamples = 0;
		
		//Fill first sample of layer lookup table
		for (int i = 0; i < firstSampleOfLayer.length; i++) {
			int sampleCount = layers.get(i).getSampleCount();
			this.firstSampleOfLayer[i] = counter;
			counter += sampleCount;
			totalSamples += sampleCount;
		}
	}

	private void convertToComplex(List<Layer> layers, NumberFactory numberFactory, double[][] temp) {
		int counter = 0;
		for (int l = 0 ; l < layers.size() ; l++) {
			double[] points = temp[l];
			for (int i = 0 ; i+1 < points.length ; i+=2){
				offsets[counter++] = numberFactory.createComplexNumber(points[i], points[i+1]);
			}
		}
	}

	private void distributePoints(List<Layer> layers, double simStep, int simCount, double[][] temp,
			int totalSamples, int minLayer, int maxLayer) {
		int activeSamples = 0;
		for (int l = 0 ; l <= maxLayer ; l++)
			activeSamples += layers.get(0).getSampleCount();
		
		//in every step...
		for (int step = 0 ; step < simCount ; step++) {
			double lowestDist = 1;
			double totalDist = 0;
			
			//iterate over every layer...
			for (int l = 0 ; l <= maxLayer ; l++) {
				double[] points = temp[l];
				
				//iterate over every point (pairwise double)
				for (int i = 0 ; i < points.length ; i += 2) {
					double x = points[i];
					double y = points[i+1];
					
					double moveX = 0;
					double moveY = 0;
					
					//influenced by points of previous layers
					for (int l2 = 0 ; l2 <= maxLayer ; l2++) {
						
						//every point in layer
						for (int j = 0 ; j < temp[l2].length ; j += 2) {
							double x2 = temp[l2][j];
							double y2 = temp[l2][j+1];
							
							//vector from influencing to influenced
							double dx = x2-x;
							double dy = y2-y;
							
							//sign == 1 if value of influencing is higher
							double signX = -dx/Math.abs(dx);
							double signY = -dy/Math.abs(dy);
							
							//Same position -> ignore
							if (dx == 0 && dy == 0) 
								continue;
							
							//If wrapped distance is lower, take wrapped distance and invert sign variable
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
							
							//calculate distance
							double dist = dx*dx + dy*dy;
							totalDist += dist;
							if (dist < lowestDist)
								lowestDist = dist;
							if (dist < 0.001)
								dist = 0.001;
							double mult = l2 <= l ? 1 : 0.1; //influence of lower or same layer : influence of higher layers
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
					LOG.debug((lowestDist+"").replace('.', ','));
			}
			
//			if (debug)
//				testDraw(temp, maxLayer, step);
		}
	}

	private void generatePoints(List<Layer> layers, double[][] temp, int layerId) {
		Layer layer = layers.get(layerId);
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
		int i = 0;
		if (layerId == 0) {
			points[0] = 0.5;
			points[1] = 0.5;
			i = 2;
		}
		for ( ; i < len ; i++) {
			points[i] = random.nextDouble();
		}
		temp[layerId] = points;
	}

//	protected void testDraw(double[][] temp, int layer, int step) {
//		//draw test picture
//		int width = 200;
//		int height = 200;
//		double minDist = 1.2/width;
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

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		setLayers(layers, false);
	}

	public void setLayers(List<Layer> layers, boolean prepare) {
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
			Layer layer = layers.get(i);
			Layer layer2 = other.layers.get(i);
			if (!layer.equals(layer2))
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
	
	public ComplexNumber getOffsetForSample(int sample) {
		if (!prepared)
			throw new IllegalStateException("LayerConfiguration has to be prepared first (prepare()).");
		return offsets[sample];
	}
}
