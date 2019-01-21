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

public class ObjectTest {
	MapScope outer;
	FplObject object;
	
	@Before
	public void before() throws EvaluationException {
		outer = new MapScope();
		object = new FplObject("object", outer);
	}

	@After
	public void after() {
		outer = null;
		object = null;
	}
	
	@Test
	public void testEmptyObject() {
		assertTrue(outer.allKeys().isEmpty());
		assertNull(object.get("foo"));
		assertEquals("object", object.getName());
		assertFalse(object.isSealed());
		object.setSealed(true);
		assertTrue(object.isSealed());
		assertTrue(outer == object.getNext());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullName() {
		new FplObject(null, outer);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyName() {
		new FplObject("", outer);
	}
}
