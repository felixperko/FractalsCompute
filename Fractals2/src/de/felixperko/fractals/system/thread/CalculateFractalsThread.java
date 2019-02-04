package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.ThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.TaskProvider;

public class CalculateFractalsThread extends AbstractSystemThread{
	
	TaskProvider taskProvider;
	
	public CalculateFractalsThread(ThreadManager threadManager, CalcSystem system, TaskProvider taskProvider) {
		super(threadManager, system);
		this.taskProvider = taskProvider;
	}
	
	@Override
	public void run() {
		//do main loop while not stopped
		mainLoop : while (state != LifeCycleState.STOPPED) {
			
			//paused
			while (state == LifeCycleState.PAUSED) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					if (state == LifeCycleState.STOPPED)
						break mainLoop;
				}
			}
			
			//get task, idle if none available
			FractalsTask currentTask = null;
			while (currentTask == null) {
				currentTask = getTask();
				if (currentTask == null) {
					setLifeCycleState(LifeCycleState.IDLE);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (state == LifeCycleState.STOPPED)
							break mainLoop;
					}
				}
			}
			
			//got task, execute
			setLifeCycleState(LifeCycleState.RUNNING);
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
