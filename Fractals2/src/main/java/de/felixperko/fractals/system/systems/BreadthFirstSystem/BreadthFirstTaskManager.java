package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.ChunkBorderData;
import de.felixperko.fractals.data.ChunkBorderDataNullImpl;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.manager.server.ServerThreadManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.infra.connection.ClientLocalConnection;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.system.calculator.infra.DeviceType;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.statistics.HistogramStats;
import de.felixperko.fractals.system.statistics.SummedHistogramStats;
import de.felixperko.fractals.system.statistics.TimesliceProvider;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.infra.ViewData;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskProviderAdapter;
import de.felixperko.fractals.system.thread.CalculateFractalsThread;
import de.felixperko.fractals.system.thread.CalculateThreadReference;
import de.felixperko.fractals.util.NumberUtil;

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
	
	Comparator<BreadthFirstTask> comparator_distance = new Comparator<BreadthFirstTask>() {
		@Override
		public int compare(BreadthFirstTask arg0, BreadthFirstTask arg1) {
			if (arg0 == null)
				return -1;
			if (arg1 == null)
				return 1;
			return arg0.getDistance().compareTo(arg1.getDistance());
		}
	};

	Comparator<BreadthFirstTask> comparator_priority = new Comparator<BreadthFirstTask>() {
		@Override
		public int compare(BreadthFirstTask arg0, BreadthFirstTask arg1) {
			if (arg0 == null)
				return -1;
			if (arg1 == null)
				return 1;
			return arg0.getPriority().compareTo(arg1.getPriority());
		}
	};
	
	private static final Logger LOG = LoggerFactory.getLogger(BreadthFirstTaskManager.class);
	
	List<Queue<BreadthFirstTask>> openTasks = new ArrayList<>();
	Queue<BreadthFirstTask> nextOpenTasks = new PriorityQueue<>(comparator_priority);//highest priority chunk for each layer that isn't in nextBufferedTasks
	Queue<BreadthFirstTask> nextBufferedTasks = new PriorityQueue<>(comparator_priority);//buffer for highest priority tasks
	List<List<BreadthFirstTask>> tempList = new ArrayList<>(); //used when refreshing sorting order
	List<BreadthFirstTask> borderTasks = new ArrayList<>(); //chunks outside of the current view
	Queue<BreadthFirstTask> newQueue = new LinkedList<>(); //for generation
	List<BreadthFirstTask> finishedTasks = new ArrayList<>();

	List<BreadthFirstTask> assignedTasks = new ArrayList<>();
	
	boolean updatedPredictedMidpoint = false;

	BFSystemContext context;
	
	boolean done = false;
	
	//Map<Integer, Map<ClientConfiguration, List<ChunkUpdateMessage>>> pendingUpdateMessages = new HashMap<>(); //TODO use! ; replace second map with Set/List of clients?
	Map<ClientConfiguration, List<ChunkUpdateMessage>> pendingUpdateMessages = new HashMap<>(); //TODO use! ; replace second map with Set/List of clients?
	
	double midpointChunkX;
	double midpointChunkY;

	Map<Integer, CalculateThreadReference> calculateThreadReferences = new HashMap<>(); //TODO remove?

	int id_counter_tasks = 0;
	
	SummedHistogramStats stats = new SummedHistogramStats();

	public BreadthFirstTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
		context = new BFSystemContext(this, system.getParameterConfiguration());
		//log = ((BreadthFirstSystem)system).getLogger().createSubLogger("tm");
	}

	@Override
	public void startTasks() {
		
		
		//TODO readd when layerConfig changes?!
		for (int i = 0 ; i < context.layerConfig.getLayers().size() ; i++) {
			openTasks.add(new PriorityQueue<>(comparator_distance));
			tempList.add(new ArrayList<>());
		}

		generateRootTask();
	}
	
	protected void generateRootTask() {
		generateTask(0, 0);
	}
	
	public BreadthFirstTask generateTask(long chunkX, long chunkY) {
		
		AbstractArrayChunk chunk = context.chunkFactory.createChunk(chunkX, chunkY);
		BreadthFirstTask task = new BreadthFirstTask(context, id_counter_tasks++, this, chunk, context.getPos(chunkX, chunkY), 
				context.layerConfig.getLayers().get(0), context.getViewId());
		task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, context.layerConfig.getLayers().get(0));
		context.getActiveViewData().insertBufferedChunk(chunk, true);
		openTasks.get(0).add(task);
		openChunks++;
		return task;
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
					if (getLifeCycleState() == LifeCycleState.STOPPED)
						break mainLoop;
				}
			}
		}
	}
	
	public boolean tick() {
		boolean changed = false;
		try {
			if (updateStats())
				changed = true;
			if (fillQueues())
				changed = true;
			if (finishTasks())
				changed = true;
			if (predictedMidpointUpdated())
				changed = true;
			ViewData activeViewData = context.getActiveViewData();
			if (activeViewData != null)
				activeViewData.tick();
		} catch (Exception e) {
			if (system.getLifeCycleState() != LifeCycleState.STOPPED)
				throw e;
		}
		return changed;
	}

	Integer lastTimeslice = 0;
	int timesliceTasksFinished = 0;
	
	private boolean updateStats() {
		ServerThreadManager threadManager = ((ServerManagers)managers).getThreadManager();
		TimesliceProvider timesliceProvider = threadManager.getTimesliceProvider();
		Integer timeslice = timesliceProvider.getCurrentTimeslice();
		if (timeslice <= lastTimeslice)
			return false;
		for (Integer i = lastTimeslice-1 ; i < timeslice ; i++) {
			int totalIterationsPerSecond = 0;
			List<CalculateFractalsThread> threads = threadManager.getCalculateThreads();
			int[] ips_threads = new int[threads.size()];
			int i2 = 0;
			for (CalculateFractalsThread thread : new ArrayList<>(threads)) {
				String name = thread.getName();
				//TODO just for debug
				int iterations = thread.getTimesliceIterations(i, false);
//				int iterations = thread.getTimesliceIterations(i, i == timeslice-1);
				int iterationsPerSecond = iterations * (int)Math.round(1d/ServerThreadManager.TIMESLICE_INTERVAL);
				totalIterationsPerSecond += iterationsPerSecond;
				LOG.debug("IPS "+timeslice+" of "+name+"):	"+iterationsPerSecond);
				ips_threads[i2] = iterationsPerSecond;
				i2++;
			}
			LOG.info("IPS "+timeslice+" (total):	"+totalIterationsPerSecond+" tasks finished: "+timesliceTasksFinished);
			timesliceTasksFinished = 0;
			getSystem().getSystemStateInfo().updateIterationsPerSecond(timeslice, 
					timesliceProvider.getStartTime(timeslice), timesliceProvider.getEndTime(timeslice), totalIterationsPerSecond, ips_threads);
		}
		lastTimeslice = timeslice;
		return true;
	}

	@Override
	public boolean setParameters(ParamContainer paramContainer) {
		setLifeCycleState(LifeCycleState.PAUSED);
		
		boolean reset = context.setParameters(paramContainer);
		
		Map<String, ParamSupplier> params = paramContainer.getClientParameters();
		if (params.get("midpoint").isChanged() || params.get("width").isChanged() || params.get("height").isChanged() || params.get("zoom").isChanged()) {
			updatePredictedMidpoint();
			if (!reset)
				updatedPredictedMidpoint = true;
		}
		
		setLifeCycleState(LifeCycleState.RUNNING);
		
		if (reset)
			generateRootTask();
		
		return reset;
	}

	@Override
	public List<? extends FractalsTask> getTasks(DeviceType deviceType, int count) {
		
		if (deviceType == DeviceType.CPU && context.getCpuCalculatorClass() == null)
			return null;
		if (deviceType == DeviceType.GPU && context.getGpuCalculatorClass() == null)
			return null;
		
		List<BreadthFirstTask> tasks = new ArrayList<>();
		synchronized (this){
			for (int i = 0 ; i < count ; i++) {
				
				//TODO remove
//				BreadthFirstTask task = null;
//				for (int try1 = 0 ; try1 < 3 ; try1++){
//					try {
//						task = nextBufferedTasks.poll();
						
//						if (task != null)
//							break;
//					} catch (NullPointerException e) {
//						e.printStackTrace();
//					}
//				}

				BreadthFirstTask task = nextBufferedTasks.poll();
				if (task == null)
					return null;

				
				//Prepare culling
//				if (task.isPreviousLayerCullingEnabled()) {
					Map<BorderAlignment, ChunkBorderData> neighbourBorderData = new HashMap<>();
					AbstractArrayChunk chunk = ((AbstractArrayChunk)task.getChunk());
					int x = chunk.getChunkX();
					int y = chunk.getChunkY();
					for (BorderAlignment alignment : BorderAlignment.values()) {
						BorderAlignment relative = alignment.getAlignmentForNeighbour();
						Chunk c = context.getActiveViewData().getBufferedChunk(alignment.getNeighbourX(x), alignment.getNeighbourY(y));
						if (c == null) {
							neighbourBorderData.put(alignment, new ChunkBorderDataNullImpl());
						} else {
							AbstractArrayChunk neighbour = (AbstractArrayChunk) c;
							neighbourBorderData.put(alignment, neighbour.getBorderData(relative));
						}
					}
					chunk.setNeighbourBorderData(neighbourBorderData);
//				}
				
				tasks.add(task);
				assignedTasks.add(task);
				task.setContext(context);
				task.getStateInfo().setState(TaskState.ASSIGNED);
			}
		}
		return tasks;
	}

	@Override
	public synchronized void taskFinished(FractalsTask task) {
		if (context.getViewId() != task.getJobId())
			return;
		BreadthFirstTask bfTask = (BreadthFirstTask) task;
		finishedTasks.add(bfTask);
		task.getStateInfo().setState(TaskState.FINISHED);
	}
	
	int openChunks;
	
	private boolean finishTasks() {
		if (finishedTasks.isEmpty())
			return false;
		setLifeCycleState(LifeCycleState.RUNNING);
		List<ClientConfiguration> clients = system.getClients();
		List<BreadthFirstTask> finishedTasks = null;
		synchronized (this) {
			finishedTasks = new ArrayList<>(this.finishedTasks);
			this.finishedTasks.clear();
		}
		for (BreadthFirstTask task : finishedTasks) {

			if (context.getViewId() != task.getJobId())
				continue;
			
			synchronized(this) {
				assignedTasks.remove(task);
			}
				
			BreadthFirstViewData activeViewData = context.getActiveViewData();
			if (activeViewData == null)
				continue;
			
			//update stats
			HistogramStats taskStats = task.getTaskStats();
			if (taskStats != null)
				stats.addHistogram(taskStats);
			//TODO distribute stat update?
			
			boolean paused = task.isCancelled() && task.getState() == TaskState.BORDER;

			if (!paused) {
				Chunk chunk = task.getChunk();
				CompressedChunk compressedChunk = activeViewData.updateBufferedAndCompressedChunk(chunk);
				
				//distribute
				if (task.getStateInfo().getLayer().renderingEnabled()) {
					
					//send update messages
					synchronized (clients) {
						int sent = 0;
						for (ClientConfiguration client : clients) {
							if (!(client.getConnection() instanceof ClientLocalConnection)) {
								ChunkUpdateMessage msg = ((ServerNetworkManager)managers.getNetworkManager()).updateChunk(client, system, compressedChunk);
								sent++;
//								msg.addSentCallback(() -> {
//									if (msg.getChunk().getJobId() != context.getViewId())
//										msg.setCancelled(true);
//								});
//									List<ChunkUpdateMessage> clientMsgs = pendingUpdateMessages.get(client);
//									if (clientMsgs == null) {
//										clientMsgs = new ArrayList<>();
//										pendingUpdateMessages.put(client, clientMsgs);
//									}
//									clientMsgs.add(msg);
//									final List<ChunkUpdateMessage> finalList = clientMsgs;
//									msg.addSentCallback(() -> {finalList.remove(msg);});
							}
						}
						LOG.debug("sent chunkUpdate to "+sent+" clients.");
					}
				}
				
				timesliceTasksFinished++;
			}
		
			//update layer and re-add or dispose
			Layer currentLayer = task.getStateInfo().getLayer();
			int currentLayerId = currentLayer.getId();
			if (currentLayer.getId() >= context.layerConfig.getLayers().size()-1) {
				openChunks--;
				if (openChunks == 0) { //finished
					
				}
				task.getStateInfo().setState(TaskState.DONE);
			} else {
				if (!paused) {
					currentLayerId++;
					Layer layer = context.layerConfig.getLayers().get(currentLayerId);
					task.getStateInfo().setLayer(layer);
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layer);
					openTasks.get(currentLayerId).add(task);
					task.getStateInfo().setState(TaskState.OPEN);
				} else {
					borderTasks.add(task);
				}
			}
		}
		finishedTasks.clear();
		return true;
	}

	@Override
	public void endTasks() {
		reset();
	}

	@Override
	public synchronized void reset() {
//		for (Map<ClientConfiguration, List<ChunkUpdateMessage>> map : pendingUpdateMessages.values()) {
			for (List<ChunkUpdateMessage> msgs : pendingUpdateMessages.values()) {
				for (ChunkUpdateMessage msg : msgs)
					msg.setCancelled(true);
				msgs.clear();
			}
//		}
		pendingUpdateMessages.clear();
		for (Queue<BreadthFirstTask> openQueue : openTasks)
			openQueue.clear();
		for (List<BreadthFirstTask> temp : tempList)
			temp.clear();
		assignedTasks.clear();
		nextOpenTasks.clear();
		nextBufferedTasks.clear();
		finishedTasks.clear();
		borderTasks.clear();
		newQueue.clear();
		stats.reset();
		BreadthFirstViewData viewData = context.getActiveViewData();
		if (viewData != null) {
			viewData.dispose();
			context.setActiveViewData(null); //TODO does that make sense?
		}
		for (TaskProviderAdapter adapter : taskProviders)
			adapter.cancelTasks();
	}
	
	public void updatePredictedMidpoint() {
		ComplexNumber delta = context.midpoint.copy();
		delta.sub(((BreadthFirstViewData)context.getActiveViewData()).anchor);
		delta.divNumber(context.chunkZoom);
		midpointChunkX = delta.realDouble();
		midpointChunkY = delta.imagDouble();
	}

	public boolean predictedMidpointUpdated() {
		if (!updatedPredictedMidpoint)
			return false;
		updatedPredictedMidpoint = false;
		List<Layer> layers;
		//clear queues to update sorting
		synchronized (this) {
			//cancel running tasks if outside of calculation area
			Iterator<BreadthFirstTask> assignedIt = assignedTasks.iterator();
			while (assignedIt.hasNext()) {
				BreadthFirstTask task = assignedIt.next();
				FractalsCalculator calculator = task.getCalculator();
				//TODO wip
				if (calculator != null) {
					try {
						Layer layer = task.getStateInfo().getLayer();
						task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layer);
					} catch (IndexOutOfBoundsException e){
						task.setCancelled(true);
						assignedIt.remove();
						LOG.info("removed task: layerId="+task.getStateInfo().getLayerId()+" not valid.");
						continue;
					}
					if (context.getDrawRegionDistance(task.getChunk()) > context.border_generation) {
						calculator.setCancelled();
						assignedIt.remove();
						tempList.get(task.getLayerId()).add(task);
					}
				}
			}
			
			//extract pending tasks
			layers = context.layerConfig.getLayers();
			for (int l = 0 ; l < layers.size() ; l++) {
				tempList.get(l).addAll(openTasks.get(l));
				openTasks.get(l).clear();
			}
			for (BreadthFirstTask task : nextOpenTasks) {
				tempList.get(task.getStateInfo().getLayer().getId()).add(task);
			}
			nextOpenTasks.clear();
			for (BreadthFirstTask task : nextBufferedTasks) {
				TaskStateInfo stateInfo = task.getStateInfo();
				Layer layer = stateInfo.getLayer();
				Integer id = layer.getId();
				tempList.get(id).add(task);
			}
			nextBufferedTasks.clear();
			
			//add border tasks
			Iterator<BreadthFirstTask> borderIt = borderTasks.iterator();
			while (borderIt.hasNext()) {
				BreadthFirstTask borderTask = borderIt.next();
				double drawRegionDistance = context.getDrawRegionDistance(borderTask.getChunk());
				if (drawRegionDistance > context.border_dispose){
					borderTask.getStateInfo().setState(TaskState.REMOVED);
					borderIt.remove();
				}
				else if (drawRegionDistance <= context.border_generation) {
					borderTask.getStateInfo().setState(TaskState.OPEN);
					openTasks.get(borderTask.getStateInfo().getLayer().getId()).add(borderTask);
					borderIt.remove();
				}
			}
		}
			
		BreadthFirstViewData viewData;
		synchronized (context) {
			viewData = context.getActiveViewData();
			if (viewData == null) {
				tempList.clear();
				return true;
			}
		}
		
		synchronized (this) {
			//re-add
//			boolean addedMidpoint = false;
			for (int l = 0 ; l < layers.size() ; l++) {
				
				for (BreadthFirstTask task : tempList.get(l)) {
//					if (task.getChunk().getChunkX() == (long)midpointChunkX && task.getChunk().getChunkY() == (long)midpointChunkY)
//						addedMidpoint = true;
					double screenDistance = context.getDrawRegionDistance(task.getChunk());
					if (screenDistance > context.border_dispose) {
						task.getStateInfo().setState(TaskState.REMOVED);
						continue;
					}
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(l));
					if (screenDistance > context.border_generation) {
						task.getStateInfo().setState(TaskState.BORDER);
						borderTasks.add(task);
					} else {
						openTasks.get(l).add(task);
					}
				}
				tempList.get(l).clear();
			}
		
		//add task for chunk at midpoint if not calculated
		int midpointChunkXFloor = (int)midpointChunkX;
		int midpointChunkYFloor = (int)midpointChunkY;
			if (!viewData.hasCompressedChunk(midpointChunkXFloor, midpointChunkYFloor)){
				AbstractArrayChunk chunk = null;
				try {
					chunk = context.chunkFactory.createChunk(midpointChunkXFloor, midpointChunkYFloor);
				} catch (IllegalStateException e){
					LOG.error("IllegalStateException in while creating root task");
					e.printStackTrace();
					return true;
				}
				BreadthFirstTask rootTask = new BreadthFirstTask(context, id_counter_tasks++, this, chunk, context.getPos(midpointChunkXFloor, midpointChunkYFloor),
						layers.get(0), context.getViewId());
				rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(0));
				viewData.insertBufferedChunk(chunk, true);
				openTasks.get(0).add(rootTask);
				openChunks++;
			}
			
			getSystem().getSystemStateInfo().getServerStateInfo().updateMidpoint(system.getId(), 
					context.numberFactory.createComplexNumber(midpointChunkX, midpointChunkY));
			//re-fill
