package de.felixperko.fractals.data;

public enum BorderAlignment {
	
	LEFT(true), RIGHT(true), UP(false), DOWN(false);
	
	final boolean horizontal;
	
	private BorderAlignment(boolean horizontal) {
		this.horizontal = horizontal;
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	public boolean isVertical() {
		return !horizontal;
	}
	
	public BorderAlignment getAlignmentForNeighbour() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default:
			return null;
		}
	}
}
