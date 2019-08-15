package de.felixperko.fractals.manager.server;

import java.net.Socket;

import de.felixperko.fractals.manager.common.ThreadManager;
import de.felixperko.fractals.network.InputScannerThread;
import de.felixperko.fractals.network.ServerWriteThread;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.RemoteTaskProvider;
import de.felixperko.fractals.system.task.TaskProvider;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public class ServerThreadManager extends ThreadManager{
	
	LocalTaskProvider localTaskProvider;
	RemoteTaskProvider remoteTaskProvider = null;
	
	InputScannerThread inputScannerThread;
	
	public ServerThreadManager(ServerManagers managers) {
		super(managers);
		localTaskProvider = new LocalTaskProvider(managers);
	}
	
	public RemoteTaskProvider initRemoteTaskProvider(int buffer) {
		remoteTaskProvider = new RemoteTaskProvider(buffer);
		return remoteTaskProvider;
	}
	
	public void startWorkerThreads(int count, boolean useRemoteTaskProvider) {
		TaskProvider taskProvider = useRemoteTaskProvider ? remoteTaskProvider : localTaskProvider;
		if (taskProvider == null)
			throw new IllegalAccessError("task provider is null (initRemoteTaskProvider() not called?)");
		for (int i = 0 ; i < count ; i++){
			CalculateFractalsThread thread = new CalculateFractalsThread(managers, taskProvider);
			addThread(thread);
			thread.setTaskProvider(taskProvider);
			thread.start();
		}
	}

	public ServerWriteThread startServerWriteThread(Socket accept) {
		ServerWriteThread thread = new ServerWriteThread((ServerManagers)managers, accept);
		threads.add(thread);
		thread.start();
		return thread;
	}
	
	public LocalTaskProvider getTaskProvider() {
		return localTaskProvider;
	}
	
	public void startInputScannerThread() {
		inputScannerThread = new InputScannerThread(managers);
		inputScannerThread.start();
	}

	
	public RemoteTaskProvider getRemoteTaskProvider() {
		return remoteTaskProvider;
	}

}
