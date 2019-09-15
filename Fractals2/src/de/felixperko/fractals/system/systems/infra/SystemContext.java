package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;
import java.util.Map;

import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.LayerConfiguration;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.Layer;

public interface SystemContext extends Serializable{

	boolean setParameters(ParamContainer paramContainer);

	Layer getLayer(int layerId);
	NumberFactory getNumberFactory();

	void taskStateUpdated(TaskStateInfo taskStateInfo, TaskState oldState);

	void setServerConnection(ServerConnection serverConnection);

	FractalsCalculator createCalculator();

	ParamContainer getParamContainer();
	Map<String, ParamSupplier> getParameters();

	Object getParamValue(String parameterKey);
	<T> T getParamValue(String parameterKey, Class<T> valueCls);
	<T> T getParamValue(String parameterKey, Class<T> valueCls, ComplexNumber chunkPos, int pixel, int sample);

	ViewContainer getViewContainer();

	LayerConfiguration getLayerConfiguration();
	Number getPixelzoom();
	int getChunkSize();

	void incrementViewId();

	void setZoom(Number zoom);

	Number getZoom();

	void setMidpoint(ComplexNumber midpoint);

	ComplexNumber getMidpoint();

	int getViewId();

}