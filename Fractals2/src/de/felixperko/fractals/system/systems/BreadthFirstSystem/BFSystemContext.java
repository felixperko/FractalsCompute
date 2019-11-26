package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.AbstractSystemContext;
import de.felixperko.fractals.system.LayerConfiguration;
import de.felixperko.fractals.system.ZoomableSystemContext;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.TricornCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.DrawRegion;
import de.felixperko.fractals.system.task.TaskManager;

public class BFSystemContext extends AbstractSystemContext<BreadthFirstViewData, BFViewContainer> implements ZoomableSystemContext<BFViewContainer> {
	
	private static final long serialVersionUID = -6082120140942989559L;

	protected transient static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("TricornCalculator", TricornCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}
	
	public transient Number zoom;
	public transient Number chunkZoom;
	
	public transient Integer width;
	public transient Integer height;
	
	public transient int buffer = 10;
	public transient double border_generation = 0d;
	public transient double border_dispose = 5d;
	
	public transient int chunksWidth;
	public transient int chunksHeight;
	
	public transient ComplexNumber relativeStartShift;

	
	transient BFScreenDrawRegion drawRegion;
	
	private transient Number pixelzoom;
	
	transient Logger log = LoggerFactory.getLogger(BFSystemContext.class);
	
	public BFSystemContext(TaskManager<?> taskManager) {
		super(taskManager, new BFViewContainer(0));
		viewContainer.setContext(this);
		if (taskManager != null)
			systemStateInfo = taskManager.getSystem().getSystemStateInfo();
	}
	
	public AbstractArrayChunk generateChunk(long chunkX, long chunkY, boolean generateTaskIfLocal) {
		if (generateTaskIfLocal && taskManager != null && taskManager instanceof BreadthFirstTaskManager) {
			BreadthFirstTask task = ((BreadthFirstTaskManager)taskManager).generateTask(chunkX, chunkY);
			return (AbstractArrayChunk) task.getChunk();
		} else {
			AbstractArrayChunk chunk = chunkFactory.createChunk(chunkX, chunkY);
			getActiveViewData().insertBufferedChunk(chunk, true);
			return chunk;
		}
	}
	
	@Override
	public boolean setParameters(ParamContainer paramContainer) {
		
		boolean reset = this.paramContainer != null && needsReset(paramContainer.getClientParameters(), this.paramContainer.getClientParameters());
		
		//if params aren't applicable to current ViewData, search inactive ViewDatas
		if (reset) {
			Collection<BreadthFirstViewData> oldViews = viewContainer.getInactiveViews();
			if (!oldViews.isEmpty()) {
				ParamContainer temp = new ParamContainer(new HashMap<>(paramContainer.getClientParameters()));
				for (BreadthFirstViewData oldViewData : oldViews) {
					ParamContainer oldParams = oldViewData.getParams();
					if (oldParams != null){ //TODO should these be buffered at all? shouldn't contain chunks anyways
						if (!needsReset(temp.getClientParameters(), oldParams.getClientParameters())) {
							reset = false;
							viewContainer.reactivateViewData(oldViewData);
							this.paramContainer = oldParams; //TODO correct?
							break;
						}
					}
				}
			}
		}
		
		Map<String, ParamSupplier> oldParams = this.paramContainer != null ? this.paramContainer.getClientParameters() : null;
		
		synchronized (this) {
			this.paramContainer = paramContainer;
			Map<String, ParamSupplier> parameters = paramContainer.getClientParameters();
			
			calculatorClass = availableCalculators.get(getParamValue("calculator", String.class));
			if (calculatorClass == null)
				throw new IllegalStateException("Couldn't find calculator for name: "+getParamValue("calculator", String.class));
			
			chunkSize = parameters.get("chunkSize").getGeneral(Integer.class);
			midpoint = parameters.get("midpoint").getGeneral(ComplexNumber.class);
			zoom = parameters.get("zoom").getGeneral(Number.class);
			
			numberFactory = parameters.get("numberFactory").getGeneral(NumberFactory.class);
			chunkFactory = parameters.get("chunkFactory").getGeneral(ArrayChunkFactory.class);
			
			LayerConfiguration oldLayerConfig = null;
			if (oldParams != null)
				oldLayerConfig = oldParams.get("layerConfiguration").getGeneral(LayerConfiguration.class);
			ParamSupplier newLayerConfigSupplier = paramContainer.getClientParameter("layerConfiguration");
			LayerConfiguration newLayerConfig = newLayerConfigSupplier.getGeneral(LayerConfiguration.class);
			if (oldLayerConfig == null || newLayerConfigSupplier.isChanged()) {
				layerConfig = newLayerConfig;
				layerConfig.prepare(numberFactory);
			} else {
				parameters.put("layerConfiguration", oldParams.get("layerConfiguration"));
			}
		
			width = parameters.get("width").getGeneral(Integer.class);
			height = parameters.get("height").getGeneral(Integer.class);
			
			border_generation = parameters.get("border_generation").getGeneral(Double.class);
			border_dispose = parameters.get("border_dispose").getGeneral(Double.class);
			buffer = parameters.get("task_buffer").getGeneral(Integer.class);
	
			if (getActiveViewData() == null || reset) {
				chunksWidth = (int)Math.ceil(width/(double)chunkSize);
				chunksHeight = (int)Math.ceil(height/(double)chunkSize);
				relativeStartShift = numberFactory.createComplexNumber(0, 0);
//				relativeStartShift = numberFactory.createComplexNumber((chunksWidth%2 == 0 ? -0.5 : 0), chunksHeight%2 == 0 ? -0.5 : 0);
			}
			
			pixelzoom = numberFactory.createNumber(width >= height ? 1./height : 1./width);
			pixelzoom.mult(zoom);
			paramContainer.addClientParameter(new StaticParamSupplier("pixelzoom", pixelzoom));
			chunkZoom = pixelzoom.copy();
			chunkZoom.mult(numberFactory.createNumber(chunkSize));
			paramContainer.addClientParameter(new StaticParamSupplier("chunkzoom", chunkZoom));
			
			Number rX = numberFactory.createNumber(0.5*(width+chunkSize));
			rX.mult(pixelzoom);
	//		Number rY = numberFactory.createNumber(0.5 * ((width > height) ? width/(double)height : 1));
			Number rY = numberFactory.createNumber(0.5*(height+chunkSize));
			rY.mult(pixelzoom);
			ComplexNumber sideDist = numberFactory.createComplexNumber(rX, rY);
	
			
			ComplexNumber anchor = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
			anchor.multNumber(numberFactory.createNumber(-0.5));
			anchor.add(midpoint);
			
			if (reset && taskManager != null)
				taskManager.reset();
			
			BreadthFirstViewData activeViewData = (BreadthFirstViewData)getActiveViewData();
			if (activeViewData == null) {
				BreadthFirstViewData viewData = new BreadthFirstViewData(anchor).setContext(this);
				setActiveViewData(viewData);
				viewData.setParams(paramContainer);
			} else {
				activeViewData.setParams(paramContainer);
			}
			ParamSupplier jobIdSupplier = parameters.get("view");
			if (jobIdSupplier == null)
				viewId = 0;
			else
				viewId = jobIdSupplier.getGeneral(Integer.class);
			chunkFactory.setViewData(getActiveViewData());
			
			if (drawRegion == null)
				drawRegion = new BFScreenDrawRegion();
			drawRegion.leftLowerCorner = midpoint.copy();
			drawRegion.leftLowerCorner.sub(sideDist);
			drawRegion.leftLowerCornerChunkX = getChunkX(drawRegion.leftLowerCorner);
			drawRegion.leftLowerCornerChunkY = getChunkY(drawRegion.leftLowerCorner);
			
			drawRegion.rightUpperCorner = sideDist;
			drawRegion.rightUpperCorner.add(midpoint);
			drawRegion.rightUpperCornerChunkX = getChunkX(drawRegion.rightUpperCorner);
			drawRegion.rightUpperCornerChunkY = getChunkY(drawRegion.rightUpperCorner);
		}
		
		return reset;
	}
	
