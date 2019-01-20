package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;

public class ParameterScopeTest {
	MapScope outer;
	ParameterScope inner;
	
	@Before
	public void before() {
		outer = new MapScope();
		inner = new ParameterScope(outer, 2);
		inner.setParameter(0, new FplString("foo"));
		inner.setParameter(1, new Symbol("bar"));
	}

	@After
	public void after() {
		outer = null;
		inner = null;
	}
	
	@Test(expected = NullPointerException.class)
	public void testNextNull() {
		new ParameterScope(null, 1);
	}
			
	@Test
	public void testEmptyScope() {
		assertTrue(outer.allKeys().isEmpty());
		assertEquals(new FplString("foo"), inner.getParameter(0));
		assertNull(outer.get("foo"));
		assertFalse(inner.isSealed());
	}

	@Test
	public void testNesting() throws EvaluationException {
		assertTrue(inner.getNext() == outer);
	}
	
	@Test
	public void testPut() throws EvaluationException {
		inner.put("foot", new FplString("baz"));
		assertEquals(new FplString("baz"), inner.get("foot"));
		assertEquals(new FplString("baz"), outer.get("foot"));
	}
	
	@Test
	public void testPutGlobal() throws EvaluationException {
		inner.putGlobal("foot", new FplString("baz"));
		assertEquals(new FplString("baz"), inner.get("foot"));
		assertEquals(new FplString("baz"), outer.get("foot"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testPutNullKey() throws EvaluationException {
		inner.put(null, new FplString("foo"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testPutEmptyKey() throws EvaluationException {
		inner.put("", new FplString("foot"));
	}
	
	@Test
	public void testChangeInner() throws EvaluationException {
		outer.put("key", new FplString("oldValue"));
		FplValue old = inner.change("key", new FplString("newValue"));
		assertEquals("\"oldValue\"", old.toString());
		assertEquals("\"oldValue\"", inner.get("key").toString());
		assertEquals("\"oldValue\"", outer.get("key").toString());
	}
}
