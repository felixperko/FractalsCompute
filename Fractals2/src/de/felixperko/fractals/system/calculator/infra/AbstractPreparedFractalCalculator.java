package de.felixperko.fractals.system.calculator.infra;

import java.util.LinkedList;
import java.util.Queue;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.task.Layer;

public abstract class AbstractPreparedFractalCalculator extends AbstractFractalsCalculator{
	
	private static final long serialVersionUID = 8917363332981274755L;
	
	final static double LOG_2 = Math.log(2);
	
	//ParamSupplier[] params;
	
	public AbstractPreparedFractalCalculator() {
		super(AbstractPreparedFractalCalculator.class);
	}

	ParamSupplier p_start;
	ParamSupplier p_c;
	ParamSupplier p_pow;
	ParamSupplier p_iterations;
	ParamSupplier p_limit;
	ParamSupplier p_samples;
	
	int iterations;
	double limit;
	int upsample;
	AbstractArrayChunk chunk;
	
	Queue<Integer> redo = new LinkedList<Integer>();
	
	CalculatePhase phase = null;
	
	@Override
	public void calculate(AbstractArrayChunk chunk) {
		this.chunk = chunk;
		limit = (Double) p_limit.get(0,0);
		iterations = (Integer) p_iterations.get(0,0);
		Layer layer = chunk.getCurrentTask().getStateInfo().getLayer();
		upsample = (layer instanceof BreadthFirstUpsampleLayer) ? ((BreadthFirstUpsampleLayer)layer).getUpsample()/2 : 0;
		int samples = layer.getSampleCount();
//		if (chunk.getChunkX() == 5 && chunk.getChunkY() == 10)
//			System.out.println("test chunk");
		
		int pixelCount = chunk.getArrayLength();
		int chunkSize = chunk.getChunkDimensions();
		
		phase = CalculatePhase.PHASE_MAINLOOP;
		
		loop : 
		for (int pixel = 0 ; pixel < pixelCount ; pixel++) {
			if (!layer.isActive(pixel))
				continue;
			if (chunk.getValue(pixel) == AbstractArrayChunk.FLAG_CULL) {
//				int x = pixel / chunkSize;
//				int y = pixel % chunkSize;
//				for (int x2 = x-1 ; x2 <= x+1 ; x++) {
//					for (int y2 = y-1 ; y2 <= y+1 ; y++) {
//						if (x < 0) {
//							
//						} else if (x >= chunkSize) {
//							
//						}
//					}
//				}
				continue;
			}
			if (isCancelled())
				break loop;
			for (int sample = chunk.getSampleCount(pixel) ; sample < samples ; sample++){
				calculateSample(pixel, sample);
			}
			chunk.getCurrentTask().getStateInfo().setProgress((pixel+1.-redo.size())/pixelCount);
		}
		
		redoLoop:
		while (!redo.isEmpty()) {
			try {
				phase = CalculatePhase.PHASE_REDO;
				int pixel = redo.poll();
				for (int sample = chunk.getSampleCount(pixel) ; sample < samples ; sample++) {
					if (isCancelled()){
						redo.clear();
						break redoLoop;
					}
					calculateSample(pixel, sample);
				}
				chunk.getCurrentTask().getStateInfo().setProgress((pixelCount-redo.size())/pixelCount);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void calculateSample(int pixel, int sample) {
		double res = -1;
		ComplexNumber current = ((ComplexNumber) p_start.get(pixel,sample)).copy();
		ComplexNumber c = ((ComplexNumber) p_c.get(pixel,sample)).copy();
		ComplexNumber pow = ((ComplexNumber) p_pow.get(pixel,sample)).copy();
		double logPow = Math.log(pow.absDouble());
//		long t1 = System.nanoTime();
		for (int k = 0 ; k < iterations ; k++) {
			executeKernel(current, pow, c);
			double abs = current.absSqDouble();
			if (abs > limit*limit) {
//							Math.log( Math.log(real*real+imag*imag)*0.5 / Math.log(2) ) / Math.log(pow)  )
				res = k - Math.log(Math.log(abs)*0.5/LOG_2)/logPow; //abs...
				sample_success(pixel);
				break;
			}
		}
//		long t2 = System.nanoTime();
//		if (res != -1)
//			res = (t2-t1);
		chunk.addSample(pixel, res, upsample);
	}
	
	private void sample_success(int pixel) {
		if (chunk.getValue(pixel, true) <= 0)
			sample_first_success(pixel);
			
	}
	
	private void sample_first_success(int pixel) {
		final int chunkDimensions = chunk.getChunkDimensions();
		final int chunkArrayLength = chunk.getArrayLength();
		int thisX = pixel / chunkDimensions;
		int thisY = pixel % chunkDimensions;
		//disable culling of surrounding pixels
		for (int dx = -1 ; dx <= 1 ; dx++) {
			for (int dy = -1 ; dy <= 1 ; dy++) {
				
				if (dx == 0 && dy == 0)
					continue;
				
				int x = thisX + dx;
				int y = thisY + dy;
				
				//determine if in neighbour chunk
				boolean local = true;
				if (x < 0){
					local = false;
					if (y > 0 && y < chunkDimensions)
						chunk.getBorderData(BorderAlignment.LEFT).set(true, y, y);
				} else if (x >= chunkDimensions){
					local = false;
					if (y > 0 && y < chunkDimensions)
						chunk.getBorderData(BorderAlignment.RIGHT).set(true, y, y);
				}
				
				if (y < 0){
					local = false;
					if (x > 0 && x < chunkDimensions)
						chunk.getBorderData(BorderAlignment.UP).set(true, x, x);
				} else if (y >= chunkDimensions){
					local = false;
					if (x > 0 && x < chunkDimensions)
						chunk.getBorderData(BorderAlignment.DOWN).set(true, x, x);
				}
				
				//local -> add to redo queue
				if (local){
					int pixel2 = x*chunkDimensions + y;
					if (chunk.getValue(pixel2, true) == AbstractArrayChunk.FLAG_CULL) {
						chunk.setCullFlags(pixel2, 1, false);
						if (phase == CalculatePhase.PHASE_REDO || pixel > pixel2) {
							redo.add(pixel2);
						}
					}
				}
			}
		}
	}

	public abstract void executeKernel(ComplexNumber current, ComplexNumber exp, ComplexNumber c);
	
	enum CalculatePhase {
		PHASE_MAINLOOP, PHASE_REDO;
	}
}
