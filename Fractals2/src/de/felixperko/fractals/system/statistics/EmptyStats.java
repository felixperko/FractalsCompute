package de.felixperko.fractals.system.statistics;

/**
 * Placeholder implementation for when TaskStats shouldn't be collected.
 */
public class EmptyStats implements IStats {

	private static final long serialVersionUID = 2749138511498142258L;

	@Override
	public void addSample(int iterations, double result) {
	}

	@Override
	public void addCulled() {
	}

	@Override
	public void executionStart() {
		throw new IllegalStateException();
	}

	@Override
	public void executionEnd() {
		throw new IllegalStateException();
	}

	@Override
	public long getExecutionTimeNS() {
		return 0;
	}

}
