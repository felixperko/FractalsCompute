package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;

public class LayerConfiguration {
	
	ComplexNumber[][] offsets;
	
	public static void main(String[] args) {
		List<BreadthFirstLayer> list = new ArrayList<>();
		
		list.add(new BreadthFirstLayer(0).with_samples(1));
		list.add(new BreadthFirstLayer(1).with_samples(2));
		list.add(new BreadthFirstLayer(2).with_samples(2));
		
		new LayerConfiguration(list, null, 0.3, 5);
	}
	
	public LayerConfiguration(List<BreadthFirstLayer> layers, NumberFactory numberFactory, double simStep, int simCount) {
		offsets = new ComplexNumber[layers.size()][];
		double[][] temp = new double[layers.size()][];
		
		for (int l = 0 ; l < temp.length ; l++) {
			BreadthFirstLayer layer = layers.get(l);
			int sampleCount = layer.getSampleCount();
			int len = sampleCount*2;
			
			//generate random points
			double[] points = new double[len];
			points[0] = 0.5;
			points[1] = 0.5;
			for (int i = 2 ; i < len ; i++) {
				points[i] = Math.random();
			}
			temp[l] = points;
			
			//move points
			for (int step = 0 ; step < simCount ; step++) {
				for (int i = 0 ; i < len ; i += 2) {
					double x = points[i];
					double y = points[i+1];
					
					double moveX = 0;
					double moveY = 0;
					for (int j = i+2 ; j < len ; j += 2) {
						double x2 = points[j];
						double y2 = points[j+1];
						double signX = -1;
						double signY = -1;
						
						double dx = Math.abs(x2-x);
						double dy = Math.abs(y2-y);
						if (dx > 0.5) {
							dx = 0.5 - dx;
							signX = 1;
						}
						if (dy > 0.5) {
							dy = 0.5 - dy;
							signY = 1;
						}
						
						double dist = Math.sqrt(dx*dx + dy*dy);
						moveX += dist*signX*simStep;
						moveY += dist*signY*simStep;
					}
					
					points[i] = clamp(x + moveX, 0, 1);
					points[i+1] = clamp(y + moveY, 0, 1);
					continue;
				}
			}
		}
	}
	
	private double clamp(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}
}
