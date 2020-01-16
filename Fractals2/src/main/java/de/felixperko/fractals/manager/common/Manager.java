package de.felixperko.fractals.manager.common;

public abstract class Manager {

	protected Managers managers;

	public Manager(Managers managers) {
		this.managers = managers;
	}
	
	public Managers getManagers() {
		return managers;
	}
}