package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

//first chunk at relative 0, 0
//generate neighbours -> add to open queue
/* N N N N N
* N N C N N
* N C 1 C N
* N N C N N
* N N N N N */
//choose next neigbour with lowest euclidian distance
//neighbour extracted -> generate neighbours that don't exist
/* N N C N N
* N C 1 C N
* C 1 1 1 C
* N C 1 C N
* N N C N N */

/* N C C C N
* C 1 1 1 C
* C 1 1 1 C
* C 1 1 1 C
* N C C C N*/
//multiple layers -> multiple search instances (collect next task for each instance in queue according to prioritization (fetch new from pass when taken)

public abstract class AbstractBreadthFirstMultilayerQueue<O extends BreadthFirstQueueEntry, L> {
	
	protected List<Queue<O>> openTasks = new ArrayList<>();
	Queue<O> nextOpenTasks;//next entry for buffer from each layer
	Queue<O> nextBufferedTasks;//buffer for highest priority tasks
	List<List<O>> tempList = new ArrayList<>(); //used when refreshing sorting order
	protected List<O> borderTasks = new ArrayList<>();
	
	Comparator<O> comparator_priority;
	Comparator<O> comparator_distance;
	
	List<L> layers = new ArrayList<>();

	int buffer = 10;
	
	public AbstractBreadthFirstMultilayerQueue(List<L> layers, Comparator<O> comparator_priority, Comparator<O> comparator_distance){
		this.layers = layers;
		this.comparator_distance = comparator_distance;
		this.comparator_priority = comparator_priority;
		
		this.nextOpenTasks = new PriorityQueue<>(comparator_priority);
		this.nextBufferedTasks = new PriorityQueue<>(comparator_priority);
		
		init();
	}
	
	public void add(O obj) {
		openTasks.get(obj.getLayerId()).add(obj);
	}
	
	public synchronized void init() {
		openTasks.clear();
		tempList.clear();
		for (int i = 0 ; i < layers.size() ; i++) {
			openTasks.add(new PriorityQueue<>(comparator_distance));
			tempList.add(new ArrayList<>());
		}
	}
	
	public void setLayers(List<L> layers) {
		this.layers = layers;
		init();
	}
	
	public List<O> poll(int count) {
		List<O> polled = new ArrayList<>();
		for (int i = 0 ; i < count ; i++) {
			O obj = null;
			//TODO remove/change try?
//			for (int try1 = 0 ; try1 < 3 ; try1++){ 
//				try {
					obj = nextBufferedTasks.poll();
//					if (obj != null)
//						break;
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//				}
//			}
//			if (obj == null)
//				break;
			
			onPoll(obj);
				
			polled.add(obj);
		}
		return polled;
	}
	
	public synchronized boolean fillQueues() {
		boolean changed = false;
//		List<BreadthFirstLayer> layers = layerConfig.getLayers();
		
		//buffer not filled -> fill if pending tasks are available
		while (nextBufferedTasks.size() < buffer) {
			boolean[] layerInNextTasks = new boolean[layers.size()]; //nextOpenTasks contains nearest chunk of each layer, track missing
			int notFilled = layers.size();
			//fill nextOpenTasks with next values from openTasks
			for (O nextOpenTask : nextOpenTasks) {
				layerInNextTasks[nextOpenTask.getLayerId()] = true;
				notFilled--;
			}
			if (notFilled > 0) { //nextOpenTasks is missing next task for at least one layer. refill if available.
				for (int l = 0 ; l < layers.size() ; l++) {
					if  (layerInNextTasks[l])
						continue;
					O task = openTasks.get(l).poll();
					if (task == null)
						continue;
					changed = true;
					nextOpenTasks.add(task);
					if (task.getLayerId() == 0)
						generateNeighbours(task);
				}
			}
			
			//cascade further if not empty
			if (!nextOpenTasks.isEmpty()) {
				O polled = nextOpenTasks.poll();
				nextBufferedTasks.add(polled);
				changed = true;
				Queue<O> queue = openTasks.get(polled.getLayerId());
				if (!queue.isEmpty()) {
					O polledTask = queue.poll();
					nextBufferedTasks.add(polledTask);
					//new chunk at layer 0 taken -> generate neighbours, fill queues
					if (polled.getLayerId() == 0)
						generateNeighbours(polledTask);
				}
			} else { //lower priority queues empty -> generate more tasks or stop
				break;
			}
		}
		return changed;
	}

	public void predictedMidpointUpdated() {
		//clear queues to update sorting
		synchronized (this) {
			for (int l = 0 ; l < layers.size() ; l++) {
				tempList.get(l).addAll(openTasks.get(l));
				openTasks.get(l).clear();
			}
			for (O task : nextOpenTasks) {
				tempList.get(task.getLayerId()).add(task);
			}
			nextOpenTasks.clear();
			for (O task : nextBufferedTasks) {
				tempList.get(task.getLayerId()).add(task);
			}
			nextBufferedTasks.clear();
			
			//add border tasks
			Iterator<O> borderIt = borderTasks.iterator();
			while (borderIt.hasNext()) {
				O borderTask = borderIt.next();
				if (isConditionGenerate(borderTask)) {
					inGeneration(borderTask);
					openTasks.get(borderTask.getLayerId()).add(borderTask);
					borderIt.remove();
				}
			}
			
			//re-add
			for (int l = 0 ; l < layers.size() ; l++) {
				for (O task : tempList.get(l)) {
					if (isConditionDispose(task)) {
						inDisposal(task);
						continue;
					}
					updateTaskPriority(task, l);
					openTasks.get(l).add(task);
				}
				tempList.get(l).clear();
			}
			
			addTaskAtMidpointIfNotExists();
		}
	}

	public synchronized void reset() {
		
		for (Queue<O> openQueue : openTasks)
			openQueue.clear();
		for (List<O> temp : tempList)
			temp.clear();
		nextOpenTasks.clear();
		nextBufferedTasks.clear();
		borderTasks.clear();
	}
	
	protected abstract void onPoll(O obj);
	
	protected abstract void updateTaskPriority(O task, int layer);
	
	protected abstract void generateNeighbours(O task);

	protected abstract boolean generateNeighbourIfNotExists(O parentTask, int dx, int dy);
	
	protected abstract void addTaskAtMidpointIfNotExists();

	protected abstract boolean isConditionDispose(O task);
	protected abstract void inDisposal(O task);

	protected abstract boolean isConditionGenerate(O task);
	protected abstract void inGeneration(O task);
	
	protected abstract void inBorder(O task);
}
