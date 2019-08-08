package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;
import java.util.Map;

import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.Layer;

public interface SystemContext extends Serializable{

	boolean setParameters(Map<String, ParamSupplier> params);

	Layer getLayer(int layerId);
	NumberFactory getNumberFactory();

	void taskStateUpdated(TaskStateInfo taskStateInfo, TaskState oldState);

	void setServerConnection(ServerConnection serverConnection);
}