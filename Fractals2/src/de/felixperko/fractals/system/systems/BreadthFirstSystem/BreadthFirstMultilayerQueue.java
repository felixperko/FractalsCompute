package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;

public class BreadthFirstMultilayerQueue extends AbstractBreadthFirstMultilayerQueue<BreadthFirstTask, BreadthFirstLayer> {
	

	BreadthFirstTaskManager taskManager;
	
	Queue<BreadthFirstTask> newQueue = new LinkedList<>(); //for generation
	
	double border_generation = 0;
	double border_dispose = 5;

	public BreadthFirstMultilayerQueue(BreadthFirstTaskManager taskManager, List<BreadthFirstLayer> layers,
			Comparator<BreadthFirstTask> comparator_priority, Comparator<BreadthFirstTask> comparator_distance) {
		super(layers, comparator_priority, comparator_distance);
		this.taskManager = taskManager;
	}
	
	@Override
	protected void addTaskAtMidpointIfNotExists() {
		//add task for chunk at midpoint if not calculated
		int midpointChunkXFloor = (int)taskManager.midpointChunkX;
		int midpointChunkYFloor = (int)taskManager.midpointChunkY;
		if (!taskManager.viewData.hasChunk(midpointChunkXFloor, midpointChunkYFloor)){
			AbstractArrayChunk chunk = taskManager.chunkFactory.createChunk(midpointChunkXFloor, midpointChunkYFloor);
			BreadthFirstTask rootTask = new BreadthFirstTask(taskManager.id_counter_tasks++, taskManager,
					chunk, taskManager.parameters, taskManager.getChunkPos(midpointChunkXFloor, midpointChunkYFloor), taskManager.createCalculator(), layers.get(0), taskManager.jobId);
			rootTask.updatePriorityAndDistance(taskManager.midpointChunkX, taskManager.midpointChunkY, layers.get(0));
			taskManager.viewData.addChunk(chunk);
			openTasks.get(0).add(rootTask);
			taskManager.openChunks++;
		}
	}

	@Override
	protected boolean generateNeighbourIfNotExists(BreadthFirstTask parentTask, int dx, int dy) {
		if (isDone())
			return false;
		
		Integer chunkX = parentTask.getChunk().getChunkX() + dx;
		Integer chunkY = parentTask.getChunk().getChunkY() + dy;
		
		if (taskManager.viewData.hasChunk(chunkX, chunkY)) //already exists
			return false;
		
		AbstractArrayChunk chunk = null;
		taskManager.chunkFactory.setViewData(taskManager.viewData);
		for (int try1 = 0 ; try1 < 10 ; try1++) {//TODO remove
			try {
				chunk = taskManager.chunkFactory.createChunk(chunkX, chunkY);
				break;
			} catch (Exception e) {
				if (try1 == 9 && parentTask.getJobId() == taskManager.jobId)
					throw e;
				else
					try {
						Thread.sleep(1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			}
		}
		BreadthFirstTask task = new BreadthFirstTask(taskManager.id_counter_tasks++, taskManager, chunk, taskManager.parameters, taskManager.getChunkPos(chunkX, chunkY), taskManager.createCalculator(), taskManager.layerConfig.getLayers().get(0), taskManager.jobId);
		task.updatePriorityAndDistance(taskManager.midpointChunkX, taskManager.midpointChunkY, taskManager.layerConfig.getLayers().get(0));
		taskManager.openChunks++;
		newQueue.add(task);
		taskManager.viewData.addChunk(chunk);
		return true;
	}

	protected boolean isDone() {
		return taskManager.done;
	}

	@Override
	protected void inDisposal(BreadthFirstTask task) {
		task.getStateInfo().setState(TaskState.REMOVED);
	}

	@Override
	protected void inGeneration(BreadthFirstTask task) {
		task.getStateInfo().setState(TaskState.OPEN);
	}

	@Override
	protected void inBorder(BreadthFirstTask task) {
		task.getStateInfo().setState(TaskState.BORDER);
	}

	@Override
	protected boolean isConditionDispose(BreadthFirstTask task){
		return taskManager.getScreenDistance(task.getChunk()) > border_dispose;
	}

	@Override
	protected boolean isConditionGenerate(BreadthFirstTask task){
		return taskManager.getScreenDistance(task.getChunk()) <= border_generation;
	}

	@Override
	public void generateNeighbours(BreadthFirstTask polledTask) {
			double highestDistance = taskManager.width >= taskManager.height ? taskManager.width : taskManager.height;
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
				if (!isConditionGenerate(newTask)) {
					inBorder(newTask);
					borderTasks.add(newTask);
				} else {
					openTasks.get(newTask.getLayerId()).add(newTask);
				}
			}
			newQueue.clear();
		}

	@Override
	protected void updateTaskPriority(BreadthFirstTask task, int layer) {
		task.updatePriorityAndDistance(taskManager.midpointChunkX, taskManager.midpointChunkY, layers.get(layer));
	}
	
	@Override
	public synchronized void reset() {
		super.reset();
		newQueue.clear();
	}
}
