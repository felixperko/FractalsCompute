package de.felixperko.fractals.system.parameters.attributes;

import de.felixperko.fractals.system.numbers.ComplexNumber;

public abstract class ComplexNumberParamAttribute extends AbstractParamAttribute<ComplexNumber> {

	public ComplexNumberParamAttribute(String prefix, String name) {
		super(prefix, name);
	}

	@Override
	public Class<?> getAttributeClass() {
		return ComplexNumber.class;
	}

	@Override
	public abstract ComplexNumber getValue();

	@Override
	public abstract void applyValue(Object value);

}
