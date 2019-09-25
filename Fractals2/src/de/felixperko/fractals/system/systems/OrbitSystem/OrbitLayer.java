package de.felixperko.fractals.system.systems.OrbitSystem;

import de.felixperko.fractals.system.task.Layer;

public class OrbitLayer implements Layer {

	private static final long serialVersionUID = -6099129991281941671L;

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public boolean isActive(int pixel) {
		return true;
	}

	@Override
	public double getPriorityMultiplier() {
		return 1;
	}

	@Override
	public double getPriorityShift() {
		return 0;
	}

	@Override
	public int getSampleCount() {
		return 1;
	}

	@Override
	public boolean cullingEnabled() {
		return false;
	}

	@Override
	public boolean renderingEnabled() {
		return true;
	}

	@Override
	public int getUpsample() {
		return 1;
	}

	@Override
	public void setId(int id) {
		if (id != 0)
			throw new IllegalArgumentException();
	}

}
