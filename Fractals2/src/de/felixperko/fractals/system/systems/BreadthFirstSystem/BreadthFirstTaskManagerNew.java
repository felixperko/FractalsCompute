package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
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
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskProviderAdapter;
import de.felixperko.fractals.system.thread.CalculateThreadReference;
import de.felixperko.fractals.util.CategoryLogger;

public class BreadthFirstTaskManagerNew extends AbstractTaskManager<BreadthFirstTask> {

	static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}
	
	Comparator<BreadthFirstTask> comparator_distance = new Comparator<BreadthFirstTask>() {
		@Override
		public int compare(BreadthFirstTask arg0, BreadthFirstTask arg1) {
			return arg0.getDistance().compareTo(arg1.getDistance());
		}
	};

	Comparator<BreadthFirstTask> comparator_priority = new Comparator<BreadthFirstTask>() {
		@Override
		public int compare(BreadthFirstTask arg0, BreadthFirstTask arg1) {
			if (arg0 == null || arg1 == null)
				return 1;
			return arg0.getPriority().compareTo(arg1.getPriority());
		}
	};
	
	BreadthFirstMultilayerQueue queue;

	LayerConfiguration layerConfig;
	
	boolean done = false;
	
	BreadthFirstViewData viewData;
	
	Class<? extends FractalsCalculator> calculatorClass;
	int chunkSize;
	
	List<BreadthFirstTask> finishedTasks = new ArrayList<>();
	
	Map<Integer, Map<ClientConfiguration, ChunkUpdateMessage>> pendingUpdateMessages = new HashMap<>();
	
	CategoryLogger log;

	public BreadthFirstTaskManagerNew(ServerManagers managers, CalcSystem system) {
		super(managers, system);
		queue = new BreadthFirstMultilayerQueue(this, new ArrayList<BreadthFirstLayer>(), comparator_priority, comparator_distance);
		log = ((BreadthFirstSystem)system).getLogger().createSubLogger("tm");
	}
	
	double midpointChunkX;
	double midpointChunkY;
	
	NumberFactory numberFactory;
	ArrayChunkFactory chunkFactory;
	
	ComplexNumber midpoint;
	ComplexNumber leftLowerCorner;
	ComplexNumber rightUpperCorner;
	double leftLowerCornerChunkX, leftLowerCornerChunkY;
	double rightUpperCornerChunkX, rightUpperCornerChunkY;
	Number zoom;
	Number chunkZoom;
	ComplexNumber relativeStartShift;
	
	Map<String, ParamSupplier> parameters;
	
	Map<Integer, CalculateThreadReference> calculateThreadReferences = new HashMap<>();

	int id_counter_tasks = 0;
	
	int width, height;
	int chunksWidth, chunksHeight;
	
	int jobId = 0;

	List<TaskProviderAdapter> taskProviders = new ArrayList<>();

	@Override
	public void startTasks() {
		generateRootTask();
	}
	
	private void generateRootTask() {
		AbstractArrayChunk chunk = chunkFactory.createChunk(0, 0);
//		ComplexNumber pos = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
//		pos.multValues(relativeStartShift);
//		pos.add(midpoint);
		BreadthFirstTask rootTask = new BreadthFirstTask(id_counter_tasks++, this, chunk, parameters, getChunkPos(0, 0), createCalculator(), layerConfig.getLayers().get(0), jobId);
		rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layerConfig.getLayers().get(0));
		viewData.addChunk(chunk);
		queue.add(rootTask);
		openChunks++;
	}
	
	public FractalsCalculator createCalculator() {
		try {
			return calculatorClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Failed to create calculator for class: "+calculatorClass.getName());
		}
	}
	
	@Override
	public void run() {
		mainLoop : while (getLifeCycleState() != LifeCycleState.STOPPED) {
			while (getLifeCycleState() == LifeCycleState.PAUSED) {//calculate = false -> pause()
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!tick()) {
				setLifeCycleState(LifeCycleState.IDLE);
				while (!tick()) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						if (!(system.getLifeCycleState() == LifeCycleState.STOPPED))
							e.printStackTrace();
					}
				}
			}
		}
	}
	
	public boolean tick() {
		boolean changed = false;
		try {
			if (queue.fillQueues())
				changed = true;
			if (finishTasks())
				changed = true;
		} catch (Exception e) {
			if (system.getLifeCycleState() != LifeCycleState.STOPPED)
				throw e;
		}
		return changed;
	}

	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
		setLifeCycleState(LifeCycleState.PAUSED);
		System.out.println("setting params... "+System.currentTimeMillis());
		boolean reset = false;
		if (this.parameters != null) {
			for (ParamSupplier supplier : params.values()) {
				supplier.updateChanged(this.parameters.get(supplier.getName()));
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						reset = true;
				}
			}
		}
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
		
		reset |= queue.setGenerationBorder(getParamValue("border_generation", Double.class));
		reset |= queue.setDisposeBorder(getParamValue("border_dispose", Double.class));
		reset |= queue.setTaskBufferSize(getParamValue("task_buffer", Integer.class));

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
		
		if (reset)
			reset();
		
		LayerConfiguration oldLayerConfig = null;
		if (oldParams != null)
			oldLayerConfig = oldParams.get("layerConfiguration").getGeneral(LayerConfiguration.class);
		ParamSupplier newLayerConfigSupplier = params.get("layerConfiguration");
		LayerConfiguration newLayerConfig = newLayerConfigSupplier.getGeneral(LayerConfiguration.class);
		if (oldLayerConfig == null || newLayerConfigSupplier.isChanged()) {
			layerConfig = newLayerConfig;
			layerConfig.prepare(numberFactory);
			queue.setLayers(layerConfig.getLayers());
		} else {
			parameters.put("layerConfiguration", oldParams.get("layerConfiguration"));
		}
		
		if (viewData == null) {
			viewData = new BreadthFirstViewData(anchor);
		}
		chunkFactory.setViewData(viewData);

		leftLowerCorner = midpoint.copy();
		leftLowerCorner.sub(sideDist);
		leftLowerCornerChunkX = getChunkX(leftLowerCorner);
		leftLowerCornerChunkY = getChunkY(leftLowerCorner);
		
		rightUpperCorner = sideDist;
		rightUpperCorner.add(midpoint);
		rightUpperCornerChunkX = getChunkX(rightUpperCorner);
		rightUpperCornerChunkY = getChunkY(rightUpperCorner);
		
		if (params.get("midpoint").isChanged() || params.get("width").isChanged() || params.get("height").isChanged() || params.get("zoom").isChanged()) {
			updatePredictedMidpoint();
			if (!reset)
				queue.predictedMidpointUpdated();
		}
		
		if (reset)
			generateRootTask();
		
		setLifeCycleState(LifeCycleState.RUNNING);
		System.out.println("params set "+System.currentTimeMillis());
		return true;
	}
	
	private <T> T getParamValue(String paramName, Class<T> cls) {
		ParamSupplier supplier = parameters.get(paramName);
		if (supplier == null)
			return null;
		return supplier.getGeneral(cls);
	}
	
	private <T> T getParamValue(String paramName, Class<T> cls, boolean required) {
		T val = getParamValue(paramName, cls);
		if (val == null && required)
			throw new IllegalArgumentException("BreadthFirstTaskManager parameter '"+paramName+"' is required but doesn't exist.");
		return val;
	}

	@Override
	public List<? extends FractalsTask> getTasks(int count) {
		return queue.poll(count);
	}

	@Override
	public synchronized void taskFinished(BreadthFirstTask task) {
		finishedTasks.add(task);
		task.getStateInfo().setState(TaskState.FINISHED);
	}
	
	int openChunks;
	
	private boolean finishTasks() {
		if (finishedTasks.isEmpty())
			return false;
		setLifeCycleState(LifeCycleState.RUNNING);
		synchronized (this) {
			HashSet<ClientConfiguration> skipClients = new HashSet<>();
			List<ClientConfiguration> clients = new ArrayList<>(((BreadthFirstSystem)system).getClients());
			for (BreadthFirstTask task : finishedTasks) {
				
				final Integer taskId = task.getId();
				
				if (task.getStateInfo().getLayer().renderingEnabled()) {
					//skip clients if message already exists
					Map<ClientConfiguration, ChunkUpdateMessage> oldMessages = pendingUpdateMessages.get(taskId);
					if (oldMessages != null) {
						for (Entry<ClientConfiguration, ChunkUpdateMessage> e : oldMessages.entrySet()) {
							skipClients.add(e.getKey());
						}
					} else {
						oldMessages = new HashMap<>();
						pendingUpdateMessages.put(taskId, oldMessages);
					}
				
					//send update messages
					for (ClientConfiguration client : clients) {
//						if (skipClients.contains(client))
//							continue;
						ChunkUpdateMessage message = ((ServerNetworkManager)managers.getNetworkManager()).updateChunk(client, system, task.chunk);
						if (message != null){
							synchronized (oldMessages) {
								oldMessages.put(client, message);	
							}
							final BreadthFirstTaskManagerNew thisObj = this;
							final Map<ClientConfiguration, ChunkUpdateMessage> oldMessagesFinal = oldMessages;
							message.addSentCallback(new Runnable() {
								@Override
								public void run() {
									synchronized (thisObj) {
										oldMessagesFinal.remove(client);
										if (oldMessagesFinal.isEmpty())
											pendingUpdateMessages.remove(taskId);
									}
								}
							});
						}
					}
					skipClients.clear();
				}
				
				//update layer and re-add or dispose
				Layer currentLayer = task.getStateInfo().getLayer();
				int currentLayerId = currentLayer.getId();
				if (currentLayer.getId() >= layerConfig.getLayers().size()-1) {
					openChunks--;
					if (openChunks == 0) { //finished
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						system.stop();
					}
					task.getStateInfo().setState(TaskState.DONE);
				} else {
					currentLayerId++;
					BreadthFirstLayer layer = layerConfig.getLayers().get(currentLayerId);
					task.getStateInfo().setLayer(layer);
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layer);
					queue.add(task);
					task.getStateInfo().setState(TaskState.OPEN);
				}
			}
			finishedTasks.clear();
		}
		return true;
	}

	@Override
	public void endTasks() {
		reset();
	}

	@Override
	public synchronized void reset() {
		queue.reset();
		for (Map<ClientConfiguration, ChunkUpdateMessage> map : new HashMap<>(pendingUpdateMessages).values()) {
			for (ChunkUpdateMessage msg : map.values())
				msg.setCancelled(true);
		}
		pendingUpdateMessages.clear();
		finishedTasks.clear();
		jobId++;
		if (viewData != null) {
			viewData.dispose();
			viewData = null;
		}
		for (TaskProviderAdapter adapter : taskProviders)
			adapter.cancelTasks();
	}
	
	public void updatePredictedMidpoint() {
		ComplexNumber delta = midpoint.copy();
		delta.sub(viewData.anchor);
		delta.divNumber(chunkZoom);
		midpointChunkX = delta.realDouble();
		midpointChunkY = delta.imagDouble();
	}

	public ComplexNumber getChunkPos(long chunkX, long chunkY) {
		ComplexNumber chunkPos = numberFactory.createComplexNumber(chunkX, chunkY);
		chunkPos.add(relativeStartShift);
		chunkPos.mult(chunkZoom);
		chunkPos.add(viewData.anchor);
		return chunkPos;
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
	
	public void addTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter) {
		taskProviders.add(taskProviderAdapter);
	}
	
	public void removeTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter) {
		taskProviders.remove(taskProviderAdapter);
	}
}
