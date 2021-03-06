package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
	
	@Test
	public void getSymbolReturnsSymbol() {
		assertEquals(new Symbol("foo"), parameter.getSymbol());
	}
}
