package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplInteger;

public class ConditionalTest extends AbstractFplTest {

    @Test
    public void testIfElse() throws Exception {
    	// cover constructor
    	new Conditional();
    	
        assertEquals(FplInteger.valueOf(2), evaluate("if", "(if 1 2 3)"));
        assertEquals(FplInteger.valueOf(3), evaluate("else-0", "(if 0 2 3)"));
        assertEquals(FplInteger.valueOf(3), evaluate("else-nil", "(if nil 2 3)"));
        
        assertEquals(null, evaluate("if", "(if 1 nil 3)"));
        assertEquals(null, evaluate("if", "(if nil 3 nil)"));
    }
}
