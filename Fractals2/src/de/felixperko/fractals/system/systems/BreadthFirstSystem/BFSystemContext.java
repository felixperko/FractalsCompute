package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.shareddata.MappedSharedData;
import de.felixperko.fractals.data.shareddata.MappedSharedDataUpdate;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.network.messages.task.TaskStateChangedMessage;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.SystemStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateUpdate;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public class BFSystemContext implements SystemContext {
	
	private static final long serialVersionUID = -6082120140942989559L;

	transient static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}
	
	transient TaskManager<?> taskManager;
	
	public transient Map<String, ParamSupplier> parameters;
	public transient Class<? extends FractalsCalculator> calculatorClass;
	
	public transient NumberFactory numberFactory;
	public transient ArrayChunkFactory chunkFactory;
	
	public transient ComplexNumber midpoint;
	public transient int chunkSize;
	public transient Number zoom;
	public transient Number chunkZoom;
	
	
	public transient Integer width;
	public transient Integer height;
	
	public transient int buffer = 10;
	public transient double border_generation = 0d;
	public transient double border_dispose = 5d;
	
	public transient BreadthFirstViewData viewData;
	
	public transient int chunksWidth;
	public transient int chunksHeight;
	
	public transient ComplexNumber relativeStartShift;
	
	public transient LayerConfiguration layerConfig;
	
	public transient int jobId;
	
	public transient ComplexNumber leftLowerCorner;
	public transient double leftLowerCornerChunkX;
	public transient double leftLowerCornerChunkY;
	public transient ComplexNumber rightUpperCorner;
	public transient double rightUpperCornerChunkX;
	public transient double rightUpperCornerChunkY;
	
	transient SystemStateInfo systemStateInfo = null;
	transient ServerConnection serverConnection;
	
	public BFSystemContext(TaskManager<?> taskManager) {
		this.taskManager = taskManager;
		if (taskManager != null)
			systemStateInfo = taskManager.getSystem().getSystemStateInfo();
	}
	
	@Override
	public Layer getLayer(int layerId) {
		return layerConfig.getLayer(layerId);
	}
	
	@Override
	public NumberFactory getNumberFactory() {
		return numberFactory;
	}

	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
		
		boolean reset = needsReset(params, this.parameters);
		
		Map<String, ParamSupplier> oldParams = this.parameters;
		this.parameters = params;
		
		calculatorClass = availableCalculators.get(((String)params.get("calculator").get(0, 0)));
		if (calculatorClass == null)
			throw new IllegalStateException("Couldn't find calculator for name: "+params.get("calculator").get(0, 0).toString());
		
		chunkSize = parameters.get("chunkSize").getGeneral(Integer.class);
		midpoint = parameters.get("midpoint").getGeneral(ComplexNumber.class);
		zoom = parameters.get("zoom").getGeneral(Number.class);
		
		numberFactory = parameters.get("numberFactory").getGeneral(NumberFactory.class);
		chunkFactory = parameters.get("chunkFactory").getGeneral(ArrayChunkFactory.class);
		
		width = parameters.get("width").getGeneral(Integer.class);
		height = parameters.get("height").getGeneral(Integer.class);
		
		border_generation = parameters.get("border_generation").getGeneral(Double.class);
		border_dispose = parameters.get("border_dispose").getGeneral(Double.class);
		buffer = parameters.get("task_buffer").getGeneral(Integer.class);

		if (viewData == null || reset) {
			chunksWidth = (int)Math.ceil(width/(double)chunkSize);
			chunksHeight = (int)Math.ceil(height/(double)chunkSize);
			relativeStartShift = numberFactory.createComplexNumber((chunksWidth%2 == 0 ? -0.5 : 0), chunksHeight%2 == 0 ? -0.5 : 0);
		}
		
		Number pixelzoom = numberFactory.createNumber(width >= height ? 1./height : 1./width);
		pixelzoom.mult(zoom);
		params.put("pixelzoom", new StaticParamSupplier("pixelzoom", pixelzoom));
		chunkZoom = pixelzoom.copy();
		chunkZoom.mult(numberFactory.createNumber(chunkSize));
		
		Number rX = numberFactory.createNumber(0.5*(width+chunkSize));
		rX.mult(pixelzoom);
