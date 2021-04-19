package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.parser.Position;

public class SimpleObjectTest {
	private static String NL = System.lineSeparator();
	Scope outer;
	FplObject object;

	@BeforeEach
	public void before() throws EvaluationException {
		outer = new MapScope("outer");
		object = new FplObject("obj", new Position("object-test", 2, 3));
	}

	@AfterEach
	public void after() {
		outer = null;
		object = null;
	}

	@Test
	public void emptyObject() throws EvaluationException {
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
		assertEquals("{" + NL + "    foo: \"bar\"" + NL + "    one: {" + NL + "}" + NL + NL + "}" + NL,
				object.toString());
	}

	@Test
	public void evaluate() throws EvaluationException {
		assertTrue(object == object.evaluate(outer));
	}

	@Test
	public void nullName() {
		assertThrows(IllegalArgumentException.class, () -> {
			new FplObject(null);
		});
	}

	@Test
	public void emptyName() {
		assertThrows(IllegalArgumentException.class, () -> {
			new FplObject("");
		});
	}

	@Test
	public void withPositionNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			new FplObject("name", null);
		});
	}

	@Test
	public void nullPositionTwo() {
		assertThrows(IllegalArgumentException.class, () -> {
			new FplObject("scope-name", null, new MapScope("test"));
		});
	}

	@Test
	public void nullKey() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			object.put(null, FplInteger.valueOf(0));
		});
	}

	@Test
	public void emptyKey() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			object.put("", FplInteger.valueOf(0));
		});
	}

	@Test
	public void put() throws ScopeException {
		FplInteger one = FplInteger.valueOf(1);
		FplInteger two = FplInteger.valueOf(2);
		object.put("one", one);
		object.put("two", two);
		assertEquals(one, object.get("one"));
		object.put("one", null);
		assertNull(object.get("one"));
	}
}
