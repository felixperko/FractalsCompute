package de.felixperko.fractals.system.systems.OrbitSystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.data.ArrayChunkFactory;
import de.felixperko.fractals.network.ParamContainer;
import de.felixperko.fractals.network.infra.connection.ServerConnection;
import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.Numbers.infra.Number;
import de.felixperko.fractals.system.Numbers.infra.NumberFactory;
import de.felixperko.fractals.system.calculator.infra.FractalsCalculator;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.StaticParamSupplier;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BFSystemContext;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.BreadthFirstViewData;
import de.felixperko.fractals.system.systems.BreadthFirstSystem.LayerConfiguration;
import de.felixperko.fractals.system.systems.infra.SystemContext;
import de.felixperko.fractals.system.systems.infra.ViewContainer;
import de.felixperko.fractals.system.systems.stateinfo.TaskState;
import de.felixperko.fractals.system.systems.stateinfo.TaskStateInfo;
import de.felixperko.fractals.system.task.Layer;
import de.felixperko.fractals.system.task.TaskManager;

public class OrbitSystemContext extends BFSystemContext {

	public OrbitSystemContext(TaskManager<?> taskManager) {
		super(taskManager);
	}

	@Override
	public boolean setParameters(ParamContainer paramContainer) {
		boolean reset = this.paramContainer != null && needsReset(paramContainer.getClientParameters(), this.paramContainer.getClientParameters());
		
		Map<String, ParamSupplier> oldParams = this.paramContainer != null ? this.paramContainer.getClientParameters() : null;
		
		synchronized (this) {
			this.paramContainer = paramContainer;
			Map<String, ParamSupplier> parameters = paramContainer.getClientParameters();
			
			calculatorClass = availableCalculators.get(getParamValue("calculator", String.class));
			if (calculatorClass == null)
				throw new IllegalStateException("Couldn't find calculator for name: "+getParamValue("calculator", String.class));
			
			midpoint = parameters.get("midpoint").getGeneral(ComplexNumber.class);
			zoom = parameters.get("zoom").getGeneral(Number.class);
			
			numberFactory = parameters.get("numberFactory").getGeneral(NumberFactory.class);
			chunkFactory = parameters.get("chunkFactory").getGeneral(ArrayChunkFactory.class);
			
			if (reset && taskManager != null)
				taskManager.reset();
			
			BreadthFirstViewData activeViewData = (BreadthFirstViewData) getActiveViewData();
			if (activeViewData == null) {
				setActiveViewData(new BreadthFirstViewData(numberFactory.createComplexNumber(0, 0)).setContext(this));
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
	public Layer getLayer(int layerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LayerConfiguration getLayerConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getPixelzoom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChunkSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
