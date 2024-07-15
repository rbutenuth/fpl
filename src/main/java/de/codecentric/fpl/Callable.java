package de.codecentric.fpl;

@FunctionalInterface
public interface Callable<T> {

	T execute() throws Throwable;

}
