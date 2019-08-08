package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.infra.LifeCycleState;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.task.Layer;

public class BFSystemContext implements SystemContext {
	
	static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}
	
	BreadthFirstTaskManager taskManager;
	
	public Map<String, ParamSupplier> parameters;
	public Class<? extends FractalsCalculator> calculatorClass;
	public Integer chunkSize;
	public ComplexNumber midpoint;
	public Number zoom;
	public NumberFactory numberFactory;
	public ArrayChunkFactory chunkFactory;
	public Integer width;
	public Integer height;
	public Double border_generation;
	public Double border_dispose;
	public Integer buffer;
	public BreadthFirstViewData viewData;
	public int chunksWidth;
	public int chunksHeight;
	public ComplexNumber relativeStartShift;
	public Number chunkZoom;
	public LayerConfiguration layerConfig;
	public int jobId;
	public ComplexNumber leftLowerCorner;
	public double leftLowerCornerChunkY;
	public double leftLowerCornerChunkX;
	public ComplexNumber rightUpperCorner;
	public double rightUpperCornerChunkX;
	public double rightUpperCornerChunkY;
	
	public BFSystemContext(BreadthFirstTaskManager taskManager) {
		this.taskManager = taskManager;
	}
	
	public BreadthFirstLayer getLayer(int layerId) {
		return (BreadthFirstLayer) layerConfig.getLayer(layerId);
	}

	@Override
	public boolean setParameters(Map<String, ParamSupplier> params) {
		
		boolean reset = needsReset(params, this.parameters);
		
		Map<String, ParamSupplier> oldParams = this.parameters;
		this.parameters = params;
		
		calculatorClass = availableCalculators.get(((String)params.get("calculator").get(0, 0)));
		if (calculatorClass == null)
			throw new IllegalStateException("Couldn't find calculator for name: "+params.get("calculator").get(0, 0).toString());
		
		chunkSize = parameters.get("chunkSize").getGeneral(Integer.class);
		midpoint = parameters.get("midpoint").getGeneral(ComplexNumber.class);
		zoom = parameters.get("zoom").getGeneral(Number.class);
		
		numberFactory = parameters.get("numberFactory").getGeneral(NumberFactory.class);
		chunkFactory = parameters.get("chunkFactory").getGeneral(ArrayChunkFactory.class);
		
		width = parameters.get("width").getGeneral(Integer.class);
		height = parameters.get("height").getGeneral(Integer.class);
		
		border_generation = parameters.get("border_generation").getGeneral(Double.class);
		border_dispose = parameters.get("border_dispose").getGeneral(Double.class);
		buffer = parameters.get("task_buffer").getGeneral(Integer.class);

		if (viewData == null || reset) {
			chunksWidth = (int)Math.ceil(width/(double)chunkSize);
			chunksHeight = (int)Math.ceil(height/(double)chunkSize);
			relativeStartShift = numberFactory.createComplexNumber((chunksWidth%2 == 0 ? -0.5 : 0), chunksHeight%2 == 0 ? -0.5 : 0);
		}
		
		Number pixelzoom = numberFactory.createNumber(width >= height ? 1./height : 1./width);
		pixelzoom.mult(zoom);
		params.put("pixelzoom", new StaticParamSupplier("pixelzoom", pixelzoom));
		chunkZoom = pixelzoom.copy();
		chunkZoom.mult(numberFactory.createNumber(chunkSize));
		
		Number rX = numberFactory.createNumber(0.5*(width+chunkSize));
		rX.mult(pixelzoom);
//		Number rY = numberFactory.createNumber(0.5 * ((width > height) ? width/(double)height : 1));
		Number rY = numberFactory.createNumber(0.5*(height+chunkSize));
		rY.mult(pixelzoom);
		ComplexNumber sideDist = numberFactory.createComplexNumber(rX, rY);

		
		ComplexNumber anchor = numberFactory.createComplexNumber(chunkZoom, chunkZoom);
		anchor.multNumber(numberFactory.createNumber(-0.5));
		anchor.add(midpoint);
		
		if (reset)
			taskManager.reset();
		
		LayerConfiguration oldLayerConfig = null;
		if (oldParams != null)
			oldLayerConfig = oldParams.get("layerConfiguration").getGeneral(LayerConfiguration.class);
		ParamSupplier newLayerConfigSupplier = params.get("layerConfiguration");
		LayerConfiguration newLayerConfig = newLayerConfigSupplier.getGeneral(LayerConfiguration.class);
		if (oldLayerConfig == null || newLayerConfigSupplier.isChanged()) {
			layerConfig = newLayerConfig;
			layerConfig.prepare(numberFactory);
		} else {
			parameters.put("layerConfiguration", oldParams.get("layerConfiguration"));
		}
		
//		ParamSupplier layersParam = parameters.get("layers");
//		List<?> layers2 = layersParam.getGeneral(List.class);
//		if (layers.isEmpty() || layersParam.isChanged()) {
//			layers.clear();
//			for (Object obj : layers2) {
//				if (!(obj instanceof BreadthFirstLayer))
//					throw new IllegalStateException("content in layers isn't compartible with BreadthFirstLayer");
//				layers.add((BreadthFirstLayer)obj);
//				
//				openTasks.add(new PriorityQueue<>(comparator_distance));
//				tempList.add(new ArrayList<>());
//			}
//			if (layers.isEmpty())
//				throw new IllegalStateException("no layers configured");
//		}
		
		if (viewData == null) {
			viewData = new BreadthFirstViewData(anchor);
		}
		ParamSupplier jobIdSupplier = parameters.get("view");
		if (jobIdSupplier == null)
			jobId = 0;
		else
			jobId = jobIdSupplier.getGeneral(Integer.class);
		chunkFactory.setViewData(viewData);

		leftLowerCorner = midpoint.copy();
		leftLowerCorner.sub(sideDist);
		leftLowerCornerChunkX = getChunkX(leftLowerCorner);
		leftLowerCornerChunkY = getChunkY(leftLowerCorner);
		
		rightUpperCorner = sideDist;
		rightUpperCorner.add(midpoint);
		rightUpperCornerChunkX = getChunkX(rightUpperCorner);
		rightUpperCornerChunkY = getChunkY(rightUpperCorner);
		
		if (params.get("midpoint").isChanged() || params.get("width").isChanged() || params.get("height").isChanged() || params.get("zoom").isChanged()) {
			taskManager.updatePredictedMidpoint();
			if (!reset)
				taskManager.predictedMidpointUpdated();
		}
		
		if (reset)
			taskManager.generateRootTask();
		
		return true;
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
		value.sub(viewData.anchor.getReal());
		value.div(chunkZoom);
		return value.toDouble();
	}
	
	public double getChunkY(ComplexNumber pos) {
		Number value = pos.getImag();
		value.sub(viewData.anchor.getImag());
		value.div(chunkZoom);
		return value.toDouble();
	}
}
