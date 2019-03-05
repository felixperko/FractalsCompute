package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;

public class LayerConfiguration {
	
	static boolean debug = false;
	
	ComplexNumber[][] offsets;
	
	Color[] layerColors = new Color[] {new Color(1f, 0, 0), new Color(0f, 1f, 0f), new Color(0f,0f,1f)};
	
	public static void main(String[] args) {
		debug = true;
		List<BreadthFirstLayer> list = new ArrayList<>();
		
		list.add(new BreadthFirstLayer(0).with_samples(1));
		list.add(new BreadthFirstLayer(1).with_samples(4));
		list.add(new BreadthFirstLayer(2).with_samples(64));
		
		new LayerConfiguration(list, null, 0.01, 100);
	}
	
	public LayerConfiguration(List<BreadthFirstLayer> layers, NumberFactory numberFactory, double simStep, int simCount) {
		offsets = new ComplexNumber[layers.size()][];
		double[][] temp = new double[layers.size()][];
		
		int totalSamples = 0;
		for (BreadthFirstLayer layer : layers)
			totalSamples += layer.getSampleCount();
		
		generatePoints(layers, temp);
		
		distributePoints(layers, simStep, simCount, temp, totalSamples);
		
		convertToComplex(layers, numberFactory, temp);
	}

	private void convertToComplex(List<BreadthFirstLayer> layers, NumberFactory numberFactory, double[][] temp) {
		for (int l = 0 ; l < layers.size() ; l++) {
			double[] points = temp[l];
			ComplexNumber[] compPoints = new ComplexNumber[points.length/2];
			for (int i = 0 ; i < points.length ; i+=2)
				compPoints[i] = numberFactory.createComplexNumber(points[i], points[i+1]);
		}
	}

	private void distributePoints(List<BreadthFirstLayer> layers, double simStep, int simCount, double[][] temp,
			int totalSamples) {
		for (int step = 0 ; step < simCount ; step++) {
			double lowestDist = 1;
			double totalDist = 0;
			for (int l = 0 ; l < layers.size() ; l++) {
				double[] points = temp[l];
				for (int i = 0 ; i < points.length ; i += 2) {
					double x = points[i];
					double y = points[i+1];
					
					double moveX = 0;
					double moveY = 0;
					for (int l2 = 0 ; l2 < layers.size() ; l2++) {
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
							double mult = l2 == l ? 2 : 1;
							mult *= 1-(temp[l].length*0.5/totalSamples);
							mult *= 1-(step/(double)simCount);
							moveX += (signX*simStep)*mult/dist;
							moveY += (signY*simStep)*mult/dist;
						}
					}
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
			
			if (debug)
				testDraw(temp, step);
		}
	}

	private void generatePoints(List<BreadthFirstLayer> layers, double[][] temp) {
		for (int l = 0 ; l < temp.length ; l++) {
			BreadthFirstLayer layer = layers.get(l);
			int sampleCount = layer.getSampleCount();
			int len = sampleCount*2;
			
			//generate random points
			double[] points = new double[len];
			if (l == 0) {
				points[0] = 0.5;
				points[1] = 0.5;
				for (int i = 2 ; i < len ; i++) {
					points[i] = Math.random();
				}
			} else {
				for (int i = 0 ; i < len ; i++) {
					points[i] = Math.random();
				}
			}
			temp[l] = points;
			
		}
	}

	private void testDraw(double[][] temp, int step) {
		//draw test picture
		int width = 200;
		int height = 200;
		double minDist = 2./width;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0 ; x < width ; x++) {
			for (int y = 0 ; y < height ; y++) {
				Color color = null;
				double relX = x/(double)width;
				double relY = y/(double)height;
				pointsearch:
				for (int l2 = 0 ; l2 < temp.length ; l2++) {
					double[] points2 = temp[l2];
					if (points2 == null)
						continue;
					for (int i = 0 ; i < points2.length ; i+=2) {
						double x2 = points2[i];
						double y2 = points2[i+1];
						double dx = x2-relX;
						double dy = y2-relY;
						if (Math.sqrt(dx*dx+dy*dy) < minDist) {
							color = layerColors[l2];
							break pointsearch;
						}
					}
				}
				if (color == null)
					color = new Color(0, 0, 0, 1);
				image.setRGB(x, y, color.getRGB());
			}
		}
		try {
			ImageIO.write(image, "png", new File(step+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double clamp(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}
}
