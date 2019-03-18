package de.felixperko.fractals.system.systems.BreadthFirstSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Selection<T> implements Serializable{
	
	private static final long serialVersionUID = -7610746896579572016L;
	
	String name;
	Map<String, T> options = new HashMap<>();
	List<String> nameList = new ArrayList<>();
	
	public Selection(String name){
		this.name = name;
	}
	
	public void addOption(String name, T option) {
		options.put(name, option);
		nameList.add(name);
	}
	
	public T getOption(String name) {
		return options.get(name);
	}
	
	public List<String> getOptionNames(){
		return nameList;
	}

	public String getName() {
		return name;
	}
}
