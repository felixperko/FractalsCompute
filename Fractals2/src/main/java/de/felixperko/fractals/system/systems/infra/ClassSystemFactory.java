package de.felixperko.fractals.system.systems.infra;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import de.felixperko.fractals.manager.common.Managers;
import de.felixperko.fractals.manager.server.ServerManagers;

public class ClassSystemFactory implements CalcSystemFactory{
	
	Class<? extends CalcSystem> cls;
	
	public ClassSystemFactory(Class<? extends CalcSystem> cls) {
		this.cls = cls;
	}
	
	@Override
	public CalcSystem createSystem(UUID systemId, Managers managers) {
		try {
			return cls.getConstructor(UUID.class, ServerManagers.class).newInstance(systemId, managers);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
