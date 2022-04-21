package de.felixperko.fractals.util.serialization.jackson;

import de.felixperko.fractals.system.numbers.impl.DoubleNumber;

public class JsonValueWrapper extends JsonAbstractTypedObject{
	
	public static final String TYPE_NAME = "wrappedvalue";
	
	Class<?> cls;
	String val;
	
	public JsonValueWrapper() {
		super(TYPE_NAME);
	}

	public JsonValueWrapper(Object val) {
		super(TYPE_NAME);
		this.val = val.toString();
		this.cls = val.getClass();
	}

	public Class<?> getCls() {
		return cls;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}
}