	public static boolean needsReset(Map<String, ParamSupplier> newParams, Map<String, ParamSupplier> oldParams){ //TODO merge with method in SystemClientData
		boolean reset = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : newParams.values()) {
				supplier.updateChanged(oldParams.get(supplier.getName()));
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						reset = true;
				}
			}
		}
		return reset;
	}
	
	public double getChunkX(ComplexNumber pos) {
		Number value = pos.getReal();
		value.sub(getCurrentAnchor().getReal());
		value.div(chunkZoom);
		return value.toDouble();
	}
	
	public double getChunkY(ComplexNumber pos) {
		Number value = pos.getImag();
		value.sub(getCurrentAnchor().getImag());
		value.div(chunkZoom);
		return value.toDouble();
	}

	public ComplexNumber getPos(long chunkX, long chunkY) {
		ComplexNumber chunkPos = numberFactory.createComplexNumber(chunkX, chunkY);
		toPos(chunkPos);
		return chunkPos;
	}
	
	private ComplexNumber getCurrentAnchor() {
		BreadthFirstViewData activeViewData = (BreadthFirstViewData)getActiveViewData();
		if (activeViewData != null)
			return activeViewData.anchor;
		log.warn("getCurrentAnchor() called while activeViewData == null, returned (0, 0)");
		return numberFactory.createComplexNumber(0, 0);
	}

	public double getDrawRegionDistance(Chunk chunk) {
		return getDrawRegionDistance(chunk.getChunkX(), chunk.getChunkY());
	}
	
	public double getDrawRegionDistance(long chunkX, long chunkY) {
		return getDrawRegion().getRegionDistance(this, chunkX, chunkY);
	}

	public boolean isInDrawRegion(int chunkX, int chunkY) {
		return getDrawRegion().inRegion(this, chunkX, chunkY);
	}
	
	@Override
	public Number getPixelzoom() {
		return pixelzoom;
	}

	@Override
	public Number getZoom() {
		return zoom;
	}
	
	@Override
	public void setZoom(Number zoom) {
		this.zoom = zoom;
		StaticParamSupplier supplier = new StaticParamSupplier("zoom", zoom);
		supplier.setLayerRelevant(true);
		paramContainer.addClientParameter(supplier);	
	}
	
	@Override
	public AbstractArrayChunk createChunk(int chunkX, int chunkY) {
		return chunkFactory.createChunk(chunkX, chunkY);
	}

	public void toPos(ComplexNumber gridPos) {
		gridPos.add(relativeStartShift);
		gridPos.multNumber(chunkZoom);
		gridPos.add(((BreadthFirstViewData)getActiveViewData()).anchor);
	}

	public Number getChunkZoom() {
		return chunkZoom;
	}

	public DrawRegion<BFSystemContext> getDrawRegion() {
		return drawRegion;
	}
}
