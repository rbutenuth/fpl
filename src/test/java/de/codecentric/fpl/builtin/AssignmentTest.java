package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Tests for the functions "set", "set-global", "let" etc.
 */
public class AssignmentTest extends AbstractFplTest {
	private static final FplInteger TRUE = FplInteger.valueOf(1);
	private static final FplInteger FALSE = FplInteger.valueOf(0);

	@Test
	public void coverDefaultConstructor() throws Exception {
		new Assignment();
	}

	@Test
	public void simplePutAndGet() throws Exception {
		Scope local = new MapScope("local", scope);
		assertNull(scope.get("local"));
		assertNull(local.get("local"));
		assertNull(evaluate(local, "put", "(put local 20)"));
		// Value should be visible in local scope, but not in global scope
		assertEquals(FplInteger.valueOf(20), local.get("local"));
		assertNull(scope.get("local"));

		// Clear value
		assertEquals(FplInteger.valueOf(20), evaluate(local, "put", "(put local nil)"));
		assertNull(local.get("local"));
	}

	@Test
	public void putWithNullKeyThrowsException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate(scope, "put", "(put nil 20)");
		});
		assertEquals("nil is not a valid name", e.getMessage());
	}

	@Test
	public void putEmptyKeyThrowsException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate(scope, "put", "(put \"\" 20)");
		});
		assertEquals("\"\" is not a valid name", e.getMessage());
	}
	
	@Test
	public void simplePutDoesNotMatch() throws Exception {
		assertEquals(FALSE, evaluate("no match", "(match-put (a b) '(42))"));
		assertNull(scope.get("a"));
		assertNull(scope.get("b"));
	}
	
	@Test
	public void putDoesNotMatch() throws Exception {
		assertEquals(FALSE, evaluate("no match", "(match-put (a (b)) '(42 43))"));
		assertNull(scope.get("a"));
		assertNull(scope.get("b"));
	}
	
	@Test
	public void putDoesNotMatch2() throws Exception {
		assertEquals(FALSE, evaluate("no match", "(match-put (a (b c)) '(42 (43)))"));
		assertNull(scope.get("a"));
		assertNull(scope.get("b"));
		assertNull(scope.get("c"));
	}
	
	@Test
	public void simplePutMatch() throws Exception {
		assertEquals(TRUE, evaluate("simple match", "(match-put (a b) '(42 43))"));
		assertEquals(FplInteger.valueOf(42), scope.get("a"));
		assertEquals(FplInteger.valueOf(43), scope.get("b"));
	}

	@Test
	public void putMatch() throws Exception {
		assertEquals(TRUE, evaluate("simple match", "(match-put (a (b c)) '(42 (43 44)))"));
		assertEquals(FplInteger.valueOf(42), scope.get("a"));
		assertEquals(FplInteger.valueOf(43), scope.get("b"));
		assertEquals(FplInteger.valueOf(44), scope.get("c"));
	}

	@Test
	public void simplePutGlobal() throws Exception {
		Scope local = new MapScope("local", scope);
		assertNull(scope.get("global"));
		assertNull(local.get("global"));
		assertNull(evaluate(local, "put-global", "(put-global global 20)"));

		// Value should be visible in local scope (via recursive get), and in global
		// scope
		assertEquals(FplInteger.valueOf(20), local.get("global"));
		assertEquals(FplInteger.valueOf(20), scope.get("global"));

		// Changing it in local should only affect the local scope
		assertEquals(FplInteger.valueOf(20), evaluate(local, "set", "(set global 30)"));
		assertEquals(FplInteger.valueOf(30), local.get("global"));
		assertEquals(FplInteger.valueOf(30), scope.get("global"));

		assertEquals(FplInteger.valueOf(30), evaluate(local, "get", "global"));
		assertEquals(FplInteger.valueOf(30), evaluate(scope, "get", "global"));
	}

	@Test
	public void putGlobalWithNullKeyThrowsException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate(scope, "put-global", "(put-global nil 20)");
		});
		assertEquals("nil is not a valid name", e.getMessage());
	}

	@Test
	public void putGlobalWithEmptyKeyThrowsException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate(scope, "put-global", "(put-global \"\" 20)");
		});
		assertEquals("\"\" is not a valid name", e.getMessage());
	}

	@Test
	public void putWithQuotedTarget() throws Exception {
		Scope local = new MapScope("local", scope);
		assertNull(evaluate(local, "put", "(put (quote local) 20)"));
		assertEquals(FplInteger.valueOf(20), (local.get("local")));

		// Clear value
		assertEquals(FplInteger.valueOf(20), evaluate(local, "put", "(put local nil)"));
		assertNull(local.get("local"));
	}

	@Test
	public void putWithTargetNotSymbolFails() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			Scope local = new MapScope("local", scope);
			assertEquals(20, ((FplInteger) evaluate(local, "put", "(put 10 20)")).getValue());
		});
	}

	@Test
	public void simpleDefGlobal() throws Exception {
		Scope local = new MapScope("local", scope);
		assertEquals(FplInteger.valueOf(20), evaluate(local, "def-global", "(def-global global 20)"));

		// Value should be visible in local scope (via recursive get), and in global
		// scope
		assertEquals(FplInteger.valueOf(20), local.get("global"));
		assertEquals(FplInteger.valueOf(20), scope.get("global"));
	}

	@Test
	public void defGlobalWithEmptyKeyThrowsException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate(scope, "def-global", "(def-global \"\" 20)");
		});
		assertEquals("\"\" is not a valid name", e.getMessage());
	}

	@Test
	public void simpleSet() throws Exception {
		assertNull(evaluate("put", "(put key 10)"));
		assertEquals(FplInteger.valueOf(10), scope.get("key"));
		assertEquals(FplInteger.valueOf(10), evaluate("set", "(set key 20)"));
		assertEquals(FplInteger.valueOf(20), scope.get("key"));
	}

	@Test
	public void setOnUndefinedFails() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("set", "(set foo 20)");
		});
		assertEquals("No value with key foo found", e.getMessage());
	}

	@Test
	public void defWithEmptyKeyFails() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("def", "(def \"\" 20)");
		});
		assertEquals("\"\" is not a valid name", e.getMessage());
	}

	@Test
	public void simpleDef() throws Exception {
		assertEquals(10, ((FplInteger) evaluate("def", "(def key 10)")).getValue());
		assertEquals(10, ((FplInteger) scope.get("key")).getValue());
	}

	@Test
	public void defField() throws Exception {
		ListResultCallback callback = evaluateResource("def-field.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		assertEquals(new FplString("bar"), object.get("foo"));
	}

	@Test
	public void defFieldNoObject() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("def-field", "(def-field key 10)");
		});
		assertEquals("No object found", e.getMessage());
	}

	@Test
	public void defFieldNil() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("def-field", "(def-class my-class (def-field foo nil) )");
		});
		assertEquals("value is nil", e.getMessage());
	}

	@Test
	public void assignmentToParameterFails() throws Exception {
		evaluate("def-function", "(def-function f (a) (put a 2))");
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("call-function", "(f 3)");
		});
		assertEquals("Parameter a can't be a target.", e.getMessage());
	}
}
