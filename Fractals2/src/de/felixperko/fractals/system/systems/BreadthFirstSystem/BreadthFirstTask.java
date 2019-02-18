package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTask;
import de.felixperko.fractals.system.task.TaskManager;

public class BreadthFirstTask extends BasicTask {
	
	int layer;

	public BreadthFirstTask(int id, TaskManager taskManager, Chunk chunk, Map<String, ParamSupplier> taskParameters,
			ComplexNumber chunkPos, FractalsCalculator calculator, int layer) {
		super(id, taskManager, chunk, taskParameters, chunkPos, calculator);
		this.layer = layer;
	}

}
