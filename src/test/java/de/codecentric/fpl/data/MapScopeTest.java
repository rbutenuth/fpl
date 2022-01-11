package de.codecentric.fpl.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

public class MapScopeTest {
	private MapScope outer;
	private MapScope inner;

	@BeforeEach
	public void before() {
		outer = new MapScope("outer");
		inner = new MapScope("inner", outer);
	}

	@AfterEach
	public void after() {
		outer = null;
		inner = null;
	}

	@Test
	public void emptyScope() {
		assertNull(inner.get("foo"));
		assertNull(outer.get("foo"));
		assertEquals("MapScope<outer>", outer.toString());
		assertTrue(inner.keySet().isEmpty());
		assertTrue(outer.keySet().isEmpty());
		assertTrue(inner.entrieSet().isEmpty());
		assertTrue(outer.entrieSet().isEmpty());
	}

	@Test
	public void def() {
		inner.define("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		Set<String> keys = inner.keySet();
		assertEquals(1, keys.size());
		assertTrue(keys.contains("foo"));
	}
	
	@Test
	public void nesting() throws ScopeException {
		assertTrue(inner.getNext() == outer);
		outer.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertEquals(new FplString("bar"), outer.get("foo"));
	}

	@Test
	public void nestingOtherDirection() throws ScopeException {
		assertTrue(inner.getNext() == outer);
		inner.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), inner.get("foo"));
		assertNull(outer.get("foo"));
	}

	@Test
	public void assertPutNullKeyFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			outer.put(null, new FplString("foo"));
		});
	}

	@Test
	public void assertPutEmptyKeyFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			outer.put("", new FplString("foo"));
		});
	}

	@Test
	public void assertDefineNullValueFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			outer.define("foo", null);
		});
	}

	@Test
	public void assertDefineNullKeyFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			outer.define("", new FplString("bar"));
		});
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
		changeWithException("someKey", null, "value is nil");
	}

	private void changeWithException(String key, FplValue value, String expected) {
		ScopeException e = assertThrows(ScopeException.class, () -> {
			inner.replace(key, value);
		});
		assertEquals(expected, e.getMessage());
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

	@Test
	public void changeNotExisting() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			inner.replace("non-existing-key", new FplString("foo"));
		});
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
