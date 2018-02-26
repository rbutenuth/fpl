package de.codecentric.fpl.benchmark;

public class Sink<T> {
	private volatile T value;
	
	public void use(T value) {
		this.value = value;
	}
	
	public T get() {
		return value;
	}
}
