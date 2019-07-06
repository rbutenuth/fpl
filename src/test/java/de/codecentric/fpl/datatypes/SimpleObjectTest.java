package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.parser.Position;

public class SimpleObjectTest {
	private static String NL = System.lineSeparator();
	Scope outer;
	FplObject object;

	@Before
	public void before() throws EvaluationException {
		outer = new Scope("outer");
		object = new FplObject("obj", new Position("object-test", 2, 3));
	}

	@After
	public void after() {
		outer = null;
		object = null;
	}

	@Test
	public void emptyObject() throws EvaluationException {
		assertTrue(outer.isEmpty());
		assertNull(object.get("foo"));
		assertEquals("object-test", object.getPosition().getName());
		object.evaluate(outer);
		assertNull(object.getNext());
	}

	@Test
	public void entriesToString() throws Exception {
		object.put("foo", new FplString("bar"));
		object.put("one", FplInteger.valueOf(1));
		assertEquals("{" + NL + "    foo: \"bar\"" + NL + "    one: 1" + NL + "}" + NL, object.toString());
	}

	@Test
	public void nestedToString() throws Exception {
		object.put("foo", new FplString("bar"));
		object.put("one", new FplObject("nested"));
		assertEquals("{" + NL + "    foo: \"bar\"" + NL + "    one: <dictionary>" + NL + "}" + NL, object.toString());
	}

	@Test
	public void testEvaluate() throws EvaluationException {
		assertTrue(object == object.evaluate(outer));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullName() {
		new FplObject(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyName() {
		new FplObject("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withPositionNull() {
		new FplObject("name", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullPositionTwo() {
		new FplObject("scope-name", null, new Scope("test"));
	}

	@Test(expected = ScopeException.class)
	public void testNullKey() throws ScopeException {
		object.put(null, FplInteger.valueOf(0));
	}

	@Test(expected = ScopeException.class)
	public void testEmptyKey() throws ScopeException {
		object.put("", FplInteger.valueOf(0));
	}

	@Test
	public void testPut() throws ScopeException {
		FplInteger one = FplInteger.valueOf(1);
		FplInteger two = FplInteger.valueOf(2);
		object.put("one", one);
		object.put("two", two);
		assertEquals(one, object.get("one"));
		object.put("one", null);
		assertNull(object.get("one"));
	}
}
