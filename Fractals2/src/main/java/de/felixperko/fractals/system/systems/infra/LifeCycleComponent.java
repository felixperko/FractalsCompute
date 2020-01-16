package de.felixperko.fractals.system.systems.infra;

public interface LifeCycleComponent {
	public LifeCycleState getLifeCycleState();

	void setLifeCycleState(LifeCycleState state);
}
