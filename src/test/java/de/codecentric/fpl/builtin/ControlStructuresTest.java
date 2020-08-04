package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.list.FplList;

public class ControlStructuresTest extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new ControlStructures();
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
	

	@Test
	public void tryCatchSuccess() throws Exception {
		evaluate("catcher", "(def-function catcher (message stack) (def-global log-message message)(def-global stack-trace stack))");
		evaluate("success-source", "(def-function success (x) x)");
		assertEquals(FplInteger.valueOf(42), evaluate("catch", "(try-catch (success 42) catcher)"));
		assertNull(scope.get("log-message"));
		assertNull(scope.get("stack-trace"));
	}
	
	@Test
	public void tryCatchWithException() throws Exception {
		evaluate("catcher", "(def-function catcher (message stack) (def-global log-message message)(def-global stack-trace stack) 43)");
		evaluate("bam-source", "(def-function bam (x) (throw \"bam-message\"))");
		assertEquals(FplInteger.valueOf(43), evaluate("catch", "(try-catch (bam 1) catcher)"));
		FplString logMessage = (FplString) scope.get("log-message");
		assertEquals("bam-message", logMessage.getContent());
		FplList stackTrace = (FplList) scope.get("stack-trace");
		FplList entry = (FplList) stackTrace.get(1);
		assertEquals("bam-source", ((FplString) entry.get(0)).getContent());
		assertEquals(1, ((FplInteger) entry.get(1)).getValue());
		assertEquals("bam", ((FplString)entry.get(2)).getContent());
	}
	
	@Test
	public void tryCatchWithExceptionCatchFunctionIsNull() throws Exception {
		evaluate("bam-source", "(def-function bam (x) (throw \"bam-message\"))");
		assertNull(evaluate("catch", "(try-catch (bam 1) nil)"));
	}
}
