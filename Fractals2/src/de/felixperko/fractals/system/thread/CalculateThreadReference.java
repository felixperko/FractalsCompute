package de.felixperko.fractals.system.thread;

import de.felixperko.fractals.manager.common.Managers;

public class CalculateThreadReference {
	
	int threadId;
	int clientId; //local if -1
	
	transient Managers managers;
	
	/**
	 * create a reference to a local thread
	 * @param threadId
	 */
	public CalculateThreadReference(Managers managers, int threadId) {
		this.clientId = -1;
		this.threadId = threadId;
	}
	
	public CalculateThreadReference(Managers managers, int clientId, int threadId) {
		this.clientId = clientId;
		this.threadId = threadId;
	}
	
	public void abortTask(int threadId, int taskId) {
		if (clientId != -1) {
			throw new IllegalStateException("remote tasks can't be aborted yet"); //TODO abort remote tasks implementation
		} else {
			CalculateFractalsThread thread = managers.getThreadManager().getCalculateFractalsThread(threadId);
			thread.abortTask(taskId);
		}
	}
}
