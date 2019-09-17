package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ClientConnection;
import de.felixperko.fractals.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.TaskProviderAdapter;

public class OrbitTaskManager extends AbstractTaskManager<OrbitTask> {
	
	Map<ClientConfiguration, Integer> sentIndices = new HashMap<>();
	
	OrbitTask task;
	
	SystemContext context;

	public OrbitTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
		context = new OrbitContext();
		task = new OrbitTask(context, 0, this, context.getViewId(), context.getLayer(0));
	}

	@Override
	public void startTasks() {
		task.getStateInfo().setState(TaskState.OPEN);
	}

	@Override
	public void endTasks() {
		task.getStateInfo().setState(TaskState.FINISHED);
	}

	@Override
	public boolean setParameters(ParamContainer paramContainer) {
		boolean reset = context.setParameters(paramContainer);
		return reset;
	}

	@Override
	public void reset() {
		task = new OrbitTask(context, 0, this, context.getViewId(), context.getLayer(0));
		sentIndices.clear();
	}

	@Override
	public void taskFinished(FractalsTask task) {
		
		int itDone = ((OrbitTask)task).index-1;
		
		//TODO prepare chunk (or alternative)
		CompressedChunk compressedChunk = null;
		
		for (Entry<ClientConfiguration, Integer> e : sentIndices.entrySet()){
			
			ClientConfiguration clientConf = e.getKey();
			ClientConnection conn = clientConf.getConnection();
			Integer it = e.getValue();
			
			if (itDone > it) {
				conn.writeMessage(new ChunkUpdateMessage(conn, system.getId(), compressedChunk, null));
			}
		}
		
		//TODO reassign task
	}

	@Override
	public List<? extends FractalsTask> getTasks(int count) {
		if (task.getState() != TaskState.OPEN)
			return null;
		List<OrbitTask> list = new ArrayList();
		list.add(task);
		return list;
	}

}
