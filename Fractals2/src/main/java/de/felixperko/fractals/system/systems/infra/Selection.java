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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptions == null) ? 0 : descriptions.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nameList == null) ? 0 : nameList.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Selection other = (Selection) obj;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nameList == null) {
			if (other.nameList != null)
				return false;
		} else if (!nameList.equals(other.nameList))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}
}
