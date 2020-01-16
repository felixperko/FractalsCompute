package de.felixperko.fractals.system.task;

import java.lang.reflect.InvocationTargetException;

public class ClassTaskFactory implements TaskFactory{
	
	Class<? extends FractalsTask> cls;
	
	public ClassTaskFactory(Class<? extends FractalsTask> cls) {
		this.cls = cls;
	}

	@Override
	public FractalsTask createTask() {
		try {
			return cls.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
