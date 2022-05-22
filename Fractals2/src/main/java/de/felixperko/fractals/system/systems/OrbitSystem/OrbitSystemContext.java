package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.data.ParamContainer;
import de.felixperko.fractals.system.AbstractSystemContext;
import de.felixperko.fractals.system.LayerConfiguration;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.TricornCalculator;
import de.felixperko.fractals.system.calculator.infra.DeviceType;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.numbers.ComplexNumber;
import de.felixperko.fractals.system.numbers.Number;
import de.felixperko.fractals.system.numbers.NumberFactory;
import de.felixperko.fractals.system.parameters.ParamConfiguration;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.common.CommonFractalParameters;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public class OrbitSystemContext extends AbstractSystemContext<OrbitViewData, OrbitViewContainer> {

	private static final long serialVersionUID = 1713313107508895848L;
	
	transient Class<? extends FractalsCalculator> calculatorClass;
	
	protected transient static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("TricornCalculator", TricornCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}

	public OrbitSystemContext(TaskManager<?> taskManager, ParamConfiguration paramConfiguration) {
		super(taskManager, new OrbitViewContainer(), paramConfiguration);
		
//		layer = new OrbitLayer();
//		
//		List<Layer> list = new ArrayList<>();
//		list.add(layer);
//		layerConfiguration = new LayerConfiguration(list, 0, 1, 42);
	}
	
	Layer layer;
	LayerConfiguration layerConfiguration;

	@Override
	public boolean setParameters(ParamContainer paramContainer) {
		boolean reset = this.paramContainer != null && paramContainer.needsReset(this.paramContainer.getParamMap());
		
		Map<String, ParamSupplier> oldParams = this.paramContainer != null ? this.paramContainer.getParamMap() : null;

		synchronized (this) {
			this.paramContainer = paramContainer;
			Map<String, ParamSupplier> parameters = paramContainer.getParamMap();
			
			calculatorClass = availableCalculators.get(getParamValue(CommonFractalParameters.PARAM_CALCULATOR, String.class));
			if (calculatorClass == null)
				throw new IllegalStateException("Couldn't find calculator for name: "+getParamValue(CommonFractalParameters.PARAM_CALCULATOR, String.class));
			
			midpoint = parameters.get(CommonFractalParameters.PARAM_MIDPOINT).getGeneral(ComplexNumber.class);
			//zoom = parameters.get("zoom").getGeneral(Number.class);
			
			numberFactory = parameters.get(CommonFractalParameters.PARAM_NUMBERFACTORY).getGeneral(NumberFactory.class);
			chunkFactory = parameters.get(CommonFractalParameters.PARAM_CHUNKFACTORY).getGeneral(ArrayChunkFactory.class);
			
			if (reset && taskManager != null)
				taskManager.reset();
			
			OrbitViewData activeViewData = getActiveViewData();
			if (activeViewData == null) {
				setActiveViewData(new OrbitViewData().setContext(this));
			} else {
				activeViewData.setParams(paramContainer);
			}
			ParamSupplier jobIdSupplier = parameters.get(CommonFractalParameters.PARAM_VIEW);
			if (jobIdSupplier == null)
				viewId = 0;
			else
				viewId = jobIdSupplier.getGeneral(Integer.class);
			chunkFactory.setViewData(getActiveViewData());
		}
		
		return reset;
	}

	@Override
	public Number getPixelzoom() {
		return null;
	}

	@Override
	public void taskStateUpdated(TaskStateInfo taskStateInfo, TaskState oldState) {
		
	}

	@Override
	public AbstractArrayChunk createChunk(int chunkX, int chunkY) {
		return chunkFactory.createChunk(chunkX, chunkY);
	}

	@Override
	public FractalsCalculator createCalculator(DeviceType deviceType) {
		// TODO Auto-generated method stub
		return null;
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
