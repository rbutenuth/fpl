package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringTest {
    
	@Test(expected = NullPointerException.class)
    public void testBadString() {
        new FplString(null);
    }
    
    @Test
    public void testHashCodeString() {
    	assertEquals(new FplString("aaa").hashCode(), new FplString("aaa").hashCode());
    }
    
    @Test
    public void testEqualString() {
    	assertTrue(new FplString("foo").equals(new FplString("foo")));
    }

    @Test
    public void testEqualSameString() {
    	FplString one = new FplString("bar");
		assertTrue(one.equals(one));
    }

    @Test
    public void testNotEqualStringNull() {
    	assertFalse(new FplString("x").equals(null));
    }

    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void testNotEqualStringInteger() {
    	assertFalse(new FplString("foo").equals(FplInteger.valueOf(1)));
    }

    @Test
    public void testToString() {
    	String test = "a\"b\t\n\r";
    	String result = "\"a\\\"b\\t\\n\\r\"";
    	assertEquals(result, new FplString(test).toString());
    }

}
