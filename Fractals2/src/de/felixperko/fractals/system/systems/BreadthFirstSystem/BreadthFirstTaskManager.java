package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.parameters.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicSystem;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTask;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTaskManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.Layer;
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
//neigbour extracted -> generate neighbours that don't exist

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
	
	int buffer = 5;
	double generationBorder = 0;
	double removeBorder = 5;
	
	List<Queue<BreadthFirstTask>> openTasks = new ArrayList<>();
	Queue<BreadthFirstTask> nextOpenTasks = new PriorityQueue<>(comparator_priority);//one entry for each pass -> 
	Queue<BreadthFirstTask> nextBufferedTasks = new PriorityQueue<>(comparator_priority);//buffer for highest priority tasks
	
	Queue<BreadthFirstTask> newQueue = new LinkedList<>(); //for generation

	List<BreadthFirstTask> borderTasks = new ArrayList<>();
	
	List<BreadthFirstLayer> layers = new ArrayList<>(); //TODO fill from parameters
	
	boolean done = false;
	
	BreadthFirstViewData viewData;
	
	
	Class<? extends FractalsCalculator> calculatorClass;
	int chunkSize;
	
	List<BreadthFirstTask> finishedTasks = new ArrayList<>();
	
	CategoryLogger log;

	public BreadthFirstTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
		log = ((BreadthFirstSystem)system).getLogger().createSubLogger("tm");
	}
	
	double midpointChunkX;
	double midpointChunkY;
	
	
	NumberFactory numberFactory;
	ComplexNumber midpoint;
	ComplexNumber leftLowerCorner;
	ComplexNumber rightUpperCorner;
	double leftLowerCornerChunkX, leftLowerCornerChunkY;
	double rightUpperCornerChunkX, rightUpperCornerChunkY;
	Number zoom;
	Number chunkZoom;
	ComplexNumber relativeStartShift;
	
	Map<String, ParamSupplier> parameters;

	int id_counter_tasks = 0;
	
	int width, height;
	int chunksWidth, chunksHeight;

	@Override
	public void startTasks() {
		if (layers.isEmpty()) {
			layers.add(new BreadthFirstUpsampleLayer(0, 2, chunkSize));
			layers.add(new BreadthFirstLayer(1).with_priority_shift(5).with_priority_multiplier(2));
			layers.add(new BreadthFirstLayer(2).with_priority_shift(10).with_priority_multiplier(3).with_samples(5));
		}
		
		openTasks.clear();
		tempList.clear();
		for (int i = 0 ; i < layers.size() ; i++) {
			openTasks.add(new PriorityQueue<>(comparator_distance));
			tempList.add(new ArrayList<>());
		}

		generateRootTask();
//		nextOpenTasks.add(rootTask);
//		
//		generateNeighbours(rootTask);
	}
	
	private void generateRootTask() {
		Chunk chunk = new Chunk(0, 0, chunkSize);
//		ComplexNumber pos = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
//		pos.multValues(relativeStartShift);
//		pos.add(midpoint);
		BreadthFirstTask rootTask = new BreadthFirstTask(id_counter_tasks++, this, chunk, parameters, getChunkPos(0, 0), createCalculator(), layers.get(0));
		rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(0));
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
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public boolean tick() {
		fillQueues();
		finishTasks();
		return true;
	}

	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
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
		this.parameters = params;
		calculatorClass = availableCalculators.get(((String)params.get("calculator").get(0, 0)));
		if (calculatorClass == null)
			throw new IllegalStateException("Couldn't find calculator for name: "+params.get("calculator").get(0, 0).toString());
		chunkSize = parameters.get("chunkSize").getGeneral(Integer.class);
		midpoint = parameters.get("midpoint").getGeneral(ComplexNumber.class);
		zoom = parameters.get("zoom").getGeneral(Number.class);
		
		numberFactory = parameters.get("numberFactory").getGeneral(NumberFactory.class);
		
		width = parameters.get("width").getGeneral(Integer.class);
		height = parameters.get("height").getGeneral(Integer.class);
		
		chunksWidth = (int)Math.ceil(width/(double)chunkSize);
		chunksHeight = (int)Math.ceil(height/(double)chunkSize);
		relativeStartShift = numberFactory.createComplexNumber((chunksWidth%2 == 0 ? -0.5 : 0), chunksHeight%2 == 0 ? -0.5 : 0);
		
		Number pixelzoom = numberFactory.createNumber(width >= height ? 1./height : 1./width);
		pixelzoom.mult(zoom);
		params.put("pixelzoom", new StaticParamSupplier("pixelzoom", pixelzoom));
		chunkZoom = pixelzoom.copy();
		chunkZoom.mult(numberFactory.createNumber(chunkSize));
		
		Number rX = numberFactory.createNumber(width/2.);
		rX.mult(zoom);
		Number rY = numberFactory.createNumber(height/2.);
		rY.mult(zoom);
		ComplexNumber sideDist = numberFactory.createComplexNumber(rX, rY);

		
		ComplexNumber anchor = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
		anchor.multNumber(numberFactory.createNumber(-0.5));
		anchor.add(midpoint);
		
		if (viewData == null)
			viewData = new BreadthFirstViewData(anchor);

		leftLowerCorner = midpoint.copy();
		leftLowerCorner.sub(sideDist);
		leftLowerCornerChunkX = getChunkX(leftLowerCorner);
		leftLowerCornerChunkY = getChunkY(leftLowerCorner);
		
		rightUpperCorner = sideDist;
		rightUpperCorner.add(midpoint);
		rightUpperCornerChunkX = getChunkX(rightUpperCorner);
		rightUpperCornerChunkY = getChunkY(rightUpperCorner);
		
		if (reset)
			reset();
		
		if (params.get("midpoint").isChanged() || params.get("width").isChanged() || params.get("height").isChanged()) {
			updatePredictedMidpoint();
			if (!reset)
				predictedMidpointUpdated();
		}
		
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
	
	private void finishTasks() {
		if (finishedTasks.isEmpty())
			return;
		setLifeCycleState(LifeCycleState.RUNNING);
		synchronized (this) {
			for (BreadthFirstTask task : finishedTasks) {
//				log.log("task finished "+task.getId());
//				for (TaskState state : TaskState.values())
//					log.log(state.name()+": "+system.getSystemStateInfo().getTaskListForState(state).size());
				List<ClientConfiguration> clients = ((BreadthFirstSystem)system).getClients();
//				log.log("update chunk for "+clients.size()+" clients");
				for (ClientConfiguration client : clients) {
					((ServerNetworkManager)managers.getNetworkManager()).updateChunk(client, system, task.chunk);
				}
				Layer currentLayer = task.getStateInfo().getLayer();
				int currentLayerId = currentLayer.getId();
				if (currentLayer.getId() >= layers.size()-1) {
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
					task.getStateInfo().setLayer(layers.get(currentLayerId));
					openTasks.get(currentLayerId).add(task);
					task.getStateInfo().setState(TaskState.OPEN);
				}
			}
			finishedTasks.clear();
		}
	}

	@Override
	public void endTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void reset() {
		openTasks.clear();
		nextOpenTasks.clear();
		nextBufferedTasks.clear();
		finishedTasks.clear();
		borderTasks.clear();
		newQueue.clear();
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
				if (getScreenDistance(borderTask.getChunk()) <= generationBorder) {
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
					if (getScreenDistance(task.getChunk()) > removeBorder) {
						task.getStateInfo().setState(TaskState.REMOVED);
						continue;
					}
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(l));
					openTasks.get(l).add(task);
				}
				tempList.get(l).clear();
			}
			
			//add task for chunk at midpoint if not calculated
			long midpointChunkXFloor = (long)midpointChunkX;
			long midpointChunkYFloor = (long)midpointChunkY;
			if (!viewData.hasChunk(midpointChunkXFloor, midpointChunkYFloor)){
				Chunk chunk = new Chunk(midpointChunkXFloor, midpointChunkYFloor, chunkSize);
				BreadthFirstTask rootTask = new BreadthFirstTask(id_counter_tasks++, this,
						chunk, parameters, getChunkPos(midpointChunkXFloor, midpointChunkYFloor), createCalculator(), layers.get(0));
				rootTask.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(0));
				viewData.addChunk(chunk);
				openTasks.get(0).add(rootTask);
				openChunks++;
			}
			
			//re-fill
			fillQueues();//TODO sync?
		}
		
	}
	
	private void fillQueues() {
		while (nextBufferedTasks.size() < buffer) {
			//fill nextOpenTasks with next values from openTasks
			if (nextOpenTasks.isEmpty()) {
				for (int i = 0 ; i < openTasks.size() ; i++) {
					BreadthFirstTask task = openTasks.get(i).poll();
					if (task != null) {
						nextOpenTasks.add(task);
						if (task.getStateInfo().getLayer().getId() == 0)
							generateNeighbours(task);
					}
				}
			}
			//cascade further if not empty
			if (!nextOpenTasks.isEmpty()) {
				BreadthFirstTask polled = nextOpenTasks.poll();
				nextBufferedTasks.add(polled);
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
			if (screenDistance > generationBorder) {
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
		
		long chunkX = parentTask.getChunk().getChunkX() + dx;
		long chunkY = parentTask.getChunk().getChunkY() + dy;
		
		if (viewData.hasChunk(chunkX, chunkY)) //already exists
			return false;
		
		Chunk chunk = new Chunk(chunkX, chunkY, chunkSize);
		BreadthFirstTask task = new BreadthFirstTask(id_counter_tasks++, this, chunk, parameters, getChunkPos(chunkX, chunkY), createCalculator(), layers.get(0));
		task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(0));
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
		value.div(zoom);
		return value.toDouble()/chunkSize;
	}
	
	private double getChunkY(ComplexNumber pos) {
		Number value = pos.getImag();
		value.sub(viewData.anchor.getImag());
		value.div(zoom);
		return value.toDouble()/chunkSize;
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