//		Number rY = numberFactory.createNumber(0.5 * ((width > height) ? width/(double)height : 1));
		Number rY = numberFactory.createNumber(0.5*(height+chunkSize));
		rY.mult(pixelzoom);
		ComplexNumber sideDist = numberFactory.createComplexNumber(rX, rY);

		
		ComplexNumber anchor = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
		anchor.multNumber(numberFactory.createNumber(-0.5));
		anchor.add(midpoint);
		
		if (reset && taskManager != null)
			taskManager.reset();
		
		LayerConfiguration oldLayerConfig = null;
		if (oldParams != null)
			oldLayerConfig = oldParams.get("layerConfiguration").getGeneral(LayerConfiguration.class);
		ParamSupplier newLayerConfigSupplier = params.get("layerConfiguration");
		LayerConfiguration newLayerConfig = newLayerConfigSupplier.getGeneral(LayerConfiguration.class);
		if (oldLayerConfig == null || newLayerConfigSupplier.isChanged()) {
			layerConfig = newLayerConfig;
			layerConfig.prepare(numberFactory);
		} else {
			parameters.put("layerConfiguration", oldParams.get("layerConfiguration"));
		}
		
//		ParamSupplier layersParam = parameters.get("layers");
//		List<?> layers2 = layersParam.getGeneral(List.class);
//		if (layers.isEmpty() || layersParam.isChanged()) {
//			layers.clear();
//			for (Object obj : layers2) {
//				if (!(obj instanceof BreadthFirstLayer))
//					throw new IllegalStateException("content in layers isn't compartible with BreadthFirstLayer");
//				layers.add((BreadthFirstLayer)obj);
//				
//				openTasks.add(new PriorityQueue<>(comparator_distance));
//				tempList.add(new ArrayList<>());
//			}
//			if (layers.isEmpty())
//				throw new IllegalStateException("no layers configured");
//		}
		
		if (viewData == null) {
			viewData = new BreadthFirstViewData(anchor);
		}
		ParamSupplier jobIdSupplier = parameters.get("view");
		if (jobIdSupplier == null)
			jobId = 0;
		else
			jobId = jobIdSupplier.getGeneral(Integer.class);
		chunkFactory.setViewData(viewData);

		leftLowerCorner = midpoint.copy();
		leftLowerCorner.sub(sideDist);
		leftLowerCornerChunkX = getChunkX(leftLowerCorner);
		leftLowerCornerChunkY = getChunkY(leftLowerCorner);
		
		rightUpperCorner = sideDist;
		rightUpperCorner.add(midpoint);
		rightUpperCornerChunkX = getChunkX(rightUpperCorner);
		rightUpperCornerChunkY = getChunkY(rightUpperCorner);
		
		return reset;
	}
	
	public static boolean needsReset(Map<String, ParamSupplier> newParams, Map<String, ParamSupplier> oldParams){ //TODO merge with method in SystemClientData
		boolean reset = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : newParams.values()) {
				supplier.updateChanged(oldParams.get(supplier.getName()));
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						reset = true;
				}
			}
		}
		return reset;
	}
	
	public double getChunkX(ComplexNumber pos) {
		Number value = pos.getReal();
		value.sub(viewData.anchor.getReal());
		value.div(chunkZoom);
		return value.toDouble();
	}
	
	public double getChunkY(ComplexNumber pos) {
		Number value = pos.getImag();
		value.sub(viewData.anchor.getImag());
		value.div(chunkZoom);
		return value.toDouble();
	}

	public ComplexNumber getChunkPos(long chunkX, long chunkY) {
		ComplexNumber chunkPos = numberFactory.createComplexNumber(chunkX, chunkY);
		chunkPos.add(relativeStartShift);
		chunkPos.multNumber(chunkZoom);
		chunkPos.add(viewData.anchor);
		return chunkPos;
	}

	public double getScreenDistance(long chunkX, long chunkY) {
		if (chunkX+1 >= leftLowerCornerChunkX && chunkY+1 >= leftLowerCornerChunkY) {
			if (chunkX <= rightUpperCornerChunkX && chunkY <= rightUpperCornerChunkY) {
				return 0;
			}
			double dx = chunkX > rightUpperCornerChunkX ? chunkX-rightUpperCornerChunkX : 0;
			double dy = chunkY > rightUpperCornerChunkY ? chunkY-rightUpperCornerChunkY : 0;
			return Math.sqrt(dx*dx + dy*dy);
		}
		chunkX++;
		chunkY++;
		double dx = chunkX < leftLowerCornerChunkX ? leftLowerCornerChunkX-chunkX : 0;
		double dy = chunkY < leftLowerCornerChunkY ? leftLowerCornerChunkY-chunkY : 0;
		return Math.sqrt(dx*dx + dy*dy);
	}

	public double getScreenDistance(Chunk chunk) {
		return getScreenDistance(chunk.getChunkX(), chunk.getChunkY());
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
	public void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject();
		oos.writeObject(parameters);
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
		Map<String, ParamSupplier> parameters = (Map<String, ParamSupplier>) ois.readObject();
		setParameters(parameters);
	}
}
