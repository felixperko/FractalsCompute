package de.felixperko.fractals.system.systems.infra;

import java.lang.reflect.InvocationTargetException;

import de.felixperko.fractals.manager.Managers;
import de.felixperko.fractals.manager.ServerManagers;

public class ClassSystemFactory implements CalcSystemFactory{
	
	Class<? extends CalcSystem> cls;
	
	public ClassSystemFactory(Class<? extends CalcSystem> cls) {
		this.cls = cls;
	}
	
	@Override
	public CalcSystem createSystem(Managers managers) {
		try {
			return cls.getConstructor(ServerManagers.class).newInstance(managers);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
