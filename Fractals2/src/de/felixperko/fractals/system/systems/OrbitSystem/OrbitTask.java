package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.task.AbstractFractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.FractalsThread;

public class OrbitTask extends AbstractFractalsTask<OrbitTask> {

	private static final long serialVersionUID = -7778760685136366361L;
	
	ComplexNumber current;
	List<ComplexNumber> values = new ArrayList<>();
	int index;

	public OrbitTask(SystemContext context, Integer id, TaskManager<OrbitTask> taskManager, int jobId, Layer layer) {
		super(context, id, taskManager, jobId, layer);
		
	}

	@Override
	public void setThread(FractalsThread thread) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FractalsCalculator getCalculator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPriority() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
