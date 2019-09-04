package de.felixperko.fractals.util;

import java.util.HashMap;
import java.util.Iterator;

import de.felixperko.fractals.data.Chunk;

/**
 * Provides functionality to store and retrieve data in nested HashMaps using a common key classs
 */
public class NestedMap<K, V> implements Nestable<K, V>{

    @SuppressWarnings("rawtypes")
	private final HashMap<K, NestedMap> child;
    private V value;

    public NestedMap() {
        child = new HashMap<>();
        value = null;
    }

    @Override
	public boolean hasChild(K k) {
        return this.child.containsKey(k);
    }

	/**
	 * @param k - the key for a child
	 * @return NestedMap if child exists, otherwise NestedNull to avoid NullPointerExceptions on traversal of the nested structure
	 */
    @Override
	public Nestable<K, V> getChild(K k) {
    	Nestable child = this.child.get(k);
    	if (child != null)
    		return child;
    	return NestedNull.getInstance();
    }

    @Override
	public Nestable<K, V> makeChild(K k) {
    	NestedMap<K, V> child = new NestedMap<K, V>();
        this.child.put(k, child);
        return child;
    }

	@Override
	public Nestable<K, V> getOrMakeChild(K k) {
		Nestable<K, V> child = getChild(k);
		if (child != null)
			return child;
		return makeChild(k);
	}

    @Override
	public V getValue() {
        return value;
    }
	
	/**
	 * Sets the value associated with this NestedMap.
	 * @param v - new Value
	 * @return whether an existing value was overwritten or not
	 */
    @Override
	public boolean setValue(V v) {
    	boolean replaced = value != null;
        this.value = v;
        return replaced;
    }
    
    /**
     * clears this node and the nested structure beneath
     */
	@Override
	public void clear() {
		Iterator<NestedMap> childIt = child.values().iterator();
		while (childIt.hasNext()) {
			childIt.next().clear();
		}
		child.clear();
		this.value = null;
	}
}