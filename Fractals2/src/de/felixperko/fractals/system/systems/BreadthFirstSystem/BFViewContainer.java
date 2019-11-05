package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Collection;
import java.util.LinkedList;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.AbstractViewContainer;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.ViewData;

public class BFViewContainer extends AbstractViewContainer<BreadthFirstViewData> {

	LinkedList<BreadthFirstViewData> oldViewData = new LinkedList<>();
	int oldViewBufferSize;
	
	BFSystemContext context;
	
	public BFViewContainer(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
	}
	
	@Override
	public void setActiveViewData(BreadthFirstViewData viewData) {
		BreadthFirstViewData oldViewData = this.activeViewData;
		if (viewData == oldViewData)
			return;
		storeCurrentActiveViewData();
		super.setActiveViewData(viewData);
		if (oldViewData != null)
			copyData(oldViewData, viewData);
	}
	
	
	private void copyData(BreadthFirstViewData fromViewData, BreadthFirstViewData toViewData) {
		ComplexNumber complex11 = context.getNumberFactory().createComplexNumber(1, 1);
		
		for (Chunk chunk : fromViewData.getBufferedChunks()) {
			
			if (!(chunk instanceof AbstractArrayChunk))
				throw new IllegalStateException();
			
			Number oldChunkZoom = fromViewData.paramContainer.getClientParameter("chunkzoom").getGeneral(Number.class);
			Number zoomFactorN = oldChunkZoom;
			zoomFactorN.div(context.getChunkZoom());
			double zoomFactor = zoomFactorN.toDouble();
			if (zoomFactor < 1)
				zoomFactor = 1./zoomFactor;
			if (Math.abs(zoomFactor%1) > 0.00000000001) //integer zoom factor? don't bother if not
				return;

			//check other relevant parameters
			ParamContainer oldParamContainer = fromViewData.paramContainer;
			ParamContainer tempParamContainer = new ParamContainer(context.getParamContainer(), true);
			for (String name : tempParamContainer.getClientParameters().keySet()) {
				ParamSupplier tempSupplier = tempParamContainer.getClientParameter(name);
				ParamSupplier oldSupplier = oldParamContainer.getClientParameter(name);
				if (tempSupplier.evaluateChanged(oldSupplier) && tempSupplier.isLayerRelevant())
					return;
			}
			
			//prepare data
			AbstractArrayChunk fromChunk = (AbstractArrayChunk) chunk;
			ComplexNumber chunkPos = fromChunk.chunkPos;
			ComplexNumber chunkPos2 = chunkPos.copy();
			chunkPos2.add(complex11);
			double minX = context.getChunkX(chunkPos);
			double minY = context.getChunkY(chunkPos);
			double maxX = context.getChunkX(chunkPos2);
			double maxY = context.getChunkY(chunkPos2);
			int minXi = (int)Math.floor(minX);
			int minYi = (int)Math.floor(minY);
			int maxXi = (int)Math.ceil(minX);
			int maxYi = (int)Math.ceil(minY);

			//get or generate new chunks
			AbstractArrayChunk[][] toChunks = new AbstractArrayChunk[maxXi-minXi][maxYi-minYi];
			for (int x = minXi ; x < maxXi ; x++) {
				for (int y = minYi ; y < maxYi ; y++) {
					AbstractArrayChunk currentChunk = (AbstractArrayChunk) toViewData.getBufferedChunk(x, y);
					if (currentChunk == null) {
						currentChunk = context.generateChunk(x, y, true);
					}
					toChunks[x-minXi][y-minYi] = currentChunk;
				}
			}

			int fromDim = fromChunk.getChunkDimensions();
			int fromLength = fromChunk.getArrayLength();
			int toDim = context.getChunkSize();
			int toLength = toDim^2;
			
			//apply computed values
			for (int i = 0 ; i < fromLength ; i++) {
				int samples = fromChunk.getSampleCount(i);
				if (samples == 0)
					continue;
				
				//get old grid coords
				double pXFrom = i/fromDim;
				double pYFrom = i%fromDim;
				ComplexNumber pixelGridPosOld = context.getNumberFactory()
						.createComplexNumber(fromChunk.getChunkX() + pXFrom/fromDim, fromChunk.getChunkY() + pYFrom/fromDim);
				
				//get position for grid coords
				ComplexNumber pos = pixelGridPosOld;
				fromViewData.toPos(pos, oldChunkZoom);
				
				//get new grid coords
				double newGridX = context.getChunkX(pos);
				double newGridY = context.getChunkY(pos);
				
				//get new chunk 
				int iX = (int)(newGridX-minXi);
				int iY = (int)(newGridY-minYi);
				AbstractArrayChunk toChunk = toChunks[iX][iY];
				
				//get new internal index
				int pXTo = (int)Math.round((newGridX % 1) * toDim);
				int pYTo = (int)Math.round((newGridY % 1) * toDim);
				int iTo = pXTo*toDim + pYTo;
				
				//apply properties
				//TODO other properties?
				toChunk.addSample(iTo, fromChunk.getValue(iTo, true), 1);
			}
			
			//update ViewData
			for (AbstractArrayChunk[] toChunks2 : toChunks) {
				for (AbstractArrayChunk toChunk : toChunks2) {
					toViewData.insertBufferedChunk(toChunk, true);
				}
			}
		}
	}

	public void reactivateViewData(BreadthFirstViewData oldViewData) {
		boolean removed = this.oldViewData.remove(oldViewData);
		if (!removed)
			throw new IllegalArgumentException("tried to reactivate a ViewData that isn't in the deactivated list");
		setActiveViewData(oldViewData);
	}
	
	public void setOldViewBufferSize(int oldViewBufferSize) {
		this.oldViewBufferSize = oldViewBufferSize;
		enforceBufferSize();
	}

	public Collection<BreadthFirstViewData> getInactiveViews() {
		return oldViewData;
	}

	void storeCurrentActiveViewData() {
		if (activeViewData == null)
			return;
		oldViewData.addFirst(activeViewData);
		activeViewData.clearBufferedChunks();
		enforceBufferSize();
	}

	private void enforceBufferSize() {
		while (oldViewData.size() > oldViewBufferSize)
			disposeLastOldViewData();
	}

	private void disposeLastOldViewData() {
		ViewData<?> old = oldViewData.removeLast();
		old.dispose();
	}

	public void setContext(BFSystemContext context) {
		this.context = context;
	}

}
