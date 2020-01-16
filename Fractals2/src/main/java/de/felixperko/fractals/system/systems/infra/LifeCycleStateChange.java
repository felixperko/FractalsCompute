package de.felixperko.fractals.system.systems.infra;

public class LifeCycleStateChange {
	
	LifeCycleState stateBefore;
	LifeCycleState stateAfter;
	long changeTime;
	
	public LifeCycleStateChange(LifeCycleState stateBefore, LifeCycleState stateAfter) {
		this.stateBefore = stateBefore;
		this.stateAfter = stateAfter;
		this.changeTime = System.nanoTime();
	}
	
	
}
