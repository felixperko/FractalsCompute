package de.felixperko.fractals.system.calculator.infra;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.parameters.MappedParamSupplier;
import de.felixperko.fractals.system.parameters.ParamSupplier;

public abstract class AbstractFractalsCalculator implements FractalsCalculator {

	Map<String, Field> fields = null;
	
	Class<? extends AbstractFractalsCalculator> fieldClass;
	
	boolean cancelled = false;
	
	public AbstractFractalsCalculator(Class<? extends AbstractFractalsCalculator> fieldClass) {
		this.fieldClass = fieldClass;
	}
	
	public void setParams(Map<String, ParamSupplier> parameters) {
		//params = new ParamSupplier[parameters.size()];
		int index = 0;
		
		if (fields == null) {
			fields = new HashMap<>();
			for (Field f : fieldClass.getDeclaredFields()) {
				if (f.getName().startsWith("p_")) {
					fields.put(f.getName().substring(2), f);
				}
			}
		}
		
		for (Entry<String, ParamSupplier> e : parameters.entrySet()) {
			ParamSupplier param = e.getValue();
			if (param instanceof MappedParamSupplier) {
				((MappedParamSupplier)param).bindParameters(parameters);
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
