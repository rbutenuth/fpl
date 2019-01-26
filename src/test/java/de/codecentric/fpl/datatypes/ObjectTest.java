package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.parser.Position;

public class ObjectTest {
	MapScope outer;
	FplObject object;
	
	@Before
	public void before() throws EvaluationException {
		outer = new MapScope();
		object = new FplObject(new Position("object-test", 2, 3));
	}

	@After
	public void after() {
		outer = null;
		object = null;
	}
	
	@Test
	public void testEmptyObject() throws EvaluationException {
		assertTrue(outer.allKeys().isEmpty());
		assertNull(object.get("foo"));
		assertEquals("object-test", object.getPosition().getName());
		assertFalse(object.isSealed());
		object.setSealed(true);
		assertTrue(object.isSealed());
		object.evaluate(outer);
		assertTrue(outer == object.getNext());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullName() {
		new FplObject(null);
	}
}
