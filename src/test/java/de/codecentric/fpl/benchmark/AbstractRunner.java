package de.codecentric.fpl.benchmark;

public abstract class AbstractRunner implements Runner {
	protected int problemSize;
	
	@Override
	public void prepare(int problemSize) {
		this.problemSize = problemSize;
	}

	@Override
	public void cleanup() {
		problemSize = 0;
	}
}
