package de.felixperko.fractals.system;

import java.util.List;

import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.task.Layer;

/**
 * LayerConfiguration that uses the Padovan sequence derived from the golden ratio to evenly distribute samples.
 * http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
 */
public class PadovanLayerConfiguration extends LayerConfiguration {
	
	private static final long serialVersionUID = 7949407215148091003L;
	public static final String TYPE_NAME = "padovanLayerConfig";
	
//	public static void main(String[] args) {
//		List<Layer> list = new ArrayList<>();
//		
//		list.add(new BreadthFirstLayer().with_samples(1));
//		list.add(new BreadthFirstLayer().with_samples(4));
//		list.add(new BreadthFirstLayer().with_samples(9));
//		list.add(new BreadthFirstLayer().with_samples(36));
//		list.add(new BreadthFirstLayer().with_samples(1000));
//		
//		LayerConfiguration layerConfiguration = new PadovanLayerConfiguration(list);
//		layerConfiguration.setDebug(true);
//		layerConfiguration.prepare(new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class));
//	}

	public PadovanLayerConfiguration(List<Layer> layers) {
		super(TYPE_NAME, layers, 0, 0, 0);
	}
	
	public PadovanLayerConfiguration() {
		super(TYPE_NAME);
	}
	
	@Override
	public synchronized void prepare(NumberFactory numberFactory) {
		for (int i = 0; i < layers.size() ; i++) {
			layers.get(i).setId(i);
		}
		int totalSampleCount = layers.get(layers.size()-1).getSampleCount();
		offsets = new ComplexNumber[totalSampleCount];
		double g = 1.32471795724474602596;
		double a1 = 1.0/g;
		double a2 = 1.0/(g*g);
		for (int i = 0 ; i < totalSampleCount ; i++) {
			
//			g = 1.32471795724474602596
//			a1 = 1.0/g
//			a2 = 1.0/(g*g)
//			x[n] = (0.5+a1*n) %1
//			y[n] = (0.5+a2*n) %1
			
			double real = (0.5+a1*(i+1))%1;
			double imag = (0.5+a2*(i+1))%1;
			offsets[i] = numberFactory.createComplexNumber(real, imag);
			
			//testdraw
			if (debug && (i+1) % 50 == 0) {
				double[][] temp = new double[layers.size()][];
				int sampleCounter = 0;
				for (int l = 0 ; l < temp.length ; l++) {
					Layer layer = layers.get(l);
					int layerSampleCount = layer.getSampleCount();
					int count = layerSampleCount-sampleCounter;
					if (count > 0) {
						temp[l] = new double[count*2];
						for (int s = 0 ; s < count ; s++) {
							if (sampleCounter > i)
								break;
							temp[l][s*2] = offsets[sampleCounter].getReal().toDouble();
							temp[l][s*2+1] = offsets[sampleCounter].getImag().toDouble();
							sampleCounter++;
						}
					}
				}
				//testDraw(temp, layers.size(), i);
			}
		}
		prepared = true;
	}
}
