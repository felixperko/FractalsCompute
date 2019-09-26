package de.felixperko.fractals.system.systems.OrbitSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.statistics.EmptyStats;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractFractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.FractalsThread;

public class OrbitTask extends AbstractFractalsTask<OrbitTask> {

	private static final long serialVersionUID = -7778760685136366361L;
	
	ComplexNumber current;
	List<ComplexNumber> values = new ArrayList<>();
	int index;
	
	FractalsCalculator calculator;

	public transient AbstractArrayChunk chunk;
	private transient CompressedChunk compressed_chunk;

	public OrbitTask(SystemContext context, Integer id, TaskManager<OrbitTask> taskManager, int jobId, Layer layer,
			FractalsCalculator calculator, AbstractArrayChunk chunk) {
		super(context, id, taskManager, jobId, layer);
		getStateInfo().setState(TaskState.OPEN);
		this.calculator = calculator;
		this.chunk = chunk;
		chunk.setCurrentTask(this);
	}

	@Override
	public void setThread(FractalsThread thread) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FractalsCalculator getCalculator() {
		return calculator;
	}
	
	@Override
	public void setContext(SystemContext context) {
		super.setContext(context);
		this.calculator = context.createCalculator();
	}

	@Override
	public void run() throws InterruptedException {
		if (taskStats == null)
			taskStats = new EmptyStats();
		calculator.setContext(getContext());
		calculator.calculate(chunk, taskStats);
	}

	@Override
	public Double getPriority() {
		return 1d;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		Layer layer = getStateInfo().getLayer();
		compressed_chunk = new CompressedChunk((ReducedNaiveChunk) chunk);
		out.writeObject(compressed_chunk);
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		compressed_chunk = (CompressedChunk)in.readObject();
		chunk = compressed_chunk.decompress();
		
		//TODO dedicated post chunk decompression method
		chunk.setCurrentTask(this);
		
		in.defaultReadObject();
	}
}
