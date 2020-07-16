package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumberTest {
    
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
    public void notEqualIntegerString() {
    	assertFalse(FplInteger.valueOf(1).equals("foo"));
    }

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

}
