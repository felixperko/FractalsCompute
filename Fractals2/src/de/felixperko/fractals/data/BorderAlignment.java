package de.felixperko.fractals.data;

public enum BorderAlignment{
	
	LEFT(true, -1, 0), RIGHT(true, 1, 0), UP(false, 0, 1), DOWN(false, 0, -1);
	
	boolean horizontal;
	int offsetX;
	int offsetY;
	
	private BorderAlignment(boolean horizontal, int offsetX, int offsetY) {
		this.horizontal = horizontal;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
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
	
	public int getNeighbourX(int x) {
		return x+offsetX;
	}
	
	public int getNeighbourY(int y) {
		return y+offsetY;
	}
}
