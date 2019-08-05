package de.felixperko.fractals.network;

import java.util.Scanner;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ServerConnection;

public class InputScannerThread extends Thread {
	
	Managers managers;
	
	public InputScannerThread(Managers managers) {
		this.managers = managers;
	}
	
	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		
		while (!Thread.interrupted()) {
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
			try {
				int port = Integer.parseInt(to[1]);
				int startThreads = 0;
				if (input.length == 3) {
					startThreads = Integer.parseInt(input[2]);
					if (managers instanceof ServerManagers) {
						((ServerManagers)managers).getThreadManager().initRemoteTaskProvider(startThreads);
						((ServerManagers)managers).getThreadManager().startWorkerThreads(startThreads, true);
					}
					//TODO Client?!
				}
				managers.getNetworkManager().connectToServer(host, port);
			} catch (NumberFormatException e) {
				System.out.println(USAGE_CONNECT);
				return;
			}
		}
	}
}
