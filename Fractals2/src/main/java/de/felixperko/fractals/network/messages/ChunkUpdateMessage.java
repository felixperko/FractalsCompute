package de.felixperko.fractals.network.messages;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.network.infra.SystemServerMessage;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.system.systems.stateinfo.ServerStateInfo;

public class ChunkUpdateMessage extends SystemServerMessage {
	
	private static final long serialVersionUID = -2349690041977280160L;
	
	CompressedChunk chunk;
	List<DataContainer> sharedDataStateUpdates;
	//ServerStateInfo serverStateInfo;
	
	public ChunkUpdateMessage(ClientConnection connection, UUID systemId, Serializable chunk, ServerStateInfo serverStateInfo) {
		super(systemId);
		if (!chunk.getClass().equals(CompressedChunk.class))
			throw new IllegalArgumentException("Only CompressedChunk is supported for the Chunk class at the moment.");
		this.chunk = (CompressedChunk)chunk;
		this.sharedDataStateUpdates = serverStateInfo.getSharedDataUpdates(connection);
		//this.serverStateInfo = serverStateInfo;
 	}

	@Override
	protected void process() {
		//getClientMessageInterface().serverStateUpdated(serverStateInfo);
		getClientSystemInterface().chunkUpdated(chunk);
		if (sharedDataStateUpdates != null && !sharedDataStateUpdates.isEmpty())
			getClientMessageInterface().sharedDataUpdated(sharedDataStateUpdates);
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
	
	@Override
	public double getPriority() {
		return chunk.getPriority()+2;
	}
}
