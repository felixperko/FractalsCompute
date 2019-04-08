package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTask;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public class BreadthFirstTask extends BasicTask {
	
	private static final long serialVersionUID = 428442040367400862L;

	Double distance;
	Double priority;
	
	Layer previousLayer;

	public BreadthFirstTask(int id, TaskManager taskManager, AbstractArrayChunk chunk, Map<String, ParamSupplier> taskParameters,
			ComplexNumber chunkPos, FractalsCalculator calculator, Layer layer, int jobId) {
		super(id, taskManager, chunk, taskParameters, chunkPos, calculator, layer, jobId);
		chunk.setCurrentTask(this);
		getStateInfo().setState(TaskState.OPEN);
	}
	
	@Override
	public void run() {
		preprocess();
		previousLayer = getStateInfo().getLayer();
		super.run();
	}

	private void preprocess() {
		if (previousLayer == null || !(previousLayer instanceof BreadthFirstLayer))
			return;
		BreadthFirstLayer prev = (BreadthFirstLayer) previousLayer;
		
		if (prev.cullingEnabled()){
			preprocess_culling(prev);
		}
	}

	private void preprocess_culling(BreadthFirstLayer prev) {
		if (prev instanceof BreadthFirstUpsampleLayer) {
			BreadthFirstUpsampleLayer prevUpsampled = (BreadthFirstUpsampleLayer) prev;
			BitSet activePixels = prevUpsampled.getEnabledPixels();
			int upsample = prevUpsampled.upsample;
			int chunkSize = chunk.getChunkDimensions();
			
			for (int i = 0 ; i < chunk.getArrayLength() ; i++) {
			}
			
			activePixelLoop:
			for (int i = activePixels.nextSetBit(0) ; i != -1 ; i = activePixels.nextSetBit(i+1)) { //loop active pixels
				int x = i / chunkSize;
				int y = i % chunkSize;
				boolean cull = true;
				
				evalCullingLoop:
				for (int dx = -1 ; dx <= 1 ; dx++) {
					for (int dy = -1 ; dy <= 1 ; dy++) {
						int neighbourUpsampleIndex = i + dx*chunkSize + dy;
						if (neighbourUpsampleIndex < 0){
							if (dx == -1) {
								if (chunk.getNeighbourBorderData(BorderAlignment.LEFT).isSet(y)) {
									cull = false;
									break evalCullingLoop;
								}
							} else if (dy == -1){
								if (chunk.getNeighbourBorderData(BorderAlignment.UP).isSet(x)) {
									cull = false;
									break evalCullingLoop;
								}
							} else
								throw new IllegalStateException("unexpected branch");
						} else if (neighbourUpsampleIndex >= chunk.getArrayLength()) {
							if (dx == 1) {
								if (chunk.getNeighbourBorderData(BorderAlignment.RIGHT).isSet(y)) {
									cull = false;
									break evalCullingLoop;
								}
							} else if (dy == 1){
								if (chunk.getNeighbourBorderData(BorderAlignment.DOWN).isSet(x)) {
									cull = false;
									break evalCullingLoop;
								}
							} else
								throw new IllegalStateException("unexpected branch");
						} else {
							if (chunk.getValue(neighbourUpsampleIndex, true) > 0) {
								cull = false;
								break evalCullingLoop;
							}
						}
					}
				}
				
				chunk.setCullFlags(i, upsample, cull);
			}
		}
	}

	public void updateDistance(double chunkX, double chunkY) {
		distance = getChunk().distance(chunkX, chunkY);
	}
	
	public void updatePriorityAndDistance(double chunkX, double chunkY, Layer layer) {
		updateDistance(chunkX, chunkY);
		priority = distance * layer.getPriorityMultiplier() + layer.getPriorityShift();
	}

	public Double getDistance() {
		return distance;
	}

	public Double getPriority() {
		return priority;
	}
	
	public Layer getPreviousLayer() {
		return previousLayer;
	}
}
