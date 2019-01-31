package de.felixperko.fractals.system.systems.BasicSystem;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.system.Numbers.DoubleComplexNumber;
import de.felixperko.fractals.system.Numbers.DoubleNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.parameters.CoordinateParamSupplier;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.parameters.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.AbstractCalcSystem;
import de.felixperko.fractals.system.task.ClassTaskFactory;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.TaskFactory;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public class BasicSystem extends AbstractCalcSystem {

	TaskFactory factory_task = new ClassTaskFactory(BasicTask.class);
	
	BasicTaskManager taskManager;
	CalculateFractalsThread calcThread;
	CalculateFractalsThread calcThread2;
	
	@Override
	public boolean onInit(HashMap<String, String> settings) {
		NumberFactory numberFactory = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
		Map<String, ParamSupplier> params = new HashMap<>();
		params.put("width", new StaticParamSupplier("width", (Integer)500));
		params.put("height", new StaticParamSupplier("height", (Integer)500));
		params.put("midpoint", new StaticParamSupplier("midpoint", new DoubleComplexNumber(new DoubleNumber(), new DoubleNumber())));
		params.put("zoom", new StaticParamSupplier("zoom", numberFactory.createNumber(4./10)));
		params.put("iterations", new StaticParamSupplier("iterations", (Integer)1000));
		
//		params.put("start", new StaticParamSupplier("start", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
//		params.put("c", new CoordinateParamSupplier("c", numberFactory));
//		params.put("pow", new StaticParamSupplier("pow", new DoubleComplexNumber(new DoubleNumber(2), new DoubleNumber(Math.PI))));
//		params.put("limit", new StaticParamSupplier("limit", (Double)20.));
		
		params.put("c", new StaticParamSupplier("c", new DoubleComplexNumber(new DoubleNumber(0.0), new DoubleNumber(0.0))));
		params.put("start", new CoordinateParamSupplier("start", numberFactory));
		//params.put("pow", new StaticParamSupplier("pow", new DoubleComplexNumber(new DoubleNumber(2), new DoubleNumber(Math.PI))));
		params.put("limit", new StaticParamSupplier("limit", (Double)(0.2)));
		
		taskManager = new BasicTaskManager(this);
		taskManager.setParameters(params);
		LocalTaskProvider taskProvider = new LocalTaskProvider(taskManager);
		calcThread = new CalculateFractalsThread(this, taskProvider);
		//calcThread2 = new CalculateFractalsThread(this, taskProvider);
		return true;
	}

	@Override
	public boolean onStart() {
		
		taskManager.start();
		calcThread.start();
		//calcThread2.start();
		
		taskManager.startTasks();
		return true;
	}

	@Override
	public boolean onPause() {
		return true;
	}

	@Override
	public boolean onStop() {
		
		taskManager.stopThread();
		calcThread.stopThread();
		//calcThread2.stopThread();
		
		return true;
	}

}
