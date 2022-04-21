package de.felixperko.fractals.manager.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.network.threads.LocalServerThread;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public abstract class ThreadManager extends Manager {

	protected List<FractalsThread> threads = new CopyOnWriteArrayList<>();
	
	protected LocalServerThread localServerThread;

	public ThreadManager(Managers managers) {
		super(managers);
	}

	public void addThread(FractalsThread thread) {
		threads.add(thread);
	}

	public void removeThread(FractalsThread thread) {
		threads.remove(thread);
	}

	
	public CalculateFractalsThread getCalculateFractalsThread(int calcThreadId) {
		for (FractalsThread thread : threads) {
			if (thread instanceof CalculateFractalsThread) {
				CalculateFractalsThread t = (CalculateFractalsThread) thread;
				if (t.getCalcThreadId() == calcThreadId) {
					return t;
				}
			}
		}
		return null;
	}
	
	public List<CalculateFractalsThread> getCalculateThreads(){
		List<CalculateFractalsThread> calculateThreads = new ArrayList<>();
		for (FractalsThread thread : threads){
			if (thread instanceof CalculateFractalsThread)
				calculateThreads.add((CalculateFractalsThread) thread);
		}
		return calculateThreads;
	}

	public void startLocalServer() {
		localServerThread = new LocalServerThread(managers, "SERVER_LOCAL_MAIN", 100);
		threads.add(localServerThread);
		localServerThread.start();
	}
	
	public void stopLocalServer() {
		if (localServerThread != null){
			FractalsMain.stopInstance();
		}
	}

}