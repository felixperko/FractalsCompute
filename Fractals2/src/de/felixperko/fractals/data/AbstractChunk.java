package de.felixperko.fractals.data;

import de.felixperko.fractals.system.Numbers.infra.ComplexNumber;
import de.felixperko.fractals.system.task.FractalsTask;

public abstract class AbstractChunk implements Chunk {

	private static final long serialVersionUID = 6373922478078963013L;

	Integer chunkX, chunkY;
	
	public ComplexNumber chunkPos;
	
	transient FractalsTask currentTask;
	
	
	public AbstractChunk(int chunkX, int chunkY) {

		this.chunkX = chunkX;
		this.chunkY = chunkY;
	}

	@Override
	public Integer getChunkX() {
		return chunkX;
	}
	
	@Override
	public Integer getChunkY() {
		return chunkY;
	}

	@Override
	public void setChunkX(Integer chunkX) {
		this.chunkX = chunkX;
	}

	@Override
	public void setChunkY(Integer chunkY) {
		this.chunkY = chunkY;
	}

	@Override
	public double distanceSq(double otherX, double otherY) {
		double dx = otherX-chunkX;
		double dy = otherY-chunkY;
		return dx*dx + dy*dy;
	}
	
	@Override
	public double distance(double otherX, double otherY) {
		return Math.sqrt(distanceSq(otherX, otherY));
	}

	@Override
	public FractalsTask getCurrentTask() {
		return currentTask;
	}

	@Override
	public void setCurrentTask(FractalsTask currentTask) {
		this.currentTask = currentTask;
	}
}
