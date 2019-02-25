package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.TaskProvider;

public class CalculateFractalsThread extends AbstractFractalsThread{
	
	static int ID_COUNTER = 0;
	
	TaskProvider taskProvider;
	
	int calcThreadId = 0;
	
	public CalculateFractalsThread(Managers managers, TaskProvider taskProvider){
		super(managers, "CALC_"+ID_COUNTER);
		calcThreadId = ID_COUNTER++;
		this.taskProvider = taskProvider;
	}
	
	public void setTaskProvider(TaskProvider taskProvider) {
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
			
			//idle if no task provider is set
			if (taskProvider == null) {
				setLifeCycleState(LifeCycleState.IDLE);
				while (taskProvider == null) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (state == LifeCycleState.STOPPED)
							break mainLoop;
					}
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
			currentTask.getStateInfo().setState(TaskState.STARTED);
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
