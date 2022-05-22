package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import de.felixperko.fractals.system.calculator.FibonacciPowCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.TricornCalculator;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeCpuCalculator;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeCpuCalculatorNew;
import de.felixperko.fractals.system.calculator.EscapeTime.EscapeTimeGpuCalculator;
import de.felixperko.fractals.system.calculator.infra.DeviceType;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.systems.infra.DrawRegion;
import de.felixperko.fractals.system.task.TaskManager;

public class BFSystemContext extends AbstractSystemContext<BreadthFirstViewData, BFViewContainer> implements ZoomableSystemContext<BFViewContainer> {
	
	private static final long serialVersionUID = -6082120140942989559L;

	protected transient static Map<String, Class<? extends FractalsCalculator>> availableCpuCalculators = new HashMap<>();
	protected transient static Map<String, Class<? extends FractalsCalculator>> availableGpuCalculators = new HashMap<>();
	static {
		availableCpuCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCpuCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCpuCalculators.put("TricornCalculator", TricornCalculator.class);
		availableCpuCalculators.put("CustomCalculator", EscapeTimeCpuCalculatorNew.class);
		availableCpuCalculators.put("FibonacciPowCalculator", FibonacciPowCalculator.class);
		availableCpuCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCpuCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
		
		availableGpuCalculators.put("CustomCalculator", EscapeTimeGpuCalculator.class);
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
	
	private transient Class<? extends FractalsCalculator> cpuCalculatorClass = null;
	private transient Class<? extends FractalsCalculator> gpuCalculatorClass = null;
	
	transient BFScreenDrawRegion drawRegion;
	
	private transient Number pixelzoom;
	
	transient Logger log = LoggerFactory.getLogger(BFSystemContext.class);
	
	public BFSystemContext(TaskManager<?> taskManager, ParamConfiguration paramConfiguration) {
		super(taskManager, new BFViewContainer(0), paramConfiguration);
//        RendererContext rendererContext = renderer.getRendererContext();
//        if (rendererContext.getParamContainer() != null){
//            this.paramContainer = rendererContext.getParamContainer();
//        } else {
//            rendererContext.setParamContainer(this.paramContainer);
//        }
		setParamConfiguration(paramConfiguration);
		viewContainer.setContext(this);
		if (taskManager != null)
			systemStateInfo = taskManager.getSystem().getSystemStateInfo();
	}

	@Override
	public FractalsCalculator createCalculator(DeviceType deviceType) {
		Class<? extends FractalsCalculator> cls = null;
		try {
			if (deviceType == DeviceType.CPU)
				cls = cpuCalculatorClass;
			else if (deviceType == DeviceType.GPU)
				cls = gpuCalculatorClass;
			if (cls == null)
				return null;
			FractalsCalculator calculator = cls.getDeclaredConstructor().newInstance();
			calculator.setContext(this);
			return calculator;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Failed to create calculator for class: "+cls.getName());
		}
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
		
		boolean reset = this.paramContainer != null && needsReset(paramContainer.getParamMap(), this.paramContainer.getParamMap());
		
		//if params aren't applicable to current ViewData, search inactive ViewDatas
		if (reset) {
			Collection<BreadthFirstViewData> oldViews = viewContainer.getInactiveViews();
			if (!oldViews.isEmpty()) {
				ParamContainer temp = new ParamContainer(paramConfiguration);
				temp.setParams(paramContainer.getParamMap(), new LinkedHashMap<>());
				for (BreadthFirstViewData oldViewData : oldViews) {
					ParamContainer oldParams = oldViewData.getParams();
					if (oldParams != null){ //TODO should these be buffered at all? shouldn't contain chunks anyways
						if (!needsReset(temp.getParamMap(), oldParams.getParamMap())) {
							reset = false;
							viewContainer.reactivateViewData(oldViewData);
							this.paramContainer = oldParams; //TODO correct?
							break;
						}
					}
				}
			}
		}
		
		Map<String, ParamSupplier> oldParams = this.paramContainer != null ? this.paramContainer.getParamMap() : null;
		
		synchronized (this) {
			this.paramContainer = paramContainer;
			paramContainer.setParamConfiguration(paramConfiguration);
//			Map<String, ParamSupplier> parameters = paramContainer.getClientParameters();
			
			String calculatorName = getParamValue(CommonFractalParameters.PARAM_CALCULATOR, String.class);
			cpuCalculatorClass = availableCpuCalculators.get(calculatorName);
			gpuCalculatorClass = availableGpuCalculators.get(calculatorName);
			if (cpuCalculatorClass == null && gpuCalculatorClass == null)
				throw new IllegalStateException("Couldn't find calculator for name: "+calculatorName);
			
			chunkSize = paramContainer.getParam(CommonFractalParameters.PARAM_CHUNKFACTORY).getGeneral(ArrayChunkFactory.class).getChunkSize();
			midpoint = paramContainer.getParam(CommonFractalParameters.PARAM_MIDPOINT).getGeneral(ComplexNumber.class);
			zoom = paramContainer.getParam(BreadthFirstSystem.PARAM_ZOOM).getGeneral(Number.class);
			
			numberFactory = paramContainer.getParam(CommonFractalParameters.PARAM_NUMBERFACTORY).getGeneral(NumberFactory.class);
			chunkFactory = paramContainer.getParam(CommonFractalParameters.PARAM_CHUNKFACTORY).getGeneral(ArrayChunkFactory.class);
			
			boolean layersChanged = updateLayerConfig(paramContainer, BreadthFirstSystem.PARAM_LAYER_CONFIG, oldParams, paramContainer.getParamMap());
			if (layersChanged)
				reset = true;
		
			width = paramContainer.getParam(BreadthFirstSystem.PARAM_WIDTH).getGeneral(Integer.class);
			height = paramContainer.getParam(BreadthFirstSystem.PARAM_HEIGHT).getGeneral(Integer.class);
			
			border_generation = paramContainer.getParam(BreadthFirstSystem.PARAM_BORDER_GENERATION).getGeneral(Double.class);
			border_dispose = paramContainer.getParam(BreadthFirstSystem.PARAM_BORDER_DISPOSE).getGeneral(Double.class);
			buffer = paramContainer.getParam(BreadthFirstSystem.PARAM_TASK_BUFFER).getGeneral(Integer.class);
	
			if (getActiveViewData() == null || reset) {
				chunksWidth = (int)Math.ceil(width/(double)chunkSize);
				chunksHeight = (int)Math.ceil(height/(double)chunkSize);
				relativeStartShift = numberFactory.createComplexNumber(0, 0);
//				relativeStartShift = numberFactory.createComplexNumber((chunksWidth%2 == 0 ? -0.5 : 0), chunksHeight%2 == 0 ? -0.5 : 0);
			}
			
			pixelzoom = numberFactory.createNumber(width >= height ? 1./height : 1./width);
			pixelzoom.mult(zoom);
			paramContainer.addParam(new StaticParamSupplier("pixelzoom", pixelzoom)); //TODO deprecated
			chunkZoom = pixelzoom.copy();
			chunkZoom.mult(numberFactory.createNumber(chunkSize));
			paramContainer.addParam(new StaticParamSupplier("chunkzoom", chunkZoom)); //TODO deprecated			
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
			ParamSupplier jobIdSupplier = paramContainer.getParam(CommonFractalParameters.PARAM_VIEW);
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
	
	public static boolean needsReset(Map<String, ParamSupplier> newParams, Map<String, ParamSupplier> oldParams){
		boolean reset = false;
		if (oldParams != null) {
			for (ParamSupplier supplier : newParams.values()) {
				supplier.updateChanged(oldParams.get(supplier.getUID()));
				if (supplier.isChanged()) {
					if (supplier.isSystemRelevant() || supplier.isLayerRelevant())
						reset = true;
				}
			}
		}
		return reset;
	}
	
//	public double getChunkX(ComplexNumber pos) {
//		Number value = midpoint.getReal().copy();
//		value.sub(pos.getReal());
//		value.div(chunkZoom);
//		return value.toDouble();
//	}
//	
//	public double getChunkY(ComplexNumber pos) {
//		Number value = midpoint.getImag().copy();
//		value.sub(pos.getImag());
//		value.div(chunkZoom);
//		return value.toDouble();
//	}
	
	public double getChunkX(ComplexNumber pos) {
		Number value = pos.getReal();
		ComplexNumber anchor = getCurrentAnchor();
		value.sub(anchor.getReal());
		value.div(chunkZoom);
		return value.toDouble();
	}
	
	public double getChunkY(ComplexNumber pos) {
		Number value = pos.getImag();
		ComplexNumber anchor = getCurrentAnchor();
		value.sub(anchor.getImag());
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
		StaticParamSupplier supplier = new StaticParamSupplier("zoom", zoom); //TODO deprecated
		supplier.setLayerRelevant(true);
		paramContainer.addParam(supplier);	
	}
	
	@Override
	public AbstractArrayChunk createChunk(int chunkX, int chunkY) {
		return chunkFactory.createChunk(chunkX, chunkY);
	}

	public void toPos(ComplexNumber gridPos) {
		gridPos.add(relativeStartShift);
		gridPos.multNumber(chunkZoom);
//		BreadthFirstViewData activeViewData = (BreadthFirstViewData)getActiveViewData();
//		ComplexNumber anchor = activeViewData.anchor;
//		gridPos.add(anchor);
		gridPos.add(((BreadthFirstViewData)getActiveViewData()).anchor);
	}

	public Number getChunkZoom() {
		return chunkZoom;
	}

	public DrawRegion<BFSystemContext> getDrawRegion() {
		return drawRegion;
	}

	public Class<? extends FractalsCalculator> getCpuCalculatorClass() {
		return cpuCalculatorClass;
	}

	public Class<? extends FractalsCalculator> getGpuCalculatorClass() {
		return gpuCalculatorClass;
	}

	@Override
	public Map<String, ParamSupplier> getParametersByName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ParamSupplier> getParametersByUID() {
		// TODO Auto-generated method stub
		return null;
	}
}
