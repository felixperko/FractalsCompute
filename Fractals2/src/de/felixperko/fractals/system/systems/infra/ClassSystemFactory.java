package de.felixperko.fractals.system.systems.infra;

import java.lang.reflect.InvocationTargetException;

import de.felixperko.fractals.manager.ThreadManager;

public class ClassSystemFactory implements CalcSystemFactory{
	
	Class<? extends CalcSystem> cls;
	
	public ClassSystemFactory(Class<? extends CalcSystem> cls) {
		this.cls = cls;
	}
	
	@Override
	public CalcSystem createSystem(ThreadManager threadManager) {
		try {
			return cls.getConstructor(ThreadManager.class).newInstance(threadManager);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
