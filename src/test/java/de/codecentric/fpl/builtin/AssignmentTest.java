package de.codecentric.fpl.builtin;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Tests for the functions "set", "set-global", "let" etc.
 */
public class AssignmentTest extends AbstractFplTest {

	@Test
	public void coverDefaultConstructor() throws Exception {
		new Assignment();
	}

	@Test
	public void simplePutAndGet() throws Exception {
		Scope local = new Scope(scope);
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
		try {
			evaluate(scope, "put", "(put nil 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("nil is not a valid name", e.getMessage());
		}
	}

	@Test
	public void putEmptyKeyThrowsException() throws Exception {
		try {
			evaluate(scope, "put", "(put \"\" 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}

	@Test
	public void simplePutGlobal() throws Exception {
		Scope local = new Scope(scope);
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
		try {
			evaluate(scope, "put-global", "(put-global nil 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("nil is not a valid name", e.getMessage());
		}
	}

	@Test
	public void putGlobalWithEmptyKeyThrowsException() throws Exception {
		try {
			evaluate(scope, "put-global", "(put-global \"\" 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}

	@Test
	public void putWithQuotedTarget() throws Exception {
		Scope local = new Scope(scope);
		assertNull(evaluate(local, "put", "(put (quote local) 20)"));
		assertEquals(FplInteger.valueOf(20), (local.get("local")));

		// Clear value
		assertEquals(FplInteger.valueOf(20), evaluate(local, "put", "(put local nil)"));
		assertNull(local.get("local"));
	}

	@Test(expected = EvaluationException.class)
	public void putWithTargetNotSymbolFails() throws Exception {
		Scope local = new Scope(scope);
		assertEquals(20, ((FplInteger) evaluate(local, "put", "(put 10 20)")).getValue());
	}

	@Test
	public void simpleDefGlobal() throws Exception {
		Scope local = new Scope(scope);
		assertEquals(FplInteger.valueOf(20), evaluate(local, "def-global", "(def-global global 20)"));

		// Value should be visible in local scope (via recursive get), and in global
		// scope
		assertEquals(FplInteger.valueOf(20), local.get("global"));
		assertEquals(FplInteger.valueOf(20), scope.get("global"));
	}

	@Test
	public void defGlobalWithEmptyKeyThrowsException() throws Exception {
		try {
			evaluate(scope, "def-global", "(def-global \"\" 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}

	@Test
	public void simpleSet() throws Exception {
		assertNull(evaluate("put", "(put key 10)"));
		assertEquals(FplInteger.valueOf(10), evaluate("set", "(set key 20)"));
		assertEquals(FplInteger.valueOf(20), scope.get("key"));
	}

	@Test
	public void setOnUndefinedFails() throws Exception {
		try {
			evaluate("set", "(set foo 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No value with key foo found", e.getMessage());
		}
	}

	@Test
	public void defWithEmptyKeyFails() throws Exception {
		try {
			evaluate("def", "(def \"\" 20)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
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
		try {
			evaluate("def-field", "(def-field key 10)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("No object found", e.getMessage());
		}
	}

	@Test
	public void defFieldNil() throws Exception {
		try {
			evaluate("def-field", "(def-class my-class (def-field foo nil) )");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("value is nil", e.getMessage());
		}
	}

	@Test
	public void objectPutAndGet() throws Exception {
		FplObject obj = (FplObject) evaluate("create", "(def obj { })");
		assertNull(evaluate("put", "(object-put obj name 42)"));
		assertEquals(FplInteger.valueOf(42), obj.get("name"));
		assertEquals(FplInteger.valueOf(42), evaluate("gut", "(object-get obj name)"));
	}

	@Test
	public void objectPutWithEmptyNameFails() throws Exception {
		try {
			evaluate("create", "(def obj { })");
			evaluate("put", "(object-put obj \"\" 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}
	
	@Test
	public void objectPutOnNotObjectFails() throws Exception {
		try {
			evaluate("put", "(object-put '(1 2 3) \"\" 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not an object: (1 2 3)", e.getMessage());
		}
	}
}
