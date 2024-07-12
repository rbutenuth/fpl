package de.codecentric.fpl;

@FunctionalInterface
public interface Executable<T> {

	T execute() throws Throwable;

}
