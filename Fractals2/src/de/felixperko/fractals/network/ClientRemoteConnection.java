package de.felixperko.fractals.network;

public class ClientRemoteConnection implements ClientConnection{
	
	SenderInfo info;
	ServerWriteThread writeThread;
	
	public ClientRemoteConnection(SenderInfo info, ServerWriteThread writeThread) {
		this.info = info;
		this.writeThread = writeThread;
	}
	
	@Override
	public void writeMessage(Message msg) {
		writeThread.writeMessage(msg);
	}

	public SenderInfo getSenderInfo() {
		return info;
	}
}
