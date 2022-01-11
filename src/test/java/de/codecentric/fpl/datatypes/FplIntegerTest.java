package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
	
    @Test
    public void hashCodeInteger() {
    	assertEquals(FplInteger.valueOf(1).hashCode(), FplInteger.valueOf(1).hashCode());
    }
    
    @Test
    public void equalInteger() {
    	assertTrue(FplInteger.valueOf(1).equals(FplInteger.valueOf(1)));
    }

    @Test
    public void notEqualInteger() {
    	assertFalse(FplInteger.valueOf(1).equals(FplInteger.valueOf(2)));
    }

    @Test
    public void equalSameInteger() {
    	FplInteger one = FplInteger.valueOf(1);
		assertTrue(one.equals(one));
    }

    @Test
    public void notEqualIntegerNull() {
    	assertFalse(FplInteger.valueOf(1).equals(null));
    }

    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void notEqualIntegerString() {
    	assertFalse(FplInteger.valueOf(1).equals("foo"));
    }
    
	@Test
	public void compareTo() {
		assertEquals(0, FplInteger.valueOf(0).compareTo(FplInteger.valueOf(0)));
		assertEquals(-1, FplInteger.valueOf(0).compareTo(FplInteger.valueOf(42)));
		assertEquals(1, FplInteger.valueOf(13).compareTo(FplInteger.valueOf(0)));
	}
}
