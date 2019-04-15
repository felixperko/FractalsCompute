package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.ChunkBorderData;
import de.felixperko.fractals.data.ChunkBorderDataImpl;
import de.felixperko.fractals.data.ChunkBorderDataImplNull;
import de.felixperko.fractals.data.NaiveChunk;
import de.felixperko.fractals.manager.common.Managers;
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
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTask;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTaskManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.thread.CalculateThreadReference;
import de.felixperko.fractals.util.CategoryLogger;

//first chunk at relative 0, 0
//generate neighbours -> add to open queue

/* N N N N N
 * N N C N N
 * N C 1 C N
 * N N C N N
 * N N N N N
 */

//choose next neigbour with lowest euclidian distance
//neighbour extracted -> generate neighbours that don't exist

/* N N C N N
 * N C 1 C N
 * C 1 1 1 C
 * N C 1 C N
 * N N C N N
 */

/* N C C C N
 * C 1 1 1 C
 * C 1 1 1 C
 * C 1 1 1 C
 * N C C C N
 */

//multiple passes -> multiple search instances (collect next task for each instance in queue according to priorization (fetch new from pass when taken)

public class BreadthFirstTaskManager extends AbstractTaskManager<BreadthFirstTask> {

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
	
	int buffer = 10;
	double border_generation = 0;
	double border_dispose = 5;
	
	List<Queue<BreadthFirstTask>> openTasks = new ArrayList<>();
	Queue<BreadthFirstTask> nextOpenTasks = new PriorityQueue<>(comparator_priority);//one entry for each pass -> 
	Queue<BreadthFirstTask> nextBufferedTasks = new PriorityQueue<>(comparator_priority);//buffer for highest priority tasks
	
	Queue<BreadthFirstTask> newQueue = new LinkedList<>(); //for generation

	List<BreadthFirstTask> borderTasks = new ArrayList<>();
	
	LayerConfiguration layerConfig;
	//List<BreadthFirstLayer> layers = new ArrayList<>();
	
	boolean done = false;
	
	BreadthFirstViewData viewData;
	
	
	Class<? extends FractalsCalculator> calculatorClass;
	int chunkSize;
	
	List<BreadthFirstTask> finishedTasks = new ArrayList<>();
	
	Map<Integer, Map<ClientConfiguration, ChunkUpdateMessage>> pendingUpdateMessages = new HashMap<>(); //TODO replace second map with Set/List of clients?
	
	CategoryLogger log;

	public BreadthFirstTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
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

	@Override
	public void startTasks() {
//		if (layers.isEmpty()) {
//			layers.add(new BreadthFirstUpsampleLayer(0, 2, chunkSize));
			//layers.add(new BreadthFirstLayer(1).with_priority_shift(5).with_priority_multiplier(2));
			//layers.add(new BreadthFirstLayer(2).with_priority_shift(10).with_priority_multiplier(3).with_samples(4));
//		}
		for (int i = 0 ; i < layerConfig.getLayers().size() ; i++) {
			openTasks.add(new PriorityQueue<>(comparator_distance));
			tempList.add(new ArrayList<>());
		}

		generateRootTask();
//		nextOpenTasks.add(rootTask);
//		
//		generateNeighbours(rootTask);
	}
	
