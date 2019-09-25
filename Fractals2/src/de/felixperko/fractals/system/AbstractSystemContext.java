package de.felixperko.fractals.system;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.shareddata.MappedSharedDataUpdate;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BFViewContainer;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;
import de.felixperko.fractals.system.systems.infra.ViewData;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public abstract class AbstractSystemContext<D extends ViewData, C extends ViewContainer<D>> implements SystemContext<C> {

	private static final long serialVersionUID = -5080191521407651861L;
	
	protected transient TaskManager<?> taskManager;

	public abstract boolean setParameters(ParamContainer paramContainer);

	public transient ParamContainer paramContainer;
	public transient Class<? extends FractalsCalculator> calculatorClass;
	public transient NumberFactory numberFactory;
	public transient ArrayChunkFactory chunkFactory;
	public transient ComplexNumber midpoint;
	public transient int chunkSize;
	protected transient C viewContainer;
	public transient LayerConfiguration layerConfig;
	protected transient SystemStateInfo systemStateInfo = null;
	protected transient ServerConnection serverConnection;
	protected transient Integer viewId;

	public AbstractSystemContext(TaskManager<?> taskManager, C viewContainer) {
		this.taskManager = taskManager;
		this.viewContainer = viewContainer;
	}

	@Override
	public void taskStateUpdated(TaskStateInfo taskStateInfo, TaskState oldState) {
		if (systemStateInfo != null) { //TODO local
			TaskStateUpdate updateMessage = taskStateInfo.getUpdateMessage();
			if (updateMessage == null || updateMessage.isSent())
				taskStateInfo.setUpdateMessage(systemStateInfo.taskStateChanged(taskStateInfo.getTaskId(), oldState, taskStateInfo));
			else {
				synchronized (updateMessage) {
					updateMessage.refresh(taskStateInfo.getState(), taskStateInfo.getLayerId(), taskStateInfo.getProgress());
					systemStateInfo.taskStateUpdated(updateMessage);
				}
			}
		} else {
			
			MappedSharedDataUpdate<TaskStateUpdate> update = new MappedSharedDataUpdate<>();
			update.setValue(taskStateInfo.getSystemId()+""+taskStateInfo.getTaskId(), new TaskStateUpdate(taskStateInfo));
			serverConnection.stateUpdates.update(update);
			
//			TaskStateChangedMessage msg = new TaskStateChangedMessage(taskStateInfo);
//			serverConnection.writeMessage(msg);
		}
	}

	@Override
	public Layer getLayer(int layerId) {
		return layerConfig.getLayer(layerId);
	}

	@Override
	public NumberFactory getNumberFactory() {
		return numberFactory;
	}

	public D getActiveViewData() {
		return viewContainer.getActiveViewData();
	}

	public void setActiveViewData(D viewData) {
		viewContainer.setActiveViewData(viewData);
	}

	@Override
	public FractalsCalculator createCalculator() {
		try {
			return calculatorClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Failed to create calculator for class: "+calculatorClass.getName());
		}
	}

	@Override
	public void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	@Override
	public C getViewContainer() {
		return viewContainer;
	}

	@Override
	public ParamContainer getParamContainer() {
		return paramContainer;
	}

	@Override
	public synchronized Map<String, ParamSupplier> getParameters() {
		return paramContainer.getClientParameters();
	}

	@Override
	public synchronized <T> T getParamValue(String parameterKey, Class<T> valueCls) {
		return paramContainer.getClientParameter(parameterKey).get(this, valueCls, null, 0, 0);
	}

	@Override
	public synchronized Object getParamValue(String parameterKey) {
		return paramContainer.getClientParameter(parameterKey).get(this, null, 0, 0);
	}

	@Override
	public synchronized <T> T getParamValue(String parameterKey, Class<T> valueCls, ComplexNumber chunkPos, int pixel, int sample) {
		return paramContainer.getClientParameter(parameterKey).get(this, valueCls, chunkPos, pixel, sample);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeObject(paramContainer);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		ParamContainer paramContainer = (ParamContainer) ois.readObject();
		setParameters(paramContainer);
	}

	@Override
	public LayerConfiguration getLayerConfiguration() {
		return layerConfig;
	}

	@Override
	public int getChunkSize() {
		return chunkSize;
	}

	@Override
	public ComplexNumber getMidpoint() {
		return midpoint;
	}

	@Override
	public void setMidpoint(ComplexNumber midpoint) {
		this.midpoint = midpoint;
		paramContainer.addClientParameter(new StaticParamSupplier("midpoint", midpoint));
	}

	@Override
	public void incrementViewId() {
		viewId = getParamValue("view", Integer.class) + 1;
		paramContainer.addClientParameter(new StaticParamSupplier("view", viewId));
	}

	@Override
	public void setViewId(Integer viewId) {
		this.viewId = viewId;
		paramContainer.addClientParameter(new StaticParamSupplier("view", viewId));
	}

	@Override
	public int getViewId() {
		return viewId;
	}

}