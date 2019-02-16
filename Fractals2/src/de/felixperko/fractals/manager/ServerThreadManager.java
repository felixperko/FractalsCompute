package de.felixperko.fractals.manager;

import java.net.Socket;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.TaskProvider;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public class ServerThreadManager extends ThreadManager{
	
	TaskProvider taskProvider = new LocalTaskProvider();
	
	public ServerThreadManager(ServerManagers managers) {
		super(managers);
	}
	
	public void startWorkerThreads(int count) {
		for (int i = 0 ; i < count ; i++){
			CalculateFractalsThread thread = new CalculateFractalsThread((ServerManagers)managers, taskProvider);
			addThread(thread);
			thread.start();
		}
	}

	public ServerWriteThread startServerWriteThread(Socket accept) {
		ServerWriteThread thread = new ServerWriteThread((ServerManagers)managers, accept);
		threads.add(thread);
		thread.start();
		return thread;
	}
	
	public TaskProvider getTaskProvider() {
		return taskProvider;
	}

}
