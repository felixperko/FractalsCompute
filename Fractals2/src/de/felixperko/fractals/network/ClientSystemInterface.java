package de.felixperko.fractals.network;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;

public interface ClientSystemInterface {
	public void chunkUpdated(AbstractArrayChunk chunk);
	public void chunksCleared();
	public void updateParameterConfiguration(ClientConfiguration clientConfiguration, ParameterConfiguration parameterConfiguration);
}
