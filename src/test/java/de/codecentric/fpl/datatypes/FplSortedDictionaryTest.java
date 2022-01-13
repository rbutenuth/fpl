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
		dict = new FplSortedDictionary(new Comparator<FplValue>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(FplValue o1, FplValue o2) {
				return ((Comparable<FplValue>)o1).compareTo((FplValue)o2);
			}
		});
	}

	@Test
	public void typeName() {
		assertEquals("sorted-dictionary", dict.typeName());
	}

	@Test
	public void toStringTest() {
		assertEquals("{" + System.lineSeparator() + "}" + System.lineSeparator(), dict.toString());
	}

	@Test
	public void keySetAndValues() {
		assertNull(dict.put(new FplString("foo"), new FplString("bar")));
		Set<FplValue> keySet = dict.keySet();
		assertEquals(1, keySet.size());
		assertTrue(keySet.contains(new FplString("foo")));
		Collection<FplValue> values = dict.values();
		assertEquals(1, values.size());
		assertTrue(values.contains(new FplString("bar")));
		Set<Entry<FplValue, FplValue>> entries = dict.entrieSet();
		assertEquals(1, entries.size());
		Entry<FplValue, FplValue> first = entries.iterator().next();
		assertEquals(new FplString("foo"), first.getKey());
	}

	@Test
	public void iterable() {
		assertNull(dict.put(new FplString("foo"), new FplString("bar")));
		Iterator<Entry<FplValue, FplValue>> iterator = dict.iterator();
		assertTrue(iterator.hasNext());
		Entry<FplValue, FplValue> next = iterator.next();
		assertEquals(new FplString("foo"), next.getKey());
		assertEquals("bar", ((FplString) next.getValue()).getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void simplePutGetAndRemove() {
		assertNull(dict.put(new FplString("foo"), new FplString("bar")));
		assertEquals(new FplString("bar"), dict.get(new FplString("foo")));
		assertEquals(new FplString("bar"), dict.put(new FplString("foo"), null));
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
			dict.put(null, new FplString("foo"));
		});
	}

	@Test
	public void assertDefineNullValueFails() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			dict.define(new FplString("foo"), null);
		});
	}

	@Test
	public void assertDefineKeyExistsFails() throws ScopeException {
		dict.define(new FplString("foo"), new FplString("bar"));
		assertThrows(ScopeException.class, () -> {
			dict.define(new FplString("foo"), new FplString("bar"));
		});
	}

	@Test
	public void replaceExisting() {
		assertNull(dict.put(new FplString("foo"), new FplString("bar")));
		assertEquals(new FplString("bar"), dict.replace(new FplString("foo"), new FplString("baz")));
	}

	@Test
	public void replaceNotExisting() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			dict.replace(new FplString("non-existing-key"), new FplString("foo"));
		});
	}

}
