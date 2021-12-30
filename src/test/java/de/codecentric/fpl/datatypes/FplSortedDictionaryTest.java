package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.data.ScopeException;

public class FplSortedDictionaryTest {
	private FplSortedDictionary dict;

	public FplSortedDictionaryTest() {
		dict = new FplSortedDictionary(new Comparator<String>() {
			
			@Override
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});
	}
	
	@Test
	public void typeName() {
		assertEquals("sorted-dictionary", dict.typeName());
	}
	
	@Test
	public void keySetAndValues() {
		assertNull(dict.put("foo", new FplString("bar")));
		Set<String> keySet = dict.keySet();
		assertEquals(1, keySet.size());
		assertTrue(keySet.contains("foo"));
		Collection<FplValue> values = dict.values();
		assertEquals(1, values.size());
		assertTrue(values.contains(new FplString("bar")));
		Set<Entry<String, FplValue>> entries = dict.entrieSet();
		assertEquals(1, entries.size());
		Entry<String, FplValue> first = entries.iterator().next();
		assertEquals("foo", first.getKey());
	}
	
	@Test
	public void iterable() {
		assertNull(dict.put("foo", new FplString("bar")));
		Iterator<Entry<String, FplValue>> iterator = dict.iterator();
		assertTrue(iterator.hasNext());
		Entry<String, FplValue> next = iterator.next();
		assertEquals("foo", next.getKey());
		assertEquals("bar", ((FplString)next.getValue()).getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void simplePutGetAndRemove() {
		assertNull(dict.put("foo", new FplString("bar")));
		assertEquals("bar", ((FplString)dict.get("foo")).getContent());
		assertEquals("bar", ((FplString)dict.put("foo", null)).getContent());	
	}

	@Test
	public void assertPutNullKeyFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			dict.put(null, new FplString("foo"));
		});
	}

	@Test
	public void assertPutEmptyKeyFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			dict.put("", new FplString("foo"));
		});
	}

	@Test
	public void assertDefineNullValueFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			dict.define("foo", null);
		});
	}

	@Test
	public void assertDefineKeyExistsFails() throws ScopeException {
		dict.define("foo", new FplString("bar"));
		assertThrows(ScopeException.class, () -> {
			dict.define("foo", new FplString("bar"));
		});
	}

	@Test
	public void replaceExisting() {
		assertNull(dict.put("foo", new FplString("bar")));
		assertEquals(new FplString("bar"), dict.replace("foo", new FplString("baz")));
	}

	@Test
	public void replaceNotExisting() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			dict.replace("non-existing-key", new FplString("foo"));
		});
	}

}
