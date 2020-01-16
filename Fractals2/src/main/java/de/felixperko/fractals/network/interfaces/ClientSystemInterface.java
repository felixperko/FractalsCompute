package de.felixperko.fractals.network.interfaces;

import java.util.UUID;

import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParameterConfiguration;

public interface ClientSystemInterface {
	public UUID getSystemId();
	public void chunkUpdated(CompressedChunk chunk);
	public void chunksCleared();
	public ParameterConfiguration getParamConfiguration();
	public void updateParameterConfiguration(ParamContainer paramContainer, ParameterConfiguration parameterConfiguration);
}
