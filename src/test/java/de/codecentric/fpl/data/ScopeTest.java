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

public class ScopeTest {
	Scope outer;
	Scope inner;
	
	@Before
	public void before() {
		outer = new Scope();
		inner = new Scope(outer);
	}

	@After
	public void after() {
		outer = null;
		inner = null;
	}
	
	@Test
	public void testEmptyScope() {
		assertTrue(outer.isEmpty());
		assertNull(inner.get("foo"));
		assertNull(outer.get("foo"));
	}

	@Test
	public void testNesting() throws ScopeException {
		assertTrue(inner.getNext() == outer);
		outer.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertEquals(new FplString("bar"), outer.get("foo"));
	}
	
	@Test
	public void testNestingOtherDirection() throws ScopeException {
		assertTrue(inner.getNext() == outer);
		inner.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertNull(outer.get("foo"));
	}
	
	@Test(expected = ScopeException.class)
	public void testPutNullKey() throws ScopeException {
		outer.put(null, new FplString("foo"));
	}
	
	@Test(expected = ScopeException.class)
	public void testPutEmptyKey() throws ScopeException {
		outer.put("", new FplString("foo"));
	}
	
	@Test
	public void testChangeNullKey() throws ScopeException {
		changeWithException(null, new FplString("newValue"), "key null or empty");
	}
	
	@Test
	public void testChangeEmptyKey() throws ScopeException {
		changeWithException("", new FplString("newValue"), "key null or empty");
	}
	
	@Test
	public void testChangeNullValue() throws ScopeException {
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
	public void testChangeOuter() throws ScopeException {
		outer.put("key", new FplString("oldValue"));
		FplValue old = inner.replace("key", new FplString("newValue"));
		assertEquals("\"oldValue\"", old.toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
	}

	@Test
	public void testChangeInner() throws ScopeException {
		inner.put("key", new FplString("oldValue"));
		FplValue old = inner.replace("key", new FplString("newValue"));
		assertEquals("\"oldValue\"", old.toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
		assertNull(outer.get("key"));
	}

	@Test(expected = ScopeException.class)
	public void testChangeNotExisting() throws ScopeException {
		inner.replace("non-existing-key", new FplString("foo"));
	}
	
	@Test
	public void testException() {
		ScopeException se = new ScopeException("huhu", new Error("bäm"));
		assertEquals("huhu", se.getMessage());
		assertEquals("bäm", se.getCause().getMessage());
		se = new ScopeException(new Error("bäm"));
		assertEquals("java.lang.Error: bäm", se.getMessage());
		assertEquals("bäm", se.getCause().getMessage());
	}
}
