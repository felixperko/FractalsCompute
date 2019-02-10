package de.felixperko.fractals.manager;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.system.thread.FractalsThread;

public class ThreadManager extends Manager{
	
	List<FractalsThread> threads = new CopyOnWriteArrayList<>();

	public ThreadManager(Managers managers) {
		super(managers);
	}

	public ServerWriteThread startServerWriteThread(Socket accept) {
		ServerWriteThread thread = new ServerWriteThread(managers, accept);
		threads.add(thread);
		thread.start();
		return thread;
	}
	
	public void addThread(FractalsThread thread) {
		threads.add(thread);
	}

	public void removeThread(FractalsThread thread) {
		threads.remove(thread);
	}

}
