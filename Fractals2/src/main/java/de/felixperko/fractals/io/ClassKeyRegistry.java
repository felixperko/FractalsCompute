package de.felixperko.fractals.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.system.parameters.suppliers.ParamSupplier;

public abstract class ClassKeyRegistry<T> {

	protected Map<Class<? extends T>, String> classKeys = new HashMap<>();

	protected abstract void initDefaultClassKeys();

	Map<String, Class<? extends T>> classesByKey = new HashMap<>();

	
	public ClassKeyRegistry() {
		initDefaultClassKeys();
		initKeyMap();
	}
	
	public ClassKeyRegistry(Map<Class<? extends T>, String> additionalKeys) {
		for (Entry<Class<? extends T>, String> e : additionalKeys.entrySet()) {
			classKeys.put(e.getKey(), e.getValue());
		}
		initDefaultClassKeys();
		initKeyMap();
	}

	protected void initKeyMap() {
		for (Entry<Class<? extends T>, String> e : classKeys.entrySet()) {
			classesByKey.put(e.getValue(), e.getKey());
		}
	}

	public String getClassKey(Class<? extends T> cls) {
		return classKeys.get(cls);
	}

	public Class<? extends T> getClass(String key) {
		return classesByKey.get(key);
	}

}