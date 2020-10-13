package de.felixperko.fractals.network.interfaces;

import java.util.UUID;

import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.parameters.ParamConfiguration;

public interface ClientSystemInterface {
	public UUID getSystemId();
	public void chunkUpdated(CompressedChunk chunk);
	public void chunksCleared();
	public ParamConfiguration getParamConfiguration();
	public void updateParameterConfiguration(ParamContainer paramContainer, ParamConfiguration parameterConfiguration);
}
