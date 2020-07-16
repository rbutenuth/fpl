package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringTest {
    
	@Test(expected = NullPointerException.class)
    public void badString() {
        new FplString(null);
    }
    
    @Test
    public void hashCodeString() {
    	assertEquals(new FplString("aaa").hashCode(), new FplString("aaa").hashCode());
    }
    
    @Test
    public void equalString() {
    	assertTrue(new FplString("foo").equals(new FplString("foo")));
    }

    @Test
    public void equalSameString() {
    	FplString one = new FplString("bar");
		assertTrue(one.equals(one));
    }

    @Test
    public void notEqualStringNull() {
    	assertFalse(new FplString("x").equals(null));
    }

    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void notEqualStringInteger() {
    	assertFalse(new FplString("foo").equals(FplInteger.valueOf(1)));
    }

    @Test
    public void toStringWithSpecialCharacters() {
    	String test = "a\"b\t\n\r";
    	String result = "\"a\\\"b\\t\\n\\r\"";
    	assertEquals(result, new FplString(test).toString());
    }

    @Test
    public void makeWithNull() {
    	assertNull(FplString.make(null));
    }
    
    @Test
    public void makeWithContent() {
    	assertEquals("Huhu", FplString.make("Huhu").getContent());
    }
}
