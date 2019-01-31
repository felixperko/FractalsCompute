package de.felixperko.fractals.network;

/**
 * connection to the server
 */
public class ServerConnection implements Connection{
	
	ClientWriteThread writeToServer;
	
	public ClientWriteThread getWriteToServer() {
		return writeToServer;
	}

	public void setWriteToServer(ClientWriteThread writeToServer) {
		this.writeToServer = writeToServer;
	}

	@Override
	public void writeMessage(Message msg) {
		if (writeToServer == null)
			throw new IllegalStateException("Attempted to write a message but the 'write to server' thread wasn't set");
		writeToServer.writeMessage(msg);
	}

	@Override
	public SenderInfo getSenderInfo() {
		return null;
	}
}