//			fillQueues();//TODO sync?
		}
		return true;
	}
	
	private synchronized boolean fillQueues() {
		boolean changed = false;
		List<Layer> layers = context.layerConfig.getLayers();
		
		//buffer not filled -> fill if pending tasks are available
		while (nextBufferedTasks.size() < context.buffer) {
			boolean[] layerInNextTasks = new boolean[layers.size()]; //nextOpenTasks contains nearest chunk of each layer, track missing
			int notFilled = layers.size();
			//fill nextOpenTasks with next values from openTasks
			for (BreadthFirstTask nextOpenTask : nextOpenTasks) {
				layerInNextTasks[nextOpenTask.getStateInfo().getLayer().getId()] = true;
				notFilled--;
			}
			if (notFilled > 0) { //nextOpenTasks is missing next task for at least one layer. refill if available.
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
			
			//cascade further if not empty
			if (!nextOpenTasks.isEmpty()) {
				BreadthFirstTask polled = nextOpenTasks.poll();
				nextBufferedTasks.add(polled);
				changed = true;
				Queue<BreadthFirstTask> queue = openTasks.get(polled.getStateInfo().getLayer().getId());
				if (!queue.isEmpty()) {
					BreadthFirstTask polledTask = queue.poll();
					if (polledTask != null){
						nextBufferedTasks.add(polledTask);
						//new chunk at layer 0 taken -> generate neighbours, fill queues
						if (polled.getStateInfo().getLayer().getId() == 0)
							generateNeighbours(polledTask);
					}
				}
			} else { //lower priority queues empty -> generate more tasks or stop
				break;
			}
		}
		return changed;
	}

	private void generateNeighbours(BreadthFirstTask polledTask) {
		//TODO use variable?
		double highestDistance = context.width >= context.height ? context.width : context.height;
		highestDistance *= 0.5 * Math.sqrt(2);
//		highestDistance += chunkSize;
//		if (polledTask.getChunk().distance(midpointChunkX, midpointChunkY) > highestDistance/chunkSize)
//			return; //TODO generate tasks later when shift brings task into view
		long t1 = System.nanoTime();
		generateNeighbourIfNotExists(polledTask, 1, 0);
		generateNeighbourIfNotExists(polledTask, -1, 0);
		generateNeighbourIfNotExists(polledTask, 0, 1);
		generateNeighbourIfNotExists(polledTask, 0, -1);
		long t2 = System.nanoTime();
		if (!newQueue.isEmpty()) {
			double time = NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS*(t2-t1), 6);
			LOG.trace("time to generate "+newQueue.size()+" Tasks: "+time+" ms");
		}
		//add new neigbours to storing queue
		for (BreadthFirstTask newTask : newQueue) {
			double screenDistance = context.getDrawRegionDistance(newTask.getChunk());
			TaskStateInfo stateInfo = newTask.getStateInfo();
			if (screenDistance > context.border_generation) {
				stateInfo.setState(TaskState.BORDER);
				borderTasks.add(newTask);
			} else {
				Layer layer = stateInfo.getLayer();
				int layerId = layer.getId();
				Queue<BreadthFirstTask> openLayerTasks = openTasks.get(layerId);
				openLayerTasks.add(newTask);
			}
		}
		newQueue.clear();
	}

	protected boolean generateNeighbourIfNotExists(BreadthFirstTask parentTask, int dx, int dy) {
		if (done)
			return false;
		
		Integer chunkX = parentTask.getChunk().getChunkX() + dx;
		Integer chunkY = parentTask.getChunk().getChunkY() + dy;
		
		if (context.getActiveViewData().hasCompressedChunk(chunkX, chunkY)) //already exists
			return false;
		
		AbstractArrayChunk chunk = null;
		context.chunkFactory.setViewData(context.getActiveViewData());
		//TODO remove
//		for (int try1 = 0 ; try1 < 10 ; try1++) {
//			try {
				chunk = context.chunkFactory.createChunk(chunkX, chunkY);
//				break;
//			} catch (Exception e) {
//				if (try1 == 9 && parentTask.getJobId() == context.getViewId())
//					throw e;
//				else
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//			}
//		}
		BreadthFirstTask task = new BreadthFirstTask(context, id_counter_tasks++, this, chunk, context.getPos(chunkX, chunkY),
				context.layerConfig.getLayers().get(0), context.getViewId());
		task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, context.layerConfig.getLayers().get(0));
		openChunks++;
		newQueue.add(task);
		context.getActiveViewData().insertBufferedChunk(chunk, true);
		return true;
	}

	public BFSystemContext getSystemContext() {
		return context;
	}
}