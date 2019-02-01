package de.felixperko.fractals.system.systems.BasicSystem;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.AbstractFractalsCalculator;
import de.felixperko.fractals.system.calculator.infra.AbstractPreparedFractalCalculator;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.parameters.StaticParamSupplier;
import de.felixperko.fractals.system.task.AbstractFractalsTask;

public class BasicTask extends AbstractFractalsTask {
	
	Chunk chunk;
	Map<String, ParamSupplier> parameters;
	
	AbstractFractalsCalculator calculator = new MandelbrotCalculator();
	
	public BasicTask(Chunk chunk, Map<String, ParamSupplier> taskParameters, ComplexNumber chunkPos) {
		this.chunk = chunk;
		this.parameters = new HashMap<>(taskParameters);
		this.parameters.put("chunkpos", new StaticParamSupplier("chunkpos", chunkPos));
		this.parameters.put("chunksize", new StaticParamSupplier("chunksize", (Integer)chunk.getChunkSize()));
	}

	@Override
	public void run() {
		calculator.setParams(parameters);
		calculator.calculate(chunk);
		chunk.incrementSampleCount();
	}

}
