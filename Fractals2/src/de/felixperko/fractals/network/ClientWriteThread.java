package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.util.CategoryLogger;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", Color.MAGENTA);
	
	public ClientWriteThread(Socket socket) throws UnknownHostException, IOException {
		super(socket);
		log = superLogger.createSubLogger("out");
		setListenLogger(new CategoryLogger("com/client/in", Color.MAGENTA));
		setConnection(FractalsMain.serverConnection);
	}
	
	@Override
	protected void prepareMessage(Message msg) {
		if (msg.getSender() == null) {
			SenderInfo info = FractalsMain.clientStateHolder.stateClientInfo.getValue();
			if (info == null)
				throw new IllegalStateException("message can't be adressed");
			msg.setSender(info);
		}
		super.prepareMessage(msg);
	}
}
