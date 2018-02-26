package de.codecentric.fpl.benchmark;

public interface Runner {
	public void prepare(int problemSize);
	public void run();
	public void cleanup();
}
