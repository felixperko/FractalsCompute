package de.felixperko.fractals.system.Numbers.infra;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public class NumberFactory implements Serializable{
	
	Class<? extends Number> numberClass;
	Class<? extends ComplexNumber> complexNumberClass;
	
	public NumberFactory(Class<? extends Number> numberClass, Class<? extends ComplexNumber> complexNumberClass) {
		this.numberClass = numberClass;
		this.complexNumberClass = complexNumberClass;
	}
	
	public Number createNumber(double value) {
		if (numberClass == null)
			throw new IllegalStateException("NumberFactory has no numberClass configured");
		try {
			return numberClass.getDeclaredConstructor(Double.TYPE).newInstance(value);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ComplexNumber createComplexNumber(double real, double imag) {
		if (complexNumberClass == null)
			throw new IllegalStateException("NumberFactory has no complexNumberClass configured");
		try {
			Number r = createNumber(real);
			Number i = createNumber(imag);
			return complexNumberClass.getDeclaredConstructor(numberClass, numberClass).newInstance(r, i);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ComplexNumber createComplexNumber(Number r, Number i) {
		if (complexNumberClass == null)
			throw new IllegalStateException("NumberFactory has no complexNumberClass configured");
		try {
			return complexNumberClass.getDeclaredConstructor(numberClass, numberClass).newInstance(r, i);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
