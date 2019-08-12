package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractFractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.FractalsThread;

public class BreadthFirstTask extends AbstractFractalsTask<BreadthFirstTask> implements BreadthFirstQueueEntry {
	
	private static final long serialVersionUID = 428442040367400862L;

	Double distance;
	Double priority;
	
	transient FractalsThread thread;
	
	int previousLayerId = -1;
	
	public transient AbstractArrayChunk chunk;
	transient CompressedChunk compressed_chunk;
	
	Map<String, ParamSupplier> parameters; //TODO transient
	
	FractalsCalculator calculator;
	
	public BreadthFirstTask(SystemContext context, int id, TaskManager taskManager, AbstractArrayChunk chunk, Map<String, ParamSupplier> taskParameters, ComplexNumber chunkPos, 
			FractalsCalculator calculator, Layer layer, int jobId) {
		super(context, id, taskManager, jobId, layer);
		getStateInfo().setState(TaskState.OPEN);
		this.calculator = calculator;
		this.chunk = chunk;
		this.chunk.chunkPos = chunkPos.copy();
		chunk.setCurrentTask(this);
			
		this.parameters = new HashMap<>();
		for (Entry<String, ParamSupplier> e : taskParameters.entrySet()){
			this.parameters.put(e.getKey(), e.getValue().copy());
		}
		this.parameters.put("chunkpos", new StaticParamSupplier("chunkpos", this.chunk.chunkPos.copy()));
		this.parameters.put("chunksize", new StaticParamSupplier("chunksize", (Integer)chunk.getChunkDimensions()));
	}
	
	@Override
	public void run() {
		preprocess();
		previousLayerId = getStateInfo().getLayerId()-1;
		parameters.put("layer", new StaticParamSupplier("layer", (Integer)getStateInfo().getLayer().getId()));
		calculator.setParams(parameters);
		calculator.calculate(chunk);
	}

	private void preprocess() {
		if (isPreviousLayerCullingEnabled()){
			preprocess_culling((BreadthFirstLayer) getContext().getLayer(previousLayerId));
		}
	}

	private void preprocess_culling(BreadthFirstLayer prev) {
		if (prev instanceof BreadthFirstUpsampleLayer) {
			BreadthFirstUpsampleLayer prevUpsampled = (BreadthFirstUpsampleLayer) prev;
			BitSet activePixels = prevUpsampled.getEnabledPixels();
			int upsample = prevUpsampled.upsample;
			int dim = chunk.getChunkDimensions();
			//for (int i = 0 ; i < chunk.getArrayLength() ; i++) {
			//}
			
			activePixelLoop:
			for (int i = activePixels.nextSetBit(0) ; i != -1 ; i = activePixels.nextSetBit(i+1)) { //loop active pixels
				int x = i / dim;
				int y = i % dim;
				boolean cull = true;
				
				evalCullingLoop:
				for (int dx = -1 ; dx <= 1 ; dx++) {
					for (int dy = -1 ; dy <= 1 ; dy++) {
						
						int neighbourUpsampleIndex = i + dx*upsample*dim + dy*upsample;
						int x2 = x+dx*upsample;
						int y2 = y+dy*upsample;

						boolean local = true;
						
						if (x2 < 0){
							local = false;
							if (chunk.getNeighbourBorderData(BorderAlignment.LEFT).isSet(y)) {
								cull = false;
								break evalCullingLoop;
							}
						} else if (x2 >= dim){
							local = false;
							if (chunk.getNeighbourBorderData(BorderAlignment.RIGHT).isSet(y)) {
								cull = false;
								break evalCullingLoop;
							}
						}
						
						if (y2 < 0){
							local = false;
							if (chunk.getNeighbourBorderData(BorderAlignment.UP).isSet(x)) {
								cull = false;
								break evalCullingLoop;
							}
						} else if (y2 >= dim){
							local = false;
							if (chunk.getNeighbourBorderData(BorderAlignment.DOWN).isSet(x)) {
								cull = false;
								break evalCullingLoop;
							}
						}
						
						if (local && chunk.getValue(neighbourUpsampleIndex, true) > 0) {
							cull = false;
							break evalCullingLoop;
						}
						
//						if (neighbourUpsampleIndex < 0){
//							if (dx == -1) {
//								if (chunk.getNeighbourBorderData(BorderAlignment.LEFT).isSet(y)) {
//									cull = false;
//									break evalCullingLoop;
//								}
//							} if (dy == -1){
//								if (chunk.getNeighbourBorderData(BorderAlignment.UP).isSet(x)) {
//									cull = false;
//									break evalCullingLoop;
//								}
//							}
//						} else if (neighbourUpsampleIndex >= chunk.getArrayLength()) {
//							if (dx == 1) {
//								if (chunk.getNeighbourBorderData(BorderAlignment.RIGHT).isSet(y)) {
//									cull = false;
//									break evalCullingLoop;
//								}
//							} if (dy == 1){
//								if (chunk.getNeighbourBorderData(BorderAlignment.DOWN).isSet(x)) {
//									cull = false;
//									break evalCullingLoop;
//								}
//							}
//						} else {
//							if (chunk.getValue(neighbourUpsampleIndex, true) > 0) {
//								cull = false;
//								break evalCullingLoop;
//							}
//						}
					}
				}
				
				chunk.setCullFlags(i, upsample, cull);
			}
		}
	}

	public void updateDistance(double chunkX, double chunkY) {
		distance = getChunk().distance(chunkX, chunkY);
	}

	public Double getDistance() {
		return distance;
	}

	public Double getPriority() {
		return priority;
	}
	
	public int getPreviousLayerId() {
		return previousLayerId;
	}
	
	public boolean isPreviousLayerCullingEnabled() {
		if (previousLayerId == -1)
			return false;
		Layer layer = getContext().getLayer(previousLayerId);
		return layer.cullingEnabled();
	}
	
	@Override
	public int getLayerId() {
		return getStateInfo().getLayer().getId();
	}
	
	@Override
	public void updatePriorityAndDistance(double midpointChunkX, double midpointChunkY, BreadthFirstLayer layer) {
		updateDistance(midpointChunkX, midpointChunkX);
		priority = distance * layer.getPriorityMultiplier() + layer.getPriorityShift();
	}
	
	public Chunk getChunk() {
		return chunk;
	}

	@Override
	public void setThread(FractalsThread thread) {
		this.thread = thread;
	}

	@Override
	public FractalsCalculator getCalculator() {
		return calculator;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		Layer layer = getStateInfo().getLayer();
		int upsample = layer instanceof BreadthFirstUpsampleLayer ? ((BreadthFirstUpsampleLayer)layer).getUpsample() : 1;
		compressed_chunk = new CompressedChunk((ReducedNaiveChunk) chunk, upsample, this, priority, true);
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
