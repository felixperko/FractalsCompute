package de.felixperko.fractals.network.messages;

import java.util.UUID;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.ServerMessage;
import de.felixperko.fractals.network.infra.SystemServerMessage;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;

public class ChunkUpdateMessage extends SystemServerMessage {
	
	private static final long serialVersionUID = -2349690041977280160L;
	
	Chunk chunk;
	ServerStateInfo serverStateInfo;
	
	public ChunkUpdateMessage(UUID systemId, Chunk chunk, ServerStateInfo serverStateInfo) {
		super(systemId);
		this.chunk = chunk;
		this.serverStateInfo = serverStateInfo;
	}

	@Override
	protected void process() {
		//TODO receive chunk
		getClientMessageInterface().serverStateUpdated(serverStateInfo);
		getClientSystemInterface().chunkUpdated(chunk);
//		FractalsMain.threadManager.getCalcPixelThread(FractalsMain.mainWindow.getMainRenderer()).addChunk(chunk);
	}
	
//	@Override
//	public void logIncoming() {
//		log.log("received "+getClass().getSimpleName()+" starting at "+chunk.getStartPosition().toString());
//	}

	public Chunk getChunk() {
		return chunk;
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}
}
