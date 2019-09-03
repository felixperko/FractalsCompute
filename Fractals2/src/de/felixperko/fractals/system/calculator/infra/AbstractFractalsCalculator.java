package de.felixperko.fractals.system.calculator.infra;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.parameters.suppliers.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;
import de.felixperko.fractals.system.systems.infra.SystemContext;

public abstract class AbstractFractalsCalculator implements FractalsCalculator {

	private static final long serialVersionUID = 448587812207550305L;

	transient Map<String, Field> fields = null;
	
	SystemContext systemContext;
	
	Class<? extends AbstractFractalsCalculator> fieldClass;
	
	boolean cancelled = false;
	
	public AbstractFractalsCalculator(Class<? extends AbstractFractalsCalculator> fieldClass) {
		this.fieldClass = fieldClass;
	}
	
	@Override
	public void setParams(SystemContext systemContext, Map<String, ParamSupplier> localParameters) {
		//params = new ParamSupplier[parameters.size()];
		this.systemContext = systemContext;
		
		int index = 0;
		
		if (fields == null) {
			fields = new HashMap<>();
			for (Field f : fieldClass.getDeclaredFields()) {
				if (f.getName().startsWith("p_")) {
					fields.put(f.getName().substring(2), f);
				}
			}
		}
		
		for (Entry<String, ParamSupplier> e : localParameters.entrySet()) {
			ParamSupplier param = e.getValue();
			if (param instanceof MappedParamSupplier) {
				((MappedParamSupplier)param).bindParameters(systemContext, localParameters);
			}
			//params[index] = e.getValue();
			//set index field if found
			Field f = fields.get(e.getKey());
			if (f != null) {
				try {
					f.set(this, param);
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled() {
		cancelled = true;
	}
}
