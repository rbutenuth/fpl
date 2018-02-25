package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumberTest {
    
    @Test
    public void testHashCodeInteger() {
    	assertEquals(FplInteger.valueOf(1).hashCode(), FplInteger.valueOf(1).hashCode());
    }
    
    @Test
    public void testEqualInteger() {
    	assertTrue(FplInteger.valueOf(1).equals(FplInteger.valueOf(1)));
    }

    @Test
    public void testEqualSameInteger() {
    	FplInteger one = FplInteger.valueOf(1);
		assertTrue(one.equals(one));
    }

    @Test
    public void testNotEqualIntegerNull() {
    	assertFalse(FplInteger.valueOf(1).equals(null));
    }

    @Test
    public void testNotEqualIntegerString() {
    	assertFalse(FplInteger.valueOf(1).equals("foo"));
    }

    @Test
    public void testHashCodeDouble() {
    	assertEquals(new FplDouble(2).hashCode(), new FplDouble(2).hashCode());
    }
    
    @Test
    public void testEqualDouble() {
    	assertTrue(new FplDouble(1).equals(new FplDouble(1)));
    }

    @Test
    public void testEqualSameDouble() {
    	FplDouble one = new FplDouble(1);
		assertTrue(one.equals(one));
    }

    @Test
    public void testNotEqualDoubleNull() {
    	assertFalse(new FplDouble(1).equals(null));
    }

    @Test
    public void testNotEqualDoubleString() {
    	assertFalse(new FplDouble(1).equals("foo"));
    }

}
