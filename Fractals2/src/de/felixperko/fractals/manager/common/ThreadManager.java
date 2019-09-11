package de.felixperko.fractals.manager.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.network.threads.LocalServerThread;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public abstract class ThreadManager extends Manager {

	protected List<FractalsThread> threads = new CopyOnWriteArrayList<>();

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

	public void startLocalServer() {
		LocalServerThread lst = new LocalServerThread(managers, "SERVER_LOCAL_MAIN", 100);
		lst.start();
	}

}