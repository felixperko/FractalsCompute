package de.felixperko.fractals.network.messages;

import java.util.UUID;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.network.infra.Message;
import de.felixperko.fractals.network.infra.ServerMessage;
import de.felixperko.fractals.network.infra.SystemServerMessage;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;

public class ChunkUpdateMessage extends SystemServerMessage {
	
	private static final long serialVersionUID = -2349690041977280160L;
	
	CompressedChunk chunk;
	ServerStateInfo serverStateInfo;
	
	public ChunkUpdateMessage(UUID systemId, CompressedChunk chunk, ServerStateInfo serverStateInfo) {
		super(systemId);
		this.chunk = chunk;
		this.serverStateInfo = serverStateInfo;
 	}

	@Override
	protected void process() {
		getClientMessageInterface().serverStateUpdated(serverStateInfo);
		getClientSystemInterface().chunkUpdated(chunk.decompress());
//		FractalsMain.threadManager.getCalcPixelThread(FractalsMain.mainWindow.getMainRenderer()).addChunk(chunk);
	}
	
//	@Override
//	public void logIncoming() {
//		log.log("received "+getClass().getSimpleName()+" starting at "+chunk.getStartPosition().toString());
//	}

	public CompressedChunk getChunk() {
		return chunk;
	}

	public void setChunk(CompressedChunk chunk) {
		this.chunk = chunk;
	}
}
