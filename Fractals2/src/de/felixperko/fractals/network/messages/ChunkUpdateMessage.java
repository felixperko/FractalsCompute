package de.felixperko.fractals.network.messages;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.network.Message;

public class ChunkUpdateMessage extends Message {
	
	private static final long serialVersionUID = -2349690041977280160L;
	
	Chunk chunk;
	
	public ChunkUpdateMessage(Chunk chunk) {
		this.chunk = chunk;
	}

	@Override
	protected void process() {
		//TODO receive chunk
		FractalsMain.threadManager.getCalcPixelThread(FractalsMain.mainWindow.getMainRenderer()).addChunk(chunk);
	}
	
	@Override
	public void logIncoming() {
		log.log("received "+getClass().getSimpleName()+" starting at "+chunk.getStartPosition().toString());
	}

	public Chunk getChunk() {
		return chunk;
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}
}
