package de.felixperko.fractals.system.systems.BasicSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	
	int id;
	Chunk chunk;
	Map<String, ParamSupplier> parameters;
	
	AbstractFractalsCalculator calculator = new MandelbrotCalculator();
	
	public BasicTask(int id, Chunk chunk, Map<String, ParamSupplier> taskParameters, ComplexNumber chunkPos) {
		this.id = id;
		this.chunk = chunk;
		this.parameters = new HashMap<>();
		for (Entry<String, ParamSupplier> e : taskParameters.entrySet()){
			this.parameters.put(e.getKey(), e.getValue().copy());
		}
//		this.parameters = new HashMap<>(taskParameters);
		this.parameters.put("chunkpos", new StaticParamSupplier("chunkpos", chunkPos));
		this.parameters.put("chunksize", new StaticParamSupplier("chunksize", (Integer)chunk.getChunkSize()));
	}

	@Override
	public void run() {
		calculator.setParams(parameters);
		calculator.calculate(chunk);
		chunk.incrementSampleCount((int)this.parameters.get("samples").get(0, 0));
	}

}
