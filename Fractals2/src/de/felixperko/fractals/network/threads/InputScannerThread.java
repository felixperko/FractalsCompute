package de.felixperko.fractals.network.threads;

import java.util.Scanner;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.task.RemoteTaskProvider;

public class InputScannerThread extends Thread {
	
	Managers managers;
	
	public InputScannerThread(Managers managers) {
		this.managers = managers;
	}
	
	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		
		while (!Thread.interrupted()) {
			while (!scanner.hasNext())
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			parse(scanner.nextLine());
		}
	}
	
	static final String USAGE_CONNECT = "Usage: connect <ip>:<port> [<startThreads>]";

	private void parse(String command) {
		if (command.startsWith("connect")) { //connect ip:port grantThreads
			String[] input = command.trim().split(" ");
			if (input.length != 2 && input.length != 3) {
				System.out.println(USAGE_CONNECT);
				return;
			}
			String[] to = input[1].split(":");
			if (to.length != 2) {
				System.out.println(USAGE_CONNECT);
				return;
			}
			String host = to[0];
			RemoteTaskProvider provider = null;
			try {
				int port = Integer.parseInt(to[1]);
				int startThreads = 0;
				if (input.length == 3) {
					startThreads = Integer.parseInt(input[2]);
					if (managers instanceof ServerManagers) {
						provider = ((ServerManagers)managers).getThreadManager().initRemoteTaskProvider(startThreads);
						((ServerManagers)managers).getThreadManager().startWorkerThreads(startThreads, true);
					}
					//TODO Client?!
				}
				ServerConnection connection = managers.getNetworkManager().connectToServer(host, port);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				provider.addServerConnection(connection);
			} catch (NumberFormatException e) {
				System.out.println(USAGE_CONNECT);
				return;
			}
		}
	}
}
