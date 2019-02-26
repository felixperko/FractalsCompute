package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Map;

import de.felixperko.fractals.data.Chunk;
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

	public BreadthFirstTask(int id, TaskManager taskManager, Chunk chunk, Map<String, ParamSupplier> taskParameters,
			ComplexNumber chunkPos, FractalsCalculator calculator, Layer layer) {
		super(id, taskManager, chunk, taskParameters, chunkPos, calculator, layer);
		chunk.setCurrentTask(this);
		getStateInfo().setState(TaskState.OPEN);
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
