package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.BitSet;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;
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
//		if (previousLayer == null || !(previousLayer instanceof BreadthFirstUpsampleLayer))
//			return;
//		BreadthFirstUpsampleLayer prev = (BreadthFirstUpsampleLayer) previousLayer;
//		if (!prev.cullingEnabled())
//			return;
//		BitSet activePixels = prev.getEnabledPixels();
//		for (int i = activePixels.nextSetBit(0) ; i != -1 ; i = activePixels.nextSetBit(i+1)) { //loop active pixels
//			if (prev.getNeighbourCulling(chunk, i)) {
//				for (int i2 : prev.getManagedIndices(i)) {
//					prev.setCullingFlag(i2);
//				}
//			}
//		}
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
}
