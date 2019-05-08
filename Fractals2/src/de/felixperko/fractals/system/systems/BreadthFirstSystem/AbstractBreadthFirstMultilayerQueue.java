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
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.TaskProviderAdapter;

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
		
		this.layers = layers;
		this.nextOpenTasks = new PriorityQueue<>(comparator_priority);
		this.nextBufferedTasks = new PriorityQueue<>(comparator_priority);
		
		init();
	}
	
	public synchronized void init() {
		for (int i = 0 ; i < layers.size() ; i++) {
			openTasks.add(new PriorityQueue<>(comparator_distance));
			tempList.add(new ArrayList<>());
		}
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
//			List<BreadthFirstLayer> layers = layerConfig.getLayers();
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
//			boolean addedMidpoint = false;
			for (int l = 0 ; l < layers.size() ; l++) {
				for (O task : tempList.get(l)) {
//					if (task.getChunk().getChunkX() == (long)midpointChunkX && task.getChunk().getChunkY() == (long)midpointChunkY)
//						addedMidpoint = true;
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
