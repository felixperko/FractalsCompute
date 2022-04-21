package de.felixperko.fractals.util.codegen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader{
	
	Map<String, byte[]> resourceCache = new HashMap<String, byte[]>();
	
	public Class<?> defineClass(String name, String binPath, byte[] b){
		resourceCache.put(binPath, b);
		return defineClass(name, b, 0, b.length);
	}
	
	@Override
	public InputStream getResourceAsStream(String binPath) {
		if (resourceCache.containsKey(binPath))
			return new ByteArrayInputStream(resourceCache.get(binPath));
		return super.getResourceAsStream(binPath);
	}
}
