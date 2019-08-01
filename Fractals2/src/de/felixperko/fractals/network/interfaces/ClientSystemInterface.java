package de.felixperko.fractals.network.interfaces;

import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.network.SystemClientData;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;

public interface ClientSystemInterface {
	public void chunkUpdated(CompressedChunk chunk);
	public void chunksCleared();
	public ParameterConfiguration getParamConfiguration();
	public void updateParameterConfiguration(SystemClientData systemClientData, ParameterConfiguration parameterConfiguration);
}
