package de.felixperko.fractals.system.parameters.attributes;

import de.felixperko.fractals.system.numbers.Number;

public abstract class NumberParamAttribute extends AbstractParamAttribute<Number<?>> {
	
	Number min = null;
	Number max = null;
	
	public NumberParamAttribute(String prefix, String name, Number min, Number max) {
		super(prefix, name);
		this.min = min;
		this.max = max;
	}
	
	public NumberParamAttribute(String prefix, String name) {
		super(prefix, name);
	}

	@Override
	public Class<?> getAttributeClass() {
		return Number.class;
	}

	@Override
	public abstract Number<?> getValue();

	@Override
	public abstract void applyValue(Object value);
	
	@Override
	public Number getMinValue() {
		return min;
	}
	
	public void setMinValue(Number min) {
		this.min = min;
	}

	@Override
	public Number getMaxValue() {
		return max;
	}
	
	public void setMaxValue(Number max) {
		this.max = max;
	}
	
	@Override
	public void setRange(Object newMin, Object newMax) {
		if (!(newMin instanceof Number && newMax instanceof Number))
			return;
		setMinValue((Number)newMin);
		setMaxValue((Number)newMax);
	}

}
