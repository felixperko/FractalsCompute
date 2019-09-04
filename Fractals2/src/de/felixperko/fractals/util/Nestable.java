package de.felixperko.fractals.util;

import java.util.Collection;

import de.felixperko.fractals.data.CompressedChunk;

public interface Nestable<K, V> {

	boolean hasChild(K k);
	
	Nestable<K, V> getChild(K k);

	Nestable<K, V> makeChild(K k);

	Nestable<K, V> getOrMakeChild(K k);

	V getValue();
	
	boolean setValue(V v);

	void clear();

	Collection<NestedMap<K, V>> getChildren();

	boolean removeChild(K k);

	boolean hasChildren();

}