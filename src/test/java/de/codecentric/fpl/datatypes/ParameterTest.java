package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParameterTest {
	private Parameter parameter = new Parameter(new Symbol("foo"), 42);
	
	@Test
	public void getNameReturnsName() {
		assertEquals("foo", parameter.getName());
	}
	
	@Test
	public void toStringReturnsName() {
		assertEquals("foo", parameter.toString());
	}
}
