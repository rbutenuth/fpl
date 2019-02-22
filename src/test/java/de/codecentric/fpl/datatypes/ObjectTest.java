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

public class ObjectTest extends AbstractFplTest {
	private static String NL = System.lineSeparator();

	public ObjectTest() {
		super(ObjectTest.class);
	}

	@Test
	public void testEmpty() throws Exception {
		ListResultCallback callback = evaluate("empty-object.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		assertEquals("{" + NL + "}" + NL, object.toString());
	}
	
	@Test
	public void testSimple() throws Exception {
		ListResultCallback callback = evaluate("simple-object.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		FplObject v = (FplObject) values.get(0);
		assertEquals(2, v.allKeys().size());
		assertEquals("value", ((FplString)v.get("key")).getContent());
		assertEquals("another-value", ((FplString)v.get("another-key")).getContent());

		FplObject object = (FplObject) scope.get("obj");
		assertTrue(v == object);
		assertTrue(object.toString().contains("(def another-key \"another-value\")"));
		assertTrue(object.toString().contains("key: \"value\""));
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
		ListResultCallback callback = evaluate("get-and-set.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		FplObject instance = (FplObject) values.get(1);
		assertNull(object.get("a"));
		assertNull(object.get("b"));
		assertEquals(FplInteger.valueOf(6), instance.get("a"));
		assertEquals(FplInteger.valueOf(7), instance.get("b"));
	}
	
}
