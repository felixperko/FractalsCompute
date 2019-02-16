package de.felixperko.fractals.manager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

}