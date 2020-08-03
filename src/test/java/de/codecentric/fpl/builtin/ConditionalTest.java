package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;

public class ConditionalTest extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new Conditional();
	}

	@Test
    public void ifElse() throws Exception {
        assertEquals(FplInteger.valueOf(2), evaluate("if", "(if-else 1 2 3)"));
        assertEquals(FplInteger.valueOf(3), evaluate("else-0", "(if-else 0 2 3)"));
        assertEquals(FplInteger.valueOf(3), evaluate("else-nil", "(if-else nil 2 3)"));
        
        assertEquals(null, evaluate("if", "(if-else 1 nil 3)"));
        assertEquals(null, evaluate("if", "(if-else nil 3 nil)"));
    }

	@Test
    public void ifOnly() throws Exception {
        assertEquals(FplInteger.valueOf(2), evaluate("if", "(if 1 2)"));
        assertNull(evaluate("if", "(if 1 nil)"));
        assertNull(evaluate("else-0", "(if 0 2)"));
        assertNull(evaluate("else-nil", "(if nil 2)"));
        assertEquals(FplInteger.valueOf(1), evaluate("if", "(if \"foo\" 1)"));
        assertNull(evaluate("if", "(if \"\" 1)"));
        assertEquals(FplInteger.valueOf(1), evaluate("if", "(if (+ 1 1) 1)"));
        assertEquals(FplInteger.valueOf(1), evaluate("if", "(if (java-instance \"java.util.ArrayList\") 1)"));
	}
	
	@Test
    public void sequential() throws Exception {
		assertEquals(FplInteger.valueOf(2), 
				evaluate("sequential", "(sequential (+ 3 4) (if 1 2))"));
	}
	
	@Test
	public void simpleThrow() throws Exception {
		try {
			evaluate("throw", "(throw \"test-message\")");
			fail("Exception missing");
		} catch (EvaluationException e) {
			assertEquals("test-message", e.getMessage());
		}
	}
}
