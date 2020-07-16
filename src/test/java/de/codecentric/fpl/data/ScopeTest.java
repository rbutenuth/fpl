package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;

public class ScopeTest {
	Scope outer;
	Scope inner;
	
	@Before
	public void before() {
		outer = new Scope("outer");
		inner = new Scope("inner", outer);
	}

	@After
	public void after() {
		outer = null;
		inner = null;
	}
	
	@Test
	public void emptyScope() {
		assertTrue(outer.isEmpty());
		assertNull(inner.get("foo"));
		assertNull(outer.get("foo"));
		assertEquals(0, inner.size());
		assertEquals("Scope<outer>", outer.toString());
	}

	@Test
	public void nesting() throws ScopeException {
		assertTrue(inner.getNext() == outer);
		outer.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertEquals(new FplString("bar"), outer.get("foo"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void checkNestingWithNullParentThrowsException() throws ScopeException {
		new Scope("name", null);
	}
	
	@Test
	public void nestingOtherDirection() throws ScopeException {
		assertTrue(inner.getNext() == outer);
		inner.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertNull(outer.get("foo"));
	}
	
	@Test(expected = ScopeException.class)
	public void assertPutNullKeyFails() throws ScopeException {
		outer.put(null, new FplString("foo"));
	}
	
	@Test(expected = ScopeException.class)
	public void assertPutEmptyKeyFails() throws ScopeException {
		outer.put("", new FplString("foo"));
	}
	
	@Test(expected = ScopeException.class)
	public void assertDefineNullValueFails() throws ScopeException {
		outer.define(new Symbol("foo"), null);
	}
	
	@Test(expected = ScopeException.class)
	public void assertDefineNullKeyFails() throws ScopeException {
		outer.define(new Symbol(""), new FplString("bar"));
	}
	
	@Test
	public void changeNullKey() throws ScopeException {
		changeWithException(null, new FplString("newValue"), "nil is not a valid name");
	}
	
	@Test
	public void changeEmptyKey() throws ScopeException {
		changeWithException("", new FplString("newValue"), "\"\" is not a valid name");
	}
	
	@Test
	public void changeNullValue() throws ScopeException {
		changeWithException("someKey", null, "Change does not allow null values");
	}
	
	private void changeWithException(String key, FplValue value, String expected) {
		try {
			inner.replace(key, value);
			fail("missing exception");
		} catch (ScopeException e) {
			assertEquals(expected, e.getMessage());
		}
	}
	
	@Test
	public void changeOuter() throws ScopeException {
		outer.put("key", new FplString("oldValue"));
		FplValue old = inner.replace("key", new FplString("newValue"));
		assertEquals("\"oldValue\"", old.toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
	}

	@Test
	public void changeInner() throws ScopeException {
		inner.put("key", new FplString("oldValue"));
		FplValue old = inner.replace("key", new FplString("newValue"));
		assertEquals("\"oldValue\"", old.toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
		assertNull(outer.get("key"));
	}

	@Test(expected = ScopeException.class)
	public void changeNotExisting() throws ScopeException {
		inner.replace("non-existing-key", new FplString("foo"));
	}
	
	@Test
	public void exception() {
		ScopeException se = new ScopeException("huhu", new Error("bäm"));
		assertEquals("huhu", se.getMessage());
		assertEquals("bäm", se.getCause().getMessage());
		se = new ScopeException(new Error("bäm"));
		assertEquals("java.lang.Error: bäm", se.getMessage());
		assertEquals("bäm", se.getCause().getMessage());
	}
}
