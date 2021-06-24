package de.felixperko.fractals.system.parameters.attributes;

import de.felixperko.fractals.system.numbers.Number;

public abstract class NumberParamAttribute extends AbstractParamAttribute<Number<?>> {

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

}
