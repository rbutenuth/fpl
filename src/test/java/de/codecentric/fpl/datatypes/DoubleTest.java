package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DoubleTest {
    

    @Test
    public void hashCodeDouble() {
    	assertEquals(new FplDouble(2).hashCode(), new FplDouble(2).hashCode());
    }
    
    @Test
    public void equalDouble() {
    	assertTrue(new FplDouble(1).equals(new FplDouble(1)));
    }

    @Test
    public void notEqualDouble() {
    	assertFalse(new FplDouble(1).equals(new FplDouble(2)));
    }

    @Test
    public void equalSameDouble() {
    	FplDouble one = new FplDouble(1);
		assertTrue(one.equals(one));
    }

    @Test
    public void notEqualDoubleNull() {
    	assertFalse(new FplDouble(1).equals(null));
    }

    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void notEqualDoubleString() {
    	assertFalse(new FplDouble(1).equals("foo"));
    }

	@Test
	public void compareTo() {
		assertEquals(0, new FplDouble(0).compareTo(new FplDouble(0)));
		assertEquals(-1, new FplDouble(0).compareTo(new FplDouble(42)));
		assertEquals(1, new FplDouble(13).compareTo(new FplDouble(0)));
	}
}
