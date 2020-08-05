package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
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
		try {
			evaluate("catch", "(try-catch (bam 1) nil)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("bam-message", e.getMessage());
		}
	}
	
	@Test
	public void tryWithResourceNotList() throws Exception {
		FplString message = (FplString) evaluate("try-with", //
				"(try-with 1 (+ 3 4) (lambda (message stacktrace) message))");
		assertEquals("Not a list: 1", message.getContent());
	}

	@Test
	public void tryWithResourceListHasNotSizeThree() throws Exception {
		FplString message = (FplString) evaluate("try-with", //
				"(try-with '((a (open \"a\") )) (+ 3 4) (lambda (message stacktrace) message))");
		assertEquals("resource must have size 3, but has size 2", message.getContent());
	}

	@Test
	public void tryWithResourceDuplicateResourceName() throws Exception {
		evaluate("open", "(def-function open (x) x)");
		
		FplString message = (FplString) evaluate("try-with", //
				"(try-with '((a (open \"a\") (lambda (x) (close x)))\n" + 
				"            (a (open \"b\") (lambda (x) (close x)))\n" + 
				"           ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (+ 3 4)) (lambda (message stacktrace) message))");
		assertEquals("Duplicate key: a", message.getContent());
	}

	@Test
	public void tryWithNoException() throws Exception {
		evaluate("open", "(def-function open (x) x)");
		evaluate("close", "(def-function close (x) (put-global (symbol x) \"is closed\"))");
		
		FplValue result = evaluate("try-with", //
				"(try-with '((a (open \"a\") (lambda (x) (close x)))\n" + 
				"            (b (open \"b\") (lambda (x) (close x)))\n" + 
				"           ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (+ 3 4)) (lambda (message stacktrace) (put-global \"message\" message)))");
		FplInteger i = (FplInteger)result;
		assertNull(scope.get("message"));
		assertEquals(7, i.getValue());
		assertEquals(new FplString("a"), scope.get("a-in-code"));
		assertEquals(new FplString("b"), scope.get("b-in-code"));
		assertEquals(new FplString("is closed"), scope.get("a"));
		assertEquals(new FplString("is closed"), scope.get("b"));
	}
	
	@Test
	public void tryWithException() throws Exception {
		evaluate("open", "(def-function open (x) x)");
		evaluate("close", "(def-function close (x) (put-global (symbol x) \"is closed\"))");
		
		FplValue result = evaluate("try-with", //
				"(try-with '((a (open \"a\") (lambda (x) (close x)))\n" + 
				"            (b (open \"b\") (lambda (x) (close x)))\n" + 
				"           ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (throw \"bam\")) (lambda (message stacktrace) (put-global \"message\" message) 8))");
		FplInteger i = (FplInteger)result;
		assertEquals(new FplString("bam"), scope.get("message"));
		assertEquals(8, i.getValue());
		assertEquals(new FplString("a"), scope.get("a-in-code"));
		assertEquals(new FplString("b"), scope.get("b-in-code"));
		assertEquals(new FplString("is closed"), scope.get("a"));
		assertEquals(new FplString("is closed"), scope.get("b"));
	}
	@Test
	public void tryWithExceptionInFinalizer() throws Exception {
		evaluate("open", "(def-function open (x) x)");
		evaluate("close", "(def-function close (x) (put-global (symbol x) \"is closed\"))");
		evaluate("close-crash", "(def-function close-crash (x) (throw \"bam\"))");
		
		FplValue result = evaluate("try-with", //
				"(try-with '((a (open \"a\") (lambda (x) (close x)))\n" + 
				"            (b (open \"b\") (lambda (x) (close-crash x)))\n" + 
				"           ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (+ 3 4)) (lambda (message stacktrace) (put-global \"message\" message)))");
		FplInteger i = (FplInteger)result;
		assertNull(scope.get("message"));
		assertEquals(7, i.getValue());
		assertEquals(new FplString("a"), scope.get("a-in-code"));
		assertEquals(new FplString("b"), scope.get("b-in-code"));
		assertEquals(new FplString("is closed"), scope.get("a"));
		assertNull(scope.get("b"));
	}
}
