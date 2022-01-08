package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

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
	public void cond() throws Exception {
		assertEquals(FplInteger.valueOf(2), evaluate("cond", "(cond 1 2 3)"));
		assertEquals(FplInteger.valueOf(3), evaluate("cond", "(cond 0 2 1 3)"));
		assertEquals(FplInteger.valueOf(4), evaluate("cond", "(cond 0 2 0 3 4)"));
		assertNull(evaluate("cond", "(cond 0 2 0 3)"));
	}

	@Test
	public void sequential() throws Exception {
		assertEquals(FplInteger.valueOf(2), evaluate("sequential", "(sequential (+ 3 4) (if 1 2))"));
	}

	@Test
	public void synchronizedNotReallyParallel() throws Exception {
		assertEquals(FplInteger.valueOf(7), evaluate("synchronized", "(synchronized \"monitor\" (+ 3 4))"));
	}

	@Test
	public void scope() throws Exception {
		assertEquals(FplInteger.valueOf(2), evaluate("scope", "(scope (def foo 42) (if 1 2))"));
		assertNull(scope.get("foo"));
	}

	@Test
	public void scopeInFunction() throws Exception {
		evaluate("def", //
				"(def-function plus-mult (a b) " //
						+ "	(scope " //
						+ "		(+ a b) " //
						+ "		(* a b) " //
						+ "	) " //
						+ ") ");
		assertEquals(FplInteger.valueOf(12), evaluate("call", "(plus-mult 3 4)"));
	}

	@Test
	public void pipeline() throws Exception {
		evaluate("def", "(def-function plus-mult (a b) "
				+ "	(pipeline $ "
				+ "		(+ a b) "
				+ "		(* $ 10) "
				+ "	) "
				+ ") ");
		assertEquals(FplInteger.valueOf(70), evaluate("pipeline", "(plus-mult 3 4)"));
	}

	@Test
	public void sequnetialRecursive() throws Exception {
		evaluate("fibonacci", "(def-function fibonacci (n)\n" //
				+ "	(if-else (le n 2)\n" //
				+ "		1\n" //
				+ "		(reduce \n" //
				+ "			(lambda (acc value) (+ acc value))\n" //
				+ "			0\n" //
				+ "			(list (fibonacci (- n 1)) (fibonacci (- n 2)))\n" //
				+ "		)\n" //
				+ "	)\n" //
				+ ")");
		FplInteger fib10 = (FplInteger) evaluate("call-fib", "(fibonacci 10)");
		assertEquals(55, fib10.getValue());
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
		evaluate("catcher",
				"(def-function catcher (message id stack) (def-global log-message message)(def-global stack-trace stack))");
		evaluate("success-source", "(def-function success (x) x)");
		assertEquals(FplInteger.valueOf(42), evaluate("catch", "(try-catch (success 42) catcher)"));
		assertNull(scope.get("log-message"));
		assertNull(scope.get("stack-trace"));
	}

	@Test
	public void tryCatchWithException() throws Exception {
		evaluate("catcher",
				"(def-function catcher (message id stack) (def-global log-message message)(def-global stack-trace stack) 43)");
		evaluate("bam-source", "(def-function bam (x) (throw \"bam-message\"))");
		assertEquals(FplInteger.valueOf(43), evaluate("catch", "(try-catch (bam 1) catcher)"));
		FplString logMessage = (FplString) scope.get("log-message");
		assertEquals("bam-message", logMessage.getContent());
		FplList stackTrace = (FplList) scope.get("stack-trace");
		FplList entry = (FplList) stackTrace.get(1);
		assertEquals("catch", ((FplString) entry.get(0)).getContent());
		assertEquals(1, ((FplInteger) entry.get(1)).getValue());
		assertEquals("bam", ((FplString) entry.get(2)).getContent());
	}

	@Test
	public void tryCatchWithExceptionWithId() throws Exception {
		evaluate("catcher",
				"(def-function catcher (message id stack) (def-global log-message message)(def-global stack-trace stack)(def-global log-id id) 43)");
		evaluate("bam-source", "(def-function bam (x) (throw-with-id \"bam-message\" 404))");
		assertEquals(FplInteger.valueOf(43), evaluate("catch", "(try-catch (bam 1) catcher)"));
		FplString logMessage = (FplString) scope.get("log-message");
		assertEquals("bam-message", logMessage.getContent());
		FplList stackTrace = (FplList) scope.get("stack-trace");
		FplList entry = (FplList) stackTrace.get(1);
		assertEquals("catch", ((FplString) entry.get(0)).getContent());
		assertEquals(1, ((FplInteger) entry.get(1)).getValue());
		assertEquals("bam", ((FplString) entry.get(2)).getContent());
		FplInteger id = (FplInteger) scope.get("log-id");
		assertEquals(404, id.getValue());
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
				"(try-with 1 (+ 3 4) (lambda (message id stacktrace) message))");
		assertEquals("Not a list: 1", message.getContent());
	}

	@Test
	public void tryWithResourceListHasNotSizeThree() throws Exception {
		FplString message = (FplString) evaluate("try-with", //
				"(try-with ((a (open \"a\") )) (+ 3 4) (lambda (message id stacktrace) message))");
		assertEquals("resource must have size 3, but has size 2", message.getContent());
	}

	@Test
	public void tryWithResourceDuplicateResourceName() throws Exception {
		evaluate("open", "(def-function open (x) x)");

		FplString message = (FplString) evaluate("try-with", //
				"(try-with ((a (open \"a\") (lambda (x) (close x)))\n"
						+ "           (a (open \"b\") (lambda (x) (close x)))\n"
						+ "          ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (+ 3 4)) (lambda (message id stacktrace) message))");
		assertEquals("Duplicate key: a", message.getContent());
	}

	@Test
	public void tryWithNoException() throws Exception {
		evaluate("open", "(def-function open (x) x)");
		evaluate("close", "(def-function close (x) (put-global (symbol x) \"is closed\"))");

		FplValue result = evaluate("try-with", //
				"(try-with ((a (open \"a\") (lambda (x) (close x)))\n"
						+ "            (b (open \"b\") (lambda (x) (close x)))\n"
						+ "           ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (+ 3 4)) (lambda (message id stacktrace) (put-global \"message\" message)))");
		FplInteger i = (FplInteger) result;
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
				"(try-with ((a (open \"a\") (lambda (x) (close x)))\n"
						+ "           (b (open \"b\") (lambda (x) (close x)))\n"
						+ "          ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (throw \"bam\")) (lambda (message id stacktrace) (put-global \"message\" message) 8))");
		FplInteger i = (FplInteger) result;
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
				"(try-with ((a (open \"a\") (lambda (x) (close x)))\n"
						+ "           (b (open \"b\") (lambda (x) (close-crash x)))\n"
						+ "          ) (sequential (put-global \"a-in-code\" a) (put-global \"b-in-code\" b) (+ 3 4)) (lambda (message id stacktrace) (put-global \"message\" message)))");
		FplInteger i = (FplInteger) result;
		assertNull(scope.get("message"));
		assertEquals(7, i.getValue());
		assertEquals(new FplString("a"), scope.get("a-in-code"));
		assertEquals(new FplString("b"), scope.get("b-in-code"));
		assertEquals(new FplString("is closed"), scope.get("a"));
		assertNull(scope.get("b"));
	}
}
