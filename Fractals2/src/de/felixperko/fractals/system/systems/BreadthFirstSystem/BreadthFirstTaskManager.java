package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.ParamSupplier;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTask;
import de.felixperko.fractals.system.systems.BasicSystem.BasicTaskManager;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;

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
	int buffer = 5;
	
	Comparator<BreadthFirstTask> comparator_distance = new Comparator<BreadthFirstTask>() {
		@Override
		public int compare(BreadthFirstTask arg0, BreadthFirstTask arg1) {
			return arg0.getDistance().compareTo(arg1.getDistance());
		}
	};

	Comparator<BreadthFirstTask> comparator_priority = new Comparator<BreadthFirstTask>() {
		@Override
		public int compare(BreadthFirstTask arg0, BreadthFirstTask arg1) {
			return arg0.getPriority().compareTo(arg1.getPriority());
		}
	};
	
	List<Queue<BreadthFirstTask>> openTasks = new ArrayList<>();
	
	Queue<BreadthFirstTask> nextOpenTasks = new PriorityQueue<>(comparator_priority);//one entry for each pass -> 
	
	Queue<BreadthFirstTask> nextBufferedTasks = new PriorityQueue<>(comparator_priority);//buffer for highest priority tasks
	

	Queue<BreadthFirstTask> newQueue = new LinkedList<>(); //for generation
	
	List<BreadthFirstLayer> layers = new ArrayList<>(); //TODO fill from parameters
	
	boolean done = false;
	
	BreadthFirstViewData viewData;
	
	//TODO update from parameters
	ComplexNumber midpoint;
	ComplexNumber leftLowerCorner;
	ComplexNumber rightUpperCorner;
	
	FractalsCalculator calculator;
	int chunkSize;

	public BreadthFirstTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
	}
	
	double midpointChunkX;
	double midpointChunkY;
	
	Number chunkZoom;
	
	Map<String, ParamSupplier> parameters;
	
	public void updatePredictedMidpoint() {
		ComplexNumber delta = midpoint.copy();
		delta.sub(viewData.anchor);
		delta.divNumber(chunkZoom);
		midpointChunkX = delta.realDouble();
		midpointChunkY = delta.imagDouble();
	}

	@Override
	public void startTasks() {
		for (int i = 0 ; i < layers.size() ; i++) {
			openTasks.add(new PriorityQueue<>(comparator_distance));
			tempList.add(new ArrayList<>());
		}
		
		BreadthFirstTask takenTask = generateTask(0, 0, 0);
		nextOpenTasks.add(takenTask);
		
		generateNeighbours(takenTask);
		
		fillQueues();
			
			//fill queues with higher priority
			
//		}
		
//		Queue<BreadthFirstTask> firstPassOpenTasks = new LinkedList<>();
//		openTasks.add(firstPassOpenTasks);
//		generateNeighbours(firstTask, firstPassOpenTasks);
		//add task at relative (0, 0)
		//generate neighbours
	}
	
	List<List<BreadthFirstTask>> tempList = new ArrayList<>();

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
				tempList.get(task.layer).add(task);
			}
			nextOpenTasks.clear();
			for (BreadthFirstTask task : nextBufferedTasks) {
				tempList.get(task.layer).add(task);
			}
			nextBufferedTasks.clear();
			
			//re-add
			for (int l = 0 ; l < layers.size() ; l++) {
				for (BreadthFirstTask task : tempList.get(l)) {
					task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(l).priority_multiplier);
					openTasks.get(l).add(task);
				}
				tempList.get(l).clear();
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
					if (task != null)
						nextOpenTasks.add(task);
				}
			}
			//cascade further if not empty
			if (!nextOpenTasks.isEmpty()) {
				BreadthFirstTask polled = nextOpenTasks.poll();
				nextBufferedTasks.add(polled);
				Queue<BreadthFirstTask> queue = openTasks.get(polled.layer);
				if (!queue.isEmpty()) {
					BreadthFirstTask polledTask = queue.poll();
					nextBufferedTasks.add(polledTask);
					//new chunk at layer 0 taken -> generate neighbours -> fill queues
					if (polled.layer == 0)
						generateNeighbours(polledTask);
				}
			} else { //lower priority queues empty -> generate more tasks or stop
				break;
			}
		}
	}

	private void generateNeighbours(BreadthFirstTask polledTask) {
		generateNeighbourIfNotExists(0, polledTask, 1, 0);
		generateNeighbourIfNotExists(0, polledTask, -1, 0);
		generateNeighbourIfNotExists(0, polledTask, 0, 1);
		generateNeighbourIfNotExists(0, polledTask, 0, -1);
		//add new neigbours to storing queue
		for (BreadthFirstTask newTask : newQueue) {
			openTasks.get(newTask.layer).add(newTask);//TODO generate queues for layers
		}
	}

	int id_counter_tasks = 0;

	private boolean generateNeighbourIfNotExists(int layer, BreadthFirstTask parentTask, int dx, int dy) {
		if (done)
			return false;
		
		long chunkX = parentTask.getChunk().getChunkX() + dx;
		long chunkY = parentTask.getChunk().getChunkY() + dy;
		
		if (viewData.hasChunk(chunkX, chunkY)) //already exists
			return false;
		
		Chunk chunk = new Chunk(chunkX, chunkY, chunkSize);
		BreadthFirstTask task = new BreadthFirstTask(id_counter_tasks++, this, chunk, parameters, chunkPos, calculator, layer);
		task.updatePriorityAndDistance(midpointChunkX, midpointChunkY, layers.get(layer).priority_multiplier);
		newQueue.add(task);
		return true;
	}
	

	@Override
	public void endTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
		this.parameters = params;
		calculator = parameters.get("calculator").getGeneral(FractalsCalculator.class);
		chunkSize = parameters.get("chunkSize").getGeneral(Integer.class);
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskFinished(BreadthFirstTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends FractalsTask> getTasks(int count) {
		// TODO Auto-generated method stub
		return null;
	}

}
