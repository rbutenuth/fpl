package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.data.Scope;

public class ObjectTest extends AbstractFplTest {
	private static String NL = System.lineSeparator();

	public ObjectTest() {
		super(ObjectTest.class);
	}

	@Test
	public void testEmpty() throws Exception {
		ListResultCallback callback = evaluateResource("empty-object.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		assertEquals("{" + NL + "}" + NL, object.toString());
	}
	
	@Test
	public void testSimple() throws Exception {
		ListResultCallback callback = evaluateResource("simple-object.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		FplObject v = (FplObject) values.get(0);
		assertEquals("object", v.typeName());
		assertEquals(2, v.size());
		FplInteger sum = (FplInteger) values.get(1);
		assertEquals(FplInteger.valueOf(7), sum);
	}
	
	@Test
	public void testSetOddNumberOfParameters() throws Exception {
		try {
			evaluate("", "(instance a 3 b)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Number of parameters must be even", e.getMessage());
		}
	}

	@Test
	public void testGetAndSet() throws Exception {
		ListResultCallback callback = evaluateResource("get-and-set.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		FplObject instance = (FplObject) values.get(1);
		assertNull(object.get("a"));
		assertNull(object.get("b"));
		assertEquals(FplInteger.valueOf(6), instance.get("a"));
		assertEquals(FplInteger.valueOf(7), instance.get("b"));
	}
	
	@Test
	public void testConstructor() throws Exception {
		ListResultCallback callback = evaluateResource("constructor.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		assertTrue(values.get(0) instanceof FplObject);
		FplObject instance = (FplObject) values.get(1);
		assertEquals("Wert", ((FplString)instance.get("key")).getContent());
		assertEquals("anders", ((FplString)instance.get("other")).getContent());
	}
	
	@Test
	public void testCallMethodFromFunction() throws Exception {
		ListResultCallback callback = evaluateResource("method-called-from-function.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(3, values.size());
		assertTrue(values.get(0) instanceof FplObject);
		FplObject instance = (FplObject) values.get(2);
		assertEquals("Wert", ((FplString)instance.get("key")).getContent());
	}
	
	@Test
	public void testObjectWithNestedObject() throws Exception {
		ListResultCallback callback = evaluateResource("object-with-nested-object.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(1, values.size());
		Scope global = engine.getScope();
		assertEquals("inner-value", ((FplString)global.get("by-inner")).getContent());
		assertEquals("outer-value", ((FplString)global.get("by-outer")).getContent());
	}
	
	@Test
	public void testEvaluateFailsWhenNotAFunction() throws Exception {
		try {
			evaluate("not-a-function", "((instance method 1) method non-sence-parameter)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("Not a function: 1", e.getMessage());
		}
	}
	
	@Test
	public void testNullEvaluatesToNull() throws Exception {
		assertNull(evaluate("not-a-function", "((instance method 1) foo)"));
	}
}
