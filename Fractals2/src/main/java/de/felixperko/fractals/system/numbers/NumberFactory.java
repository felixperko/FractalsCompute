package de.felixperko.fractals.system.numbers;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.felixperko.fractals.util.serialization.jackson.JsonAbstractTypedObject;

public class NumberFactory extends JsonAbstractTypedObject implements Serializable{
	
	private static final long serialVersionUID = 2762024579658361740L;
	public static final String TYPE_NAME = "numberFactory";
	
	Class<? extends Number> numberClass;
	Class<? extends ComplexNumber> complexNumberClass;
	
	@JsonCreator
	public NumberFactory(@JsonProperty("numberClass") Class<? extends Number> numberClass, @JsonProperty("complexNumberClass") Class<? extends ComplexNumber> complexNumberClass) {
		super(TYPE_NAME);
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
	
	public Number createNumber(String value) {
		if (numberClass == null)
			throw new IllegalStateException("NumberFactory has no numberClass configured");
		try {
			return numberClass.getDeclaredConstructor(String.class).newInstance(value);
		} catch (InvocationTargetException e){
			if ((e.getCause() instanceof NumberFormatException)){
				throw (NumberFormatException)e.getCause();
			}
			else {
				e.printStackTrace();
				return null;
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ComplexNumber createComplexNumber(double real, double imag) {
		if (complexNumberClass == null)
			throw new IllegalStateException("NumberFactory has no complexNumberClass configured");
		try {
//			Number r = createNumber(real);
//			Number i = createNumber(imag);
//			return complexNumberClass.getDeclaredConstructor(numberClass, numberClass).newInstance(r, i);
			return complexNumberClass.getDeclaredConstructor(double.class, double.class).newInstance(real, imag);
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

	public ComplexNumber createComplexNumber(String r, String i) {
		if (complexNumberClass == null)
			throw new IllegalStateException("NumberFactory has no complexNumberClass configured");
		try {
			return complexNumberClass.getDeclaredConstructor(String.class, String.class).newInstance(r, i);
		} catch (InvocationTargetException e){
			if ((e.getCause() instanceof NumberFormatException)){
				throw (NumberFormatException)e.getCause();
			}
			else {
				e.printStackTrace();
				return null;
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof NumberFactory))
			return false;
		NumberFactory other = (NumberFactory) obj;
		if (numberClass == null ^ other.numberClass == null || complexNumberClass == null ^ other.complexNumberClass == null)
			return false;
		return (((numberClass == null && other.numberClass == null) || numberClass.equals(other.numberClass))
				&& ((complexNumberClass == null && other.complexNumberClass == null) || complexNumberClass.equals(other.complexNumberClass)));
	}

	public Class<? extends Number> getNumberClass() {
		return numberClass;
	}

	public void setNumberClass(Class<? extends Number> numberClass) {
		this.numberClass = numberClass;
	}

	public Class<? extends ComplexNumber> getComplexNumberClass() {
		return complexNumberClass;
	}

	public void setComplexNumberClass(Class<? extends ComplexNumber> complexNumberClass) {
		this.complexNumberClass = complexNumberClass;
	}
}