package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FplIntegerTest {

	@Test
	public void zero() {
		FplInteger a = FplInteger.valueOf(0);
		FplInteger b = FplInteger.valueOf(0);
		assertTrue(a == b); // must be cached
		assertEquals(a, b);
		assertEquals(0, a.getValue());
	}

	@Test
	public void smallerThanSmallesCacheValue() {
		FplInteger a = FplInteger.valueOf(-200);
		FplInteger b = FplInteger.valueOf(-200);
		assertTrue(a != b); // must not be cached
		assertEquals(a, b);
		assertEquals(-200, a.getValue());
	}
	
	@Test
	public void largerThanLargestCacheValue() {
		FplInteger a = FplInteger.valueOf(200);
		FplInteger b = FplInteger.valueOf(200);
		assertTrue(a != b); // must not be cached
		assertEquals(a, b);
		assertEquals(200, a.getValue());
	}
}
