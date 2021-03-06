package de.felixperko.fractals.system.systems.infra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Selection<T> implements Serializable{
	
	private static final long serialVersionUID = -7610746896579572016L;
	
	String name;
	Map<String, T> options = new HashMap<>();
	Map<String, String> descriptions = new HashMap<>();
	List<String> nameList = new ArrayList<>();
	
	public Selection(String name){
		this.name = name;
	}
	
	public void addOption(String name, T option, String description) {
		options.put(name, option);
		descriptions.put(name, description);
		nameList.add(name);
	}
	
	public T getOption(String name) {
		return options.get(name);
	}
	
	public List<String> getOptionNames(){
		return nameList;
	}
	
	public String getDescription(String name){
		return descriptions.get(name);
	}

	public String getName() {
		return name;
	}
}
