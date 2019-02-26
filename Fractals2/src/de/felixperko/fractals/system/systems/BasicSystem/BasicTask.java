package de.felixperko.fractals.system.systems.BasicSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.AbstractFractalsCalculator;
import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.parameters.StaticParamSupplier;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractFractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public class BasicTask extends AbstractFractalsTask {
	
	public Chunk chunk;
	Map<String, ParamSupplier> parameters;
	
	FractalsCalculator calculator;
	
	
	public BasicTask(int id, TaskManager taskManager, Chunk chunk, Map<String, ParamSupplier> taskParameters,
			ComplexNumber chunkPos, FractalsCalculator calculator, Layer layer) {
		super(id, taskManager, layer);
		this.chunk = chunk;
		this.calculator = calculator;
		this.parameters = new HashMap<>();
		for (Entry<String, ParamSupplier> e : taskParameters.entrySet()){
			this.parameters.put(e.getKey(), e.getValue().copy());
		}
//		this.parameters = new HashMap<>(taskParameters);
		this.parameters.put("chunkpos", new StaticParamSupplier("chunkpos", chunkPos.copy()));
		this.chunk.chunkPos = chunkPos.copy();
		this.parameters.put("chunksize", new StaticParamSupplier("chunksize", (Integer)chunk.getChunkSize()));
	}

	@Override
	public void run() {
		calculator.setParams(parameters);
		calculator.calculate(chunk);
	}
	
	public Chunk getChunk() {
		return chunk;
	}


}
