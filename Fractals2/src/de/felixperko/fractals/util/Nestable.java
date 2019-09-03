package de.felixperko.fractals.util;

public interface Nestable<K, V> {

	boolean hasChild(K k);
	
	Nestable<K, V> getChild(K k);

	Nestable<K, V> makeChild(K k);

	Nestable<K, V> getOrMakeChild(K k);

	V getValue();
	
	boolean setValue(V v);

	void clear();

}