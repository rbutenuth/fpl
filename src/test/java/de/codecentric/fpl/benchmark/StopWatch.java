package de.codecentric.fpl.benchmark;

public class StopWatch {
	private boolean running;
	private long startTime;
	private long totalTime;

	public StopWatch start() {
		if (running) {
			throw new IllegalStateException("already running");
		}
		running = true;
		startTime = System.currentTimeMillis();
		return this;
	}
	
	public long stop() {
		if (!running) {
			throw new IllegalStateException("not running");
		}
		running = false;
		totalTime += System.currentTimeMillis() - startTime;
		return totalTime;
	}
	
	public long totalTime() {
		if (running) {
			throw new IllegalStateException("running");
		}
		return totalTime;
	}
	
	public void reset() {
		running = false;
		totalTime = 0;
	}
}
