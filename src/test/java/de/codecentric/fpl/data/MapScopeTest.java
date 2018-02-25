package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.datatypes.FplString;

public class MapScopeTest {
	MapScope outer;
	MapScope inner;
	
	@Before
	public void before() {
		outer = new MapScope();
		inner = new MapScope(outer);
	}

	@After
	public void after() {
		outer = null;
		inner = null;
	}
	
	@Test
	public void testEmptyScope() {
		assertTrue(outer.allKeys().isEmpty());
		assertNull(inner.get("foo"));
		assertNull(outer.get("foo"));
	}

	@Test
	public void testNesting() throws EvaluationException {
		assertTrue(inner.getNext() == outer);
		outer.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertEquals(new FplString("bar"), outer.get("foo"));
	}
	
	@Test
	public void testNestingOtherDirection() throws EvaluationException {
		assertTrue(inner.getNext() == outer);
		inner.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertNull(outer.get("foo"));
	}
	
	@Test
	public void testPutGlobal() throws EvaluationException {
		inner.putGlobal("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertEquals(new FplString("bar"), outer.get("foo"));
	}
	
	@Test
	public void testPutGlobalOuterSealed() throws EvaluationException {
		outer.setSealed(true);
		inner.putGlobal("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertNull(outer.get("foo"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testPutSealed() throws EvaluationException {
		outer.setSealed(true);
		assertTrue(outer.isSealed());
		outer.put("foo", new FplString("foo"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testPutGlobalSealed() throws EvaluationException {
		inner.setSealed(true);
		inner.putGlobal("foo", new FplString("bar"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testPutNullKey() throws EvaluationException {
		outer.put(null, new FplString("foo"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testPutEmptyKey() throws EvaluationException {
		outer.put("", new FplString("foo"));
	}

	@Test
	public void testAllKeys() throws EvaluationException {
		outer.put("b", new FplString("b"));
		outer.put("a", new FplString("a"));
		inner.put("c", new FplString("c"));
		List<String> keys = new ArrayList<>(inner.allKeys());
		assertEquals(3, keys.size());
		assertEquals("a", keys.get(0));
		assertEquals("b", keys.get(1));
		assertEquals("c", keys.get(2));
	}
}
