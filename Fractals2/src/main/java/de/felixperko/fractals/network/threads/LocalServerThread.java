package de.felixperko.fractals.network.threads;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.system.thread.AbstractFractalsThread;

public class LocalServerThread extends AbstractFractalsThread {

	long sleepInMs;
	
	public LocalServerThread(Managers managers, String name, long sleepInMs) {
		super(managers, name);
		this.sleepInMs = sleepInMs;
	}
	
	@Override
	public synchronized void start() {
		super.start();
		try {
			Thread.sleep(sleepInMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		FractalsMain.main(null);
	}

}