	private void generateRootTask() {
		AbstractArrayChunk chunk = chunkFactory.createChunk(0, 0);
//		ComplexNumber pos = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
//		pos.multValues(relativeStartShift);
//		pos.add(midpoint);
		BreadthFirstTask rootTask = new BreadthFirstTask(id_counter_tasks++, this, chunk, parameters, getChunkPos(0, 0), createCalculator(), layerConfig.getLayers().get(0), jobId);
		rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layerConfig.getLayers().get(0));
		viewData.addChunk(chunk);
		openTasks.get(0).add(rootTask);
		openChunks++;
	}
	
	private FractalsCalculator createCalculator() {
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
			if (fillQueues())
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
		chunkFactory.setViewData(viewData);

		leftLowerCorner = midpoint.copy();
		leftLowerCorner.sub(sideDist);
		leftLowerCornerChunkX = getChunkX(leftLowerCorner);
		leftLowerCornerChunkY = getChunkY(leftLowerCorner);
		
		rightUpperCorner = sideDist;
		rightUpperCorner.add(midpoint);
		rightUpperCornerChunkX = getChunkX(rightUpperCorner);
		rightUpperCornerChunkY = getChunkY(rightUpperCorner);
		
		if (params.get("midpoint").isChanged() || params.get("width").isChanged() || params.get("height").isChanged()) {
			updatePredictedMidpoint();
			if (!reset)
				predictedMidpointUpdated();
		}
		
		if (reset)
			generateRootTask();
		
		setLifeCycleState(LifeCycleState.RUNNING);
		System.out.println("params set "+System.currentTimeMillis());
		return true;
	}

	@Override
	public List<? extends FractalsTask> getTasks(int count) {
		List<BreadthFirstTask> tasks = new ArrayList<>();
		for (int i = 0 ; i < count ; i++) {
			BreadthFirstTask task = null;
			for (int try1 = 0 ; try1 < 3 ; try1++){
				try {
					task = nextBufferedTasks.poll();
					if (task != null)
						break;
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			if (task == null)
				break;
			
			if (task.getPreviousLayer() != null && task.getPreviousLayer().cullingEnabled()) {
				Map<BorderAlignment, ChunkBorderData> neighbourBorderData = new HashMap<>();
				AbstractArrayChunk chunk = ((AbstractArrayChunk)task.getChunk());
				int x = chunk.getChunkX();
				int y = chunk.getChunkX();
				for (BorderAlignment alignment : BorderAlignment.values()) {
					Chunk c = viewData.getChunk(alignment.getNeighbourX(x), alignment.getNeighbourY(y));
					BorderAlignment relative = alignment.getAlignmentForNeighbour();
					if (c == null) {
						neighbourBorderData.put(relative, new ChunkBorderDataImplNull());
					} else {
						AbstractArrayChunk neighbour = (AbstractArrayChunk) c;
						neighbourBorderData.put(relative, neighbour.getBorderData(alignment));
					}
				}
				chunk.setNeighbourBorderData(neighbourBorderData);
			}
				
			tasks.add(task);
			task.getStateInfo().setState(TaskState.ASSIGNED);
		}
		return tasks;
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
						if (skipClients.contains(client))
							continue;
						ChunkUpdateMessage message = ((ServerNetworkManager)managers.getNetworkManager()).updateChunk(client, system, task.chunk);
						synchronized (oldMessages) {
							oldMessages.put(client, message);	
						}
						final Map<ClientConfiguration, ChunkUpdateMessage> oldMessagesFinal = oldMessages;
						message.addSentCallback(new Runnable() {
							@Override
							public void run() {
								synchronized (oldMessagesFinal) {
									oldMessagesFinal.remove(client);
									if (oldMessagesFinal.isEmpty())
										pendingUpdateMessages.remove(taskId);
								}
							}
						});
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
					Layer layer = layerConfig.getLayers().get(currentLayerId);
					task.getStateInfo().setLayer(layer);
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layer);
					openTasks.get(currentLayerId).add(task);
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
		for (Map<ClientConfiguration, ChunkUpdateMessage> map : new HashMap<>(pendingUpdateMessages).values()) {
			for (ChunkUpdateMessage msg : map.values())
				msg.setCancelled(true);
		}
		pendingUpdateMessages.clear();
		for (Queue<BreadthFirstTask> openQueue : openTasks)
			openQueue.clear();
		for (List<BreadthFirstTask> temp : tempList)
			temp.clear();
		nextOpenTasks.clear();
		nextBufferedTasks.clear();
		finishedTasks.clear();
		borderTasks.clear();
		newQueue.clear();
		jobId++;
		if (viewData != null) {
			viewData.dispose();
			viewData = null;
		}
		//TODO abort running tasks
	}
	
	public void updatePredictedMidpoint() {
		ComplexNumber delta = midpoint.copy();
		delta.sub(viewData.anchor);
		delta.divNumber(chunkZoom);
		midpointChunkX = delta.realDouble();
		midpointChunkY = delta.imagDouble();
	}

	List<List<BreadthFirstTask>> tempList = new ArrayList<>(); //used when refreshing sorting order

	//TODO use
	public void predictedMidpointUpdated() {
		//TODO synchronization
		//clear queues to update sorting
		synchronized (this) {
			List<BreadthFirstLayer> layers = layerConfig.getLayers();
			for (int l = 0 ; l < layers.size() ; l++) {
				tempList.get(l).addAll(openTasks.get(l));
				openTasks.get(l).clear();
			}
			for (BreadthFirstTask task : nextOpenTasks) {
				tempList.get(task.getStateInfo().getLayer().getId()).add(task);
			}
			nextOpenTasks.clear();
			for (BreadthFirstTask task : nextBufferedTasks) {
				tempList.get(task.getStateInfo().getLayer().getId()).add(task);
			}
			nextBufferedTasks.clear();
			
			//add border tasks
			Iterator<BreadthFirstTask> borderIt = borderTasks.iterator();
			while (borderIt.hasNext()) {
				BreadthFirstTask borderTask = borderIt.next();
				if (getScreenDistance(borderTask.getChunk()) <= border_generation) {
					borderTask.getStateInfo().setState(TaskState.OPEN);
					openTasks.get(borderTask.getStateInfo().getLayer().getId()).add(borderTask);
					borderIt.remove();
				}
			}
			
			//re-add
//			boolean addedMidpoint = false;
			for (int l = 0 ; l < layers.size() ; l++) {
				for (BreadthFirstTask task : tempList.get(l)) {
//					if (task.getChunk().getChunkX() == (long)midpointChunkX && task.getChunk().getChunkY() == (long)midpointChunkY)
//						addedMidpoint = true;
					if (getScreenDistance(task.getChunk()) > border_dispose) {
						task.getStateInfo().setState(TaskState.REMOVED);
						continue;
					}
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(l));
					openTasks.get(l).add(task);
				}
				tempList.get(l).clear();
			}
			
			//add task for chunk at midpoint if not calculated
			int midpointChunkXFloor = (int)midpointChunkX;
			int midpointChunkYFloor = (int)midpointChunkY;
			if (!viewData.hasChunk(midpointChunkXFloor, midpointChunkYFloor)){
				AbstractArrayChunk chunk = chunkFactory.createChunk(midpointChunkXFloor, midpointChunkYFloor);
				BreadthFirstTask rootTask = new BreadthFirstTask(id_counter_tasks++, this,
						chunk, parameters, getChunkPos(midpointChunkXFloor, midpointChunkYFloor), createCalculator(), layers.get(0), jobId);
				rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(0));
				viewData.addChunk(chunk);
				openTasks.get(0).add(rootTask);
				openChunks++;
			}
			
			//re-fill
//			fillQueues();//TODO sync?
		}
		
	}
	
	
	private synchronized boolean fillQueues() {
		boolean changed = false;
		List<BreadthFirstLayer> layers = layerConfig.getLayers();
		while (nextBufferedTasks.size() < buffer) {
			boolean[] layerInNextTasks = new boolean[layers.size()];
			int notFilled = layers.size();
			//fill nextOpenTasks with next values from openTasks
			for (BreadthFirstTask nextOpenTask : nextOpenTasks) {
				layerInNextTasks[nextOpenTask.getStateInfo().getLayer().getId()] = true;
				notFilled--;
			}
			if (notFilled > 0) {
				for (int l = 0 ; l < layers.size() ; l++) {
					if  (layerInNextTasks[l])
						continue;
					BreadthFirstTask task = openTasks.get(l).poll();
					if (task == null)
						continue;
					changed = true;
					nextOpenTasks.add(task);
					if (task.getStateInfo().getLayer().getId() == 0)
						generateNeighbours(task);
				}
			}
//			if (nextOpenTasks.isEmpty()) {
//				for (int i = 0 ; i < openTasks.size() ; i++) {
//					BreadthFirstTask task = openTasks.get(i).poll();
//					if (task != null) {
//						changed = true;
//						nextOpenTasks.add(task);
//						if (task.getStateInfo().getLayer().getId() == 0)
//							generateNeighbours(task);
//					}
//					else
//						log.log("no open tasks on layer "+i);
//				}
//			}
			//cascade further if not empty
			if (!nextOpenTasks.isEmpty()) {
//				log.log("nextOpenTasks: ");
//				for (BreadthFirstTask task : nextOpenTasks)
//					log.log(" - "+task.getStateInfo().getLayer().getId()+": "+task.getChunk().getChunkX()+"/"+task.getChunk().getChunkY()
//							+" prio="+task.getPriority()+" dist="+task.getDistance());
//				int i = 0;
//				for (Queue<BreadthFirstTask> list : openTasks) {
//					log.log("open layer: "+(i++));
//					for (BreadthFirstTask task : list)
//					log.log(" - "+task.getStateInfo().getLayer().getId()+": "+task.getChunk().getChunkX()+"/"+task.getChunk().getChunkY()
//							+" prio="+task.getPriority()+" dist="+task.getDistance());
//				}
				BreadthFirstTask polled = nextOpenTasks.poll();
				nextBufferedTasks.add(polled);
				changed = true;
				Queue<BreadthFirstTask> queue = openTasks.get(polled.getStateInfo().getLayer().getId());
				if (!queue.isEmpty()) {
					BreadthFirstTask polledTask = queue.poll();
					nextBufferedTasks.add(polledTask);
					//new chunk at layer 0 taken -> generate neighbours -> fill queues
					if (polled.getStateInfo().getLayer().getId() == 0)
						generateNeighbours(polledTask);
				}
			} else { //lower priority queues empty -> generate more tasks or stop
				break;
			}
		}
		return changed;
	}

	private void generateNeighbours(BreadthFirstTask polledTask) {
		double highestDistance = width >= height ? width : height;
		highestDistance *= 0.5 * Math.sqrt(2);
//		highestDistance += chunkSize;
//		if (polledTask.getChunk().distance(midpointChunkX, midpointChunkY) > highestDistance/chunkSize)
//			return; //TODO generate tasks later when shift brings task into view
		generateNeighbourIfNotExists(polledTask, 1, 0);
		generateNeighbourIfNotExists(polledTask, -1, 0);
		generateNeighbourIfNotExists(polledTask, 0, 1);
		generateNeighbourIfNotExists(polledTask, 0, -1);
		//add new neigbours to storing queue
		for (BreadthFirstTask newTask : newQueue) {
			double screenDistance = getScreenDistance(newTask.getChunk());
			if (screenDistance > border_generation) {
				newTask.getStateInfo().setState(TaskState.BORDER);
				borderTasks.add(newTask);
			} else {
				openTasks.get(newTask.getStateInfo().getLayer().getId()).add(newTask);
			}
		}
		newQueue.clear();
	}

	private boolean generateNeighbourIfNotExists(BreadthFirstTask parentTask, int dx, int dy) {
		if (done)
			return false;
		
		Integer chunkX = parentTask.getChunk().getChunkX() + dx;
		Integer chunkY = parentTask.getChunk().getChunkY() + dy;
		
		if (viewData.hasChunk(chunkX, chunkY)) //already exists
			return false;
		
		AbstractArrayChunk chunk = null;
		chunkFactory.setViewData(viewData);
		for (int try1 = 0 ; try1 < 10 ; try1++) {//TODO remove
			try {
				chunk = chunkFactory.createChunk(chunkX, chunkY);
				break;
			} catch (Exception e) {
				if (try1 == 9 && parentTask.getJobId() == jobId)
					throw e;
				else
					try {
						Thread.sleep(1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			}
		}
		BreadthFirstTask task = new BreadthFirstTask(id_counter_tasks++, this, chunk, parameters, getChunkPos(chunkX, chunkY), createCalculator(), layerConfig.getLayers().get(0), jobId);
		task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layerConfig.getLayers().get(0));
		openChunks++;
		newQueue.add(task);
		viewData.addChunk(chunk);
		return true;
	}

	private ComplexNumber getChunkPos(long chunkX, long chunkY) {
		ComplexNumber chunkPos = numberFactory.createComplexNumber(chunkX, chunkY);
		chunkPos.add(relativeStartShift);
		chunkPos.multNumber(chunkZoom);
		chunkPos.add(viewData.anchor);
		return chunkPos;
	}
	
	private double getChunkX(ComplexNumber pos) {
		Number value = pos.getReal();
		value.sub(viewData.anchor.getReal());
		value.div(chunkZoom);
		return value.toDouble();
	}
	
	private double getChunkY(ComplexNumber pos) {
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

	private double getScreenDistance(Chunk chunk) {
		return getScreenDistance(chunk.getChunkX(), chunk.getChunkY());
	}
}
