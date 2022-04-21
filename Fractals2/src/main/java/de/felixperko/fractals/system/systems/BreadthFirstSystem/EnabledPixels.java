package de.felixperko.fractals.system.systems.BreadthFirstSystem;

public interface EnabledPixels {
	public boolean isEnabled(int index);
	public int nextEnabled(int currentIndex);
}
