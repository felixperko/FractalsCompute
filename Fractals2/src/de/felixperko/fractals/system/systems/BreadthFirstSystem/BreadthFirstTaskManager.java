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

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.ChunkBorderData;
import de.felixperko.fractals.data.ChunkBorderDataNullImpl;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.statistics.SummedHistogramStats;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.infra.ViewData;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskProviderAdapter;
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
	
	CategoryLogger log;
	
	List<Queue<BreadthFirstTask>> openTasks = new ArrayList<>();
	Queue<BreadthFirstTask> nextOpenTasks = new PriorityQueue<>(comparator_priority);//one entry for each pass -> 
	Queue<BreadthFirstTask> nextBufferedTasks = new PriorityQueue<>(comparator_priority);//buffer for highest priority tasks
	List<List<BreadthFirstTask>> tempList = new ArrayList<>(); //used when refreshing sorting order
	List<BreadthFirstTask> borderTasks = new ArrayList<>();
	Queue<BreadthFirstTask> newQueue = new LinkedList<>(); //for generation
	List<BreadthFirstTask> finishedTasks = new ArrayList<>();
	
	boolean updatedPredictedMidpoint = false;

	BFSystemContext context = new BFSystemContext(this);
	
	boolean done = false;
	
	Map<Integer, Map<ClientConfiguration, List<ChunkUpdateMessage>>> pendingUpdateMessages = new HashMap<>(); //TODO use! ; replace second map with Set/List of clients?
	
	double midpointChunkX;
	double midpointChunkY;

	Map<Integer, CalculateThreadReference> calculateThreadReferences = new HashMap<>(); //TODO remove?

	int id_counter_tasks = 0;
	
	SummedHistogramStats stats = new SummedHistogramStats();

	public BreadthFirstTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
		log = ((BreadthFirstSystem)system).getLogger().createSubLogger("tm");
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
		BreadthFirstTask rootTask = new BreadthFirstTask(context, id_counter_tasks++, this, chunk, context.getPos(chunkX, chunkY), 
				context.createCalculator(), context.layerConfig.getLayers().get(0), context.getViewId());
		rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, context.layerConfig.getLayers().get(0));
		context.getActiveViewData().insertBufferedChunk(chunk, true);
		openTasks.get(0).add(rootTask);
		openChunks++;
		return rootTask;
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
	public List<? extends FractalsTask> getTasks(int count) {
		List<BreadthFirstTask> tasks = new ArrayList<>();
		for (int i = 0 ; i < count ; i++) {
			synchronized (this){
				BreadthFirstTask task = null;
				//TODO remove
//				for (int try1 = 0 ; try1 < 3 ; try1++){
//					try {
						task = nextBufferedTasks.poll();
						if (task != null)
							break;
//					} catch (NullPointerException e) {
//						e.printStackTrace();
//					}
//				}
//				if (task == null)
//					break;

				
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
		finishedTasks.add((BreadthFirstTask)task);
		task.getStateInfo().setState(TaskState.FINISHED);
	}
	
	int openChunks;
	
	private boolean finishTasks() {
		if (finishedTasks.isEmpty())
			return false;
		setLifeCycleState(LifeCycleState.RUNNING);
		synchronized (this) {
			List<ClientConfiguration> clients = new ArrayList<>(system.getClients());
			for (BreadthFirstTask task : finishedTasks) {

				if (context.getViewId() != task.getJobId())
					continue;
				
				final Integer taskId = task.getId();
				
				//update stats
				stats.addHistogram(task.getTaskStats());
				//TODO distribute stat update?
				
				
				//distribute
				if (task.getStateInfo().getLayer().renderingEnabled()) {
					
					//compress
					Layer layer = task.chunk.getCurrentTask().getStateInfo().getLayer();
					int upsample = 1;
					if (layer instanceof BreadthFirstUpsampleLayer)
						upsample = ((BreadthFirstUpsampleLayer)layer).getUpsample();
					

					CompressedChunk compressedChunk = context.getActiveViewData().updateBufferedAndCompressedChunk(task.getChunk());
					
					//send update messages
					for (ClientConfiguration client : clients) {
						((ServerNetworkManager)managers.getNetworkManager()).updateChunk(client, system, compressedChunk);
					}
				}
				
				//update layer and re-add or dispose
				Layer currentLayer = task.getStateInfo().getLayer();
				int currentLayerId = currentLayer.getId();
				if (currentLayer.getId() >= context.layerConfig.getLayers().size()-1) {
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
					Layer layer = context.layerConfig.getLayers().get(currentLayerId);
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
		for (Map<ClientConfiguration, List<ChunkUpdateMessage>> map : pendingUpdateMessages.values()) {
			for (List<ChunkUpdateMessage> msgs : map.values()) {
				for (ChunkUpdateMessage msg : msgs)
					msg.setCancelled(true);
				msgs.clear();
			}
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
		stats.reset();
		BreadthFirstViewData viewData = context.getActiveViewData();
		if (viewData != null) {
//			viewData.dispose();
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
		//clear queues to update sorting
		synchronized (this) {
			List<Layer> layers = context.layerConfig.getLayers();
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
				if (context.getScreenDistance(borderTask.getChunk()) <= context.border_generation) {
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
					double screenDistance = context.getScreenDistance(task.getChunk());
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
			BreadthFirstViewData viewData = context.getActiveViewData();
			if (!viewData.hasCompressedChunk(midpointChunkXFloor, midpointChunkYFloor)){
				AbstractArrayChunk chunk = context.chunkFactory.createChunk(midpointChunkXFloor, midpointChunkYFloor);
				BreadthFirstTask rootTask = new BreadthFirstTask(context, id_counter_tasks++, this, chunk, context.getPos(midpointChunkXFloor, midpointChunkYFloor),
						context.createCalculator(), layers.get(0), context.getViewId());
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
					nextBufferedTasks.add(polledTask);
					//new chunk at layer 0 taken -> generate neighbours, fill queues
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
		double highestDistance = context.width >= context.height ? context.width : context.height;
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
			double screenDistance = context.getScreenDistance(newTask.getChunk());
			if (screenDistance > context.border_generation) {
				newTask.getStateInfo().setState(TaskState.BORDER);
				borderTasks.add(newTask);
			} else {
				openTasks.get(newTask.getStateInfo().getLayer().getId()).add(newTask);
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
				context.createCalculator(), context.layerConfig.getLayers().get(0), context.getViewId());
		task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, context.layerConfig.getLayers().get(0));
		openChunks++;
		newQueue.add(task);
		context.getActiveViewData().insertBufferedChunk(chunk, true);
		return true;
	}
}