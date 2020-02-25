package de.felixperko.fractals.manager.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.manager.common.ThreadManager;
import de.felixperko.fractals.network.threads.InputScannerThread;
import de.felixperko.fractals.network.threads.ServerWriteThread;
import de.felixperko.fractals.system.statistics.IntervalTimesliceProvider;
import de.felixperko.fractals.system.statistics.TimesliceProvider;
import de.felixperko.fractals.system.task.LocalTaskProvider;
import de.felixperko.fractals.system.task.RemoteTaskProvider;
import de.felixperko.fractals.system.task.TaskProvider;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;

public class ServerThreadManager extends ThreadManager{
	
	public static final double TIMESLICE_INTERVAL = 1;
	
	LocalTaskProvider localTaskProvider;
	RemoteTaskProvider remoteTaskProvider = null;
	
	InputScannerThread inputScannerThread;
	
	TimesliceProvider timesliceProvider = new IntervalTimesliceProvider(TIMESLICE_INTERVAL);

	private List<CalculateFractalsThread> calculateThreads = new ArrayList<>();
	
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
			CalculateFractalsThread thread = new CalculateFractalsThread(managers, taskProvider, timesliceProvider);
			addThread(thread);
			calculateThreads.add(thread);
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

	public TimesliceProvider getTimesliceProvider() {
		return timesliceProvider;
	}

	public List<CalculateFractalsThread> getCalculateThreads() {
		return calculateThreads;
	}

}
