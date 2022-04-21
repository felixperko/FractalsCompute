package de.felixperko.fractals.system.parameters.attributes;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParamAttributeContainer {
	
	Map<String, ParamAttribute<?>> attributes = new LinkedHashMap<>();
	
	public ParamAttribute<?> getAttribute(String name){
		return attributes.get(name);
	}
	
	public Map<String, ParamAttribute<?>> getAttributes(){
		return attributes;
	}
	
	public void addAttribute(ParamAttribute<?> attribute) {
		attributes.put(attribute.getQualifiedName(), attribute);
	}
	
	public ParamAttribute<?> removeAttribute(ParamAttribute<?> attribute) {
		return attributes.remove(attribute.getQualifiedName());
	}
}
