package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout.Alignment;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.BorderAlignment;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.ChunkBorderData;
import de.felixperko.fractals.data.ChunkBorderDataNullImpl;
import de.felixperko.fractals.data.CompressedChunk;
import de.felixperko.fractals.data.ReducedNaiveChunk;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.statistics.EmptyStats;
import de.felixperko.fractals.system.statistics.HistogramStats;
import de.felixperko.fractals.system.statistics.IHistogramStats;
import de.felixperko.fractals.system.statistics.IStats;
import de.felixperko.fractals.system.statistics.SummedHistogramStats;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.task.AbstractFractalsTask;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;
import de.felixperko.fractals.system.thread.FractalsThread;

public class BreadthFirstTask extends AbstractFractalsTask<BreadthFirstTask> implements BreadthFirstQueueEntry {
	
	private static final long serialVersionUID = 428442040367400862L;

	double distance;
	double priority;
	
	transient FractalsThread thread;
	
	public transient AbstractArrayChunk chunk;
	private transient CompressedChunk compressed_chunk;
	
	transient FractalsCalculator calculator;
	
	Map<Integer, HistogramStats> layerTaskStats = new HashMap<>(); //key = layerId
	
	
	public BreadthFirstTask(SystemContext context, int id, TaskManager taskManager, AbstractArrayChunk chunk, ComplexNumber chunkPos, 
			FractalsCalculator calculator, Layer layer, int jobId) {
		super(context, id, taskManager, jobId, layer);
		getStateInfo().setState(TaskState.OPEN);
		this.calculator = calculator;
		this.chunk = chunk;
		this.chunk.chunkPos = chunkPos.copy();
		chunk.setCurrentTask(this);
	}
	
	@Override
	public void run() {
		try {
			int layerId = getStateInfo().getLayerId();
			taskStats = new HistogramStats(1000, ((BFSystemContext)getContext()).getParamValue("iterations", Integer.class));
			
			taskStats.executionStart();
			
			preprocess();
			
			Layer layer = getStateInfo().getLayer();
			chunk.setUpsample(layer.getUpsample());
			
			calculator.setContext(getContext());
			calculator.calculate(chunk, taskStats);
			if (calculator.isCancelled()) {
				getStateInfo().setState(TaskState.BORDER);
				setCancelled(true);	
			}
			
			taskStats.executionEnd();
			layerTaskStats.put(layerId, (HistogramStats)taskStats);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void preprocess() {
		if (isPreviousLayerCullingEnabled()){
			preprocess_culling((BreadthFirstLayer) getContext().getLayer(getStateInfo().getLayerId()-1));
		}
	}

	private void preprocess_culling(BreadthFirstLayer prev) {
		int dim = chunk.getChunkDimensions();
		int prevUpsample = prev.getUpsample();
		if (prev instanceof BreadthFirstUpsampleLayer && prev.cullingEnabled()) {
			BreadthFirstUpsampleLayer prevUpsampled = (BreadthFirstUpsampleLayer) prev;
			BitSet activePixels = prevUpsampled.getEnabledPixels();
			int currentUpsample = getStateInfo().getLayer().getUpsample();
			//for (int i = 0 ; i < chunk.getArrayLength() ; i++) {
			//}
			
			activePixelLoop:
			for (int i = activePixels.nextSetBit(0) ; i != -1 ; i = activePixels.nextSetBit(i+1)) { //loop active pixels
				int x = i / dim;
				int y = i % dim;
				
				boolean cull = true;
				boolean oldCull = chunk.getValue(i, true) == AbstractArrayChunk.FLAG_CULL;
				
				evalCullingLoop:
				for (int dx = -1 ; dx <= 1 ; dx++) {
					for (int dy = -1 ; dy <= 1 ; dy++) {
						
						int neighbourUpsampleIndex = i + dx*prevUpsample*dim + dy*prevUpsample;
						int x2 = x+dx*prevUpsample;
						int y2 = y+dy*prevUpsample;

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
				
				if (oldCull != cull)
					chunk.setCullFlags(i, prevUpsample, cull);
			}
		}
//		else {
//			for (BorderAlignment alignment : BorderAlignment.values()){
//				ChunkBorderData neighbourData = chunk.getNeighbourBorderData(alignment);
//				if (neighbourData instanceof ChunkBorderDataNullImpl)
//					continue;
//				for (int i = 0 ; i < dim ; i++){
//					if (!neighbourData.isSet(i))
//						continue;
//					int x = 0;
//					int y = 0;
//					switch (alignment)
//					{
//					case UP:
//						x = i;
//						y = 0;
//						break;
//					case DOWN:
//						x = i;
//						y = dim-1;
//						break;
//					case LEFT:
//						x = 0;
//						y = i;
//						break;
//					case RIGHT:
//						x = dim-1;
//						y = i;
//						break;
//					}
//					int index = x*dim + y;
//					boolean oldCull = chunk.getValue(index, true) == AbstractArrayChunk.FLAG_CULL;
//					if (oldCull)
//						chunk.setCullFlags(index, 1, false);
//				}
//			}
//		}
	}
	
	@Override
	public HistogramStats getTaskStats() {
		if (layerTaskStats.size() > 0)
			return new SummedHistogramStats(new ArrayList<IHistogramStats>(layerTaskStats.values()));
		return null;
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
	
	public boolean isPreviousLayerCullingEnabled() {
		int previousLayerId = getStateInfo().getLayerId()-1;
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
	public void updatePriorityAndDistance(double midpointChunkX, double midpointChunkY, Layer layer) {
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
	
	@Override
	public void setContext(SystemContext context) {
		super.setContext(context);
		this.calculator = context.createCalculator();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		Layer layer = getStateInfo().getLayer();
		int upsample = layer instanceof BreadthFirstUpsampleLayer ? ((BreadthFirstUpsampleLayer)layer).getUpsample() : 1;
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
