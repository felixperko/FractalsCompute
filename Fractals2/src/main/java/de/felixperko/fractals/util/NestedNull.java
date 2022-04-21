package de.felixperko.fractals.util;

import java.util.Collection;
import java.util.Collections;

/**
 * Contains a singleton that represents an empty node in the nested structure for null-safe traversal of the nested structure.
 * Accessible through NestedNull.getInstance();
 * getChild() returns itself
 * getValue() returns null
 * @param <K>
 * @param <V>
 */
public class NestedNull<K, V> implements Nestable<K, V> {
	
	private static NestedNull instance;
	
	public static NestedNull getInstance() {
		if (instance == null)
			instance = new NestedNull();
		return instance;
	}
	
	private NestedNull() {} //can't be created elsewhere

	@Override
	public boolean hasChild(K k) {
		return false;
	}

	@Override
	public Nestable<K, V> getChild(K k) {
		return this;
	}

	@Override
	public Nestable<K, V> makeChild(K k) {
		return this;
	}

	@Override
	public Nestable<K, V> getOrMakeChild(K k) {
		return this;
	}

	@Override
	public V getValue() {
		return null;
	}
	
	@Override
	public boolean setValue(V v) {
		throw new IllegalStateException("can't set the value of the NestedNull object");
	}

	@Override
	public void clear() {
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<NestedMap<K, V>> getChildren() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean removeChild(K k) {
		return false;
	}

	
	@Override
	public boolean hasChildren() {
		return false;
	}
	
	
}
