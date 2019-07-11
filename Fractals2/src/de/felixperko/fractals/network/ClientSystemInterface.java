package de.felixperko.fractals.network;

import java.util.List;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.shareddata.DataContainer;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;

public interface ClientSystemInterface {
	public void chunkUpdated(CompressedChunk chunk);
	public void chunksCleared();
	public ParameterConfiguration getParamConfiguration();
	public void updateParameterConfiguration(ClientConfiguration clientConfiguration, ParameterConfiguration parameterConfiguration);
	public void sharedDataUpdated(List<DataContainer> sharedDataStateUpdates);
}
