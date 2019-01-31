package de.felixperko.fractals.system.systems.infra;

import java.lang.reflect.InvocationTargetException;

public class ClassSystemFactory implements CalcSystemFactory{
	
	Class<? extends CalcSystem> cls;
	
	public ClassSystemFactory(Class<? extends CalcSystem> cls) {
		this.cls = cls;
	}
	
	@Override
	public CalcSystem createSystem() {
		try {
			return cls.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
