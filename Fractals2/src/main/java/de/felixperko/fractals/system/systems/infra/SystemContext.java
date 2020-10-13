package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.LayerConfiguration;
import de.felixperko.fractals.system.calculator.infra.DeviceType;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.Layer;

public interface SystemContext<C extends ViewContainer> extends Serializable{

	boolean setParameters(ParamContainer paramContainer);

	Layer getLayer(int layerId);
	NumberFactory getNumberFactory();

	void taskStateUpdated(TaskStateInfo taskStateInfo, TaskState oldState);

	void setServerConnection(ServerConnection serverConnection);

	FractalsCalculator createCalculator(DeviceType deviceType);
	AbstractArrayChunk createChunk(int chunkX, int chunkY);

	ParamContainer getParamContainer();
	Map<String, ParamSupplier> getParameters();

	Object getParamValue(String parameterKey);
	<T> T getParamValue(String parameterKey, Class<T> valueCls);
	<T> T getParamValue(String parameterKey, Class<T> valueCls, ComplexNumber chunkPos, int pixel, int sample);

	C getViewContainer();

	LayerConfiguration getLayerConfiguration();
	Number getPixelzoom();
	int getChunkSize();

	void incrementViewId();

	void setMidpoint(ComplexNumber midpoint);

	ComplexNumber getMidpoint();

	int getViewId();

	void setViewId(Integer viewId);

	SystemStateInfo getSystemStateInfo();

	ParamConfiguration getParamConfiguration();
	
	void setParamConfiguration(ParamConfiguration paramConfiguration);

}