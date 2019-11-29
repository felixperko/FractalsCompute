package de.felixperko.fractals.system.thread;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.statistics.TimesliceProvider;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.TaskProvider;

public class CalculateFractalsThread extends AbstractFractalsThread{
	
	static int ID_COUNTER = 0;
	
	TaskProvider taskProvider;
	
	int calcThreadId = 0;
	
	FractalsTask currentTask;
	int abortedTaskId = -1;
	
	TimesliceProvider timesliceProvider;
	Map<Integer, Integer> iterations = new HashMap<>();
	int iterationCounter = 0;
	
	public CalculateFractalsThread(Managers managers, TaskProvider taskProvider, TimesliceProvider timesliceProvider){
		super(managers, "CALC_"+ID_COUNTER);
		calcThreadId = ID_COUNTER++;
		this.taskProvider = taskProvider;
		setPriority(1);
	}
	
	public void setTaskProvider(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
		if (taskProvider instanceof LocalTaskProvider)
			taskProvider.addLocalCalculateThread(this);
	}
	
	@Override
	public void run() {
		//do main loop while not stopped
		mainLoop : while (state != LifeCycleState.STOPPED) {
			
			notified = false;
			
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
			currentTask = null;
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
			currentTask.setThread(this);
			currentTask.getStateInfo().setState(TaskState.STARTED);
			try {
				currentTask.run();
				taskProvider.finishedTask(currentTask);
			} catch (InterruptedException e) {
				if (currentTask.getId() != abortedTaskId)
					e.printStackTrace();
			}
			currentTask.setThread(null);
			currentTask = null;
		}
	}
	
	private FractalsTask getTask() {
		if (taskProvider == null)
			return null;
		return taskProvider.getTask();
	}

	public void abortTask() {
		if (currentTask != null) {
			setTaskCancelled();
			//this.interrupt();
		}
	}

	public int getCalcThreadId() {
		return calcThreadId;
	}
	
	public void dispose() {
		if (taskProvider != null)
			taskProvider.removeLocalCalculateThread(this);
	}

	public void taskAvailable() {
		notified = true;
		interrupt();
	}
	
	public void setTaskCancelled() {
		if (currentTask != null) {
			FractalsCalculator calculator = currentTask.getCalculator();
			if (calculator != null)
				calculator.setCancelled();
		}
	}

	public void addIterations(int iterations) {
		Integer timeslice = timesliceProvider.getCurrentTimeslice();
		synchronized (this.iterations) {
			Integer it = this.iterations.getOrDefault(timeslice, 0);
			it++;
			this.iterations.put(timeslice, it);
		}
	}
	
	public int getTimesliceIterations(Integer timeslice, boolean removeOld) {
		synchronized (this.iterations) {
			Integer it = this.iterations.getOrDefault(timeslice, 0);
			if (removeOld) {
				this.iterations.entrySet().removeIf(e -> (e.getKey() < timeslice));
			}
			return it;
		}
	}
}
