package de.felixperko.fractals.system.systems.BasicSystem;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.NaiveChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.manager.server.ServerNetworkManager;
import de.felixperko.fractals.network.ClientConfiguration;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.numbers.impl.DoubleComplexNumber;
import de.felixperko.fractals.system.numbers.impl.DoubleNumber;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstLayer;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstUpsampleLayer;
import de.felixperko.fractals.system.systems.infra.CalcSystem;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.task.AbstractTaskManager;
import de.felixperko.fractals.system.task.FractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskProviderAdapter;

/**
 * First, naive implementation. Likely broken.
 */
@Deprecated
public class BasicTaskManager extends AbstractTaskManager<BasicTask>{
	
	static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}
	
	public BasicTaskManager(ServerManagers managers, CalcSystem system) {
		super(managers, system);
	}

	BufferedImage testImage;

	ArrayChunkFactory chunkFactory;
	NumberFactory numberFactory = new NumberFactory(DoubleNumber.class, DoubleComplexNumber.class);
	
	Chunk[][] chunks;
	List<BasicTask> openTasks = new ArrayList<>();
	
	ComplexNumber midpoint;
	Number zoom;
	int width;
	int height;
	
	List<BasicTask> finishedTasks = new ArrayList<>();
	
	Map<String, ParamSupplier> currentParameters = null;
	
	boolean calculate = false;
	
	int openChunks = 0;
	
	int totalChunkCount = 0;
	
	long startTime = 0;
	
	Class<? extends FractalsCalculator> calculatorClass = null;
	
	int jobId = 0;
	
	@Override
	public void run() {
		while (getLifeCycleState() != LifeCycleState.STOPPED) {
			while (getLifeCycleState() == LifeCycleState.PAUSED || !calculate) {//calculate = false -> pause()
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
	
	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
		try {
			midpoint = (ComplexNumber) params.get("midpoint").get(0,0);
			zoom = (Number) params.get("zoom").get(0,0);
			width = (Integer) params.get("width").get(0,0);
			height = (Integer) params.get("height").get(0,0);
			calculatorClass = availableCalculators.get(((String)params.get("calculator").get(0, 0)));
			if (calculatorClass == null)
				throw new IllegalStateException("Couldn't find calculator for name: "+params.get("calculator").get(0, 0).toString());
			chunkFactory = new ArrayChunkFactory(NaiveChunk.class, (int)params.get("chunkSize").get(0, 0));
			
			Number pixelzoom = numberFactory.createNumber(1./width);
			pixelzoom.mult(zoom);
			params.put("pixelzoom", new StaticParamSupplier("pixelzoom", pixelzoom));
			
			currentParameters = params;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		testImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		return true;
	}

	@Override
	public void startTasks() {
		int chunkSize = chunkFactory.getChunkSize();
		int dimX = width/chunkSize;
		int dimY = height/chunkSize;
		chunks = new Chunk[dimX][dimY];
		openChunks = dimX*dimY;
		totalChunkCount = openChunks;
		startTime = System.nanoTime();
		int id = 0;
		FractalsCalculator calculator = createCalculator();
		Layer layer = new BreadthFirstLayer();
		for (int x = 0 ; x < dimX ; x++) {
			for (int y = 0 ; y < dimY ; y++) {
				AbstractArrayChunk chunk = chunkFactory.createChunk(x, y);
				chunks[x][y] = chunk;
				synchronized(this) {
					ComplexNumber chunkPos = numberFactory.createComplexNumber(x/(double)dimX-0.5, y/(double)dimX-0.5);
					chunkPos.multNumber(zoom);
					chunkPos.add(midpoint);
					openTasks.add(new BasicTask(id, this, chunk, currentParameters, chunkPos, createCalculator(), layer, jobId));
					calculate = true;
					id++;
				}
			}
		}
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
	public synchronized List<BasicTask> getTasks(int count) {
		int size = openTasks.size();
		if (size == 0)
			return null;
		if (count > size)
			count = size;
		List<BasicTask> tasks = new ArrayList<>();
		for (int i = 0 ; i < count ; i++)
			tasks.add(openTasks.get(i));
		if (count < size)
			openTasks.removeAll(tasks);
		else
			openTasks.clear();
		return tasks;
	}

	@Override
	public synchronized void taskFinished(FractalsTask task) {
		finishedTasks.add((BasicTask)task);
		System.out.println("task finished "+task.getId()+"/"+totalChunkCount);
	}
	
	public boolean tick() {
		if (finishedTasks.isEmpty())
			return false;
		setLifeCycleState(LifeCycleState.RUNNING);
		synchronized(this) {
			for (BasicTask task : finishedTasks) {
				
				//compress
				Layer layer = task.chunk.getCurrentTask().getStateInfo().getLayer();
				int upsample = 1;
				if (layer instanceof BreadthFirstUpsampleLayer)
					upsample = ((BreadthFirstUpsampleLayer)layer).getUpsample();
				CompressedChunk compressedChunk = new CompressedChunk((ReducedNaiveChunk) task.chunk, upsample, task, 2, true);
				
				for (ClientConfiguration client : ((BasicSystem)system).getClients()) {
					((ServerNetworkManager)managers.getNetworkManager()).updateChunk(client, system, compressedChunk);
				}
				openChunks--;
				if (openChunks == 0) { //finished
					system.stop();
				}
			}
			finishedTasks.clear();
		}
		return true;
	}

	@Override
	public void endTasks() {
	}

	@Override
	public void reset() {
	}

	@Override
	public void addTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter) {
	}

	@Override
	public void removeTaskProviderAdapter(TaskProviderAdapter taskProviderAdapter) {
	}

	
}
