package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.data.AbstractArrayChunk;
import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.AbstractSystemContext;
import de.felixperko.fractals.system.LayerConfiguration;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.BurningShipCalculator;
import de.felixperko.fractals.system.calculator.MandelbrotCalculator;
import de.felixperko.fractals.system.calculator.NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator;
import de.felixperko.fractals.system.calculator.NewtonThridPowerMinusOneCalculator;
import de.felixperko.fractals.system.calculator.TricornCalculator;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BFSystemContext;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstViewData;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public class OrbitSystemContext extends AbstractSystemContext<OrbitViewData, OrbitViewContainer> {

	private static final long serialVersionUID = 1713313107508895848L;
	
	protected transient static Map<String, Class<? extends FractalsCalculator>> availableCalculators = new HashMap<>();
	static {
		availableCalculators.put("MandelbrotCalculator", MandelbrotCalculator.class);
		availableCalculators.put("BurningShipCalculator", BurningShipCalculator.class);
		availableCalculators.put("TricornCalculator", TricornCalculator.class);
		availableCalculators.put("NewtonThridPowerMinusOneCalculator", NewtonThridPowerMinusOneCalculator.class);
		availableCalculators.put("NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator", NewtonEighthPowerPlusFifteenTimesForthPowerMinusSixteenCalculator.class);
	}

	public OrbitSystemContext(TaskManager<?> taskManager) {
		super(taskManager, new OrbitViewContainer());
		layer = new OrbitLayer();
		
		List<Layer> list = new ArrayList<>();
		list.add(layer);
		layerConfiguration = new LayerConfiguration(list, 0, 1, 42);
	}
	
	Layer layer;
	LayerConfiguration layerConfiguration;

	@Override
	public boolean setParameters(ParamContainer paramContainer) {
		boolean reset = this.paramContainer != null && paramContainer.needsReset(this.paramContainer.getClientParameters());
		
		Map<String, ParamSupplier> oldParams = this.paramContainer != null ? this.paramContainer.getClientParameters() : null;

		synchronized (this) {
			this.paramContainer = paramContainer;
			Map<String, ParamSupplier> parameters = paramContainer.getClientParameters();
			
			calculatorClass = availableCalculators.get(getParamValue("calculator", String.class));
			if (calculatorClass == null)
				throw new IllegalStateException("Couldn't find calculator for name: "+getParamValue("calculator", String.class));
			
			midpoint = parameters.get("midpoint").getGeneral(ComplexNumber.class);
			//zoom = parameters.get("zoom").getGeneral(Number.class);
			
			numberFactory = parameters.get("numberFactory").getGeneral(NumberFactory.class);
			chunkFactory = parameters.get("chunkFactory").getGeneral(ArrayChunkFactory.class);
			
			if (reset && taskManager != null)
				taskManager.reset();
			
			OrbitViewData activeViewData = getActiveViewData();
			if (activeViewData == null) {
				setActiveViewData(new OrbitViewData().setContext(this));
			} else {
				activeViewData.setParams(paramContainer);
			}
			ParamSupplier jobIdSupplier = parameters.get("view");
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractArrayChunk createChunk(int chunkX, int chunkY) {
		// TODO Auto-generated method stub
		return null;
	}

}
