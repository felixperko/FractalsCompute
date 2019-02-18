package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerManagers;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
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
	
	List<Queue<BasicTask>> openTasks = new ArrayList<>();
	
	Queue<BasicTask> nextOpenTasks = new LinkedList<>();//one entry for each pass -> 
	
	Queue<BasicTask> nextBufferedTasks = new LinkedList<>();
	
	
	
//	Map<ComplexNumber, List<BreadthFirstViewData>> layerDataMap = new HashMap<>();
	//TODO layers as separate systems? probably.

	public BreadthFirstTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
	}

	@Override
	public void startTasks() {
		BreadthFirstTask firstTask = generateTask(0,0);
		passesNextTasks.add(firstTask);
		
		Queue<BreadthFirstTask> firstPassOpenTasks = new LinkedList<>();
		openTasks.add(firstPassOpenTasks);
		generateNeighbours(firstTask, firstPassOpenTasks);
		//add task at relative (0, 0)
		//generate neighbours
	}

	@Override
	public void endTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
		// TODO Auto-generated method stub
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
