package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.TaskProvider;

public class CalculateFractalsThread extends AbstractFractalsThread{
	
	TaskProvider taskProvider;
	
	boolean stopped = false;
	
	public CalculateFractalsThread(CalcSystem system, TaskProvider taskProvider) {
		super(system);
		this.taskProvider = taskProvider;
	}
	
	@Override
	public void run() {
		mainLoop : while (!stopped) {
			FractalsTask currentTask = null;
			while (currentTask == null) {
				currentTask = getTask();
				if (currentTask == null)
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (stopped)
							break mainLoop;
					}
			}
			currentTask.run();
			taskProvider.finishedTask(currentTask);
			currentTask = null;
		}
	}
	
	private FractalsTask getTask() {
		if (taskProvider == null)
			return null;
		return taskProvider.getTask();
	}
}
