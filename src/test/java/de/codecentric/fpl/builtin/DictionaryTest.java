package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class DictionaryTest extends AbstractFplTest {
	@Test
	public void dictionaryPutAndGet() throws Exception {
		FplObject obj = (FplObject) evaluate("create", "(def obj { })");
		assertNull(evaluate("put", "(dict-put obj name 42)"));
		assertEquals(FplInteger.valueOf(42), obj.get("name"));
		assertEquals(FplInteger.valueOf(42), evaluate("get", "(dict-get obj name)"));
	}

	@Test
	public void dictionaryDefineAndSetAndGet() throws Exception {
		FplObject obj = (FplObject) evaluate("create", "(def obj { })");
		assertEquals(FplInteger.valueOf(42), evaluate("def", "(dict-def obj name 42)"));
		assertEquals(FplInteger.valueOf(42), obj.get("name"));
		assertEquals(FplInteger.valueOf(42), evaluate("set", "(dict-set obj name 43)"));
		assertEquals(FplInteger.valueOf(43), obj.get("name"));
		assertEquals(FplInteger.valueOf(43), evaluate("get", "(dict-get obj name)"));
	}

	@Test
	public void redefineFails() throws Exception {
		evaluate("create", "(def obj { })");
		evaluate("def", "(dict-def obj name 42)");
		try {
			evaluate("def", "(dict-def obj name 43)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Duplicate key: name", e.getMessage());
		}
	}
	
	@Test
	public void setOnNotExistingKeyFails() throws Exception {
		evaluate("create", "(def obj { })");
		try {
			evaluate("set", "(dict-set obj name 43)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No value with key name found", e.getMessage());
		}
	}
	
	@Test
	public void objectPutWithEmptyNameFails() throws Exception {
		try {
			evaluate("create", "(def obj { })");
			evaluate("put", "(dict-put obj \"\" 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}
	
	@Test
	public void objectPutOnNotObjectFails() throws Exception {
		try {
			evaluate("put", "(dict-put '(1 2 3) \"\" 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not a dictionary: (1 2 3)", e.getMessage());
		}
	}

	@Test
	public void dictionaryKeys() throws Exception {
		createDictionary();
		FplList keys = (FplList) evaluate("keys", "(dict-keys dict)");
		assertEquals(3, keys.size());
		Set<String> keysAsSet = new HashSet<>();
		for (FplValue key : keys) {
			keysAsSet.add(((FplString)key).getContent());
		}
		assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")), keysAsSet);
	}
	
	@Test
	public void dictionaryValues() throws Exception {
		createDictionary();
		FplList values = (FplList) evaluate("values", "(dict-values dict)");
		assertEquals(3, values.size());
		Set<Long> valuesAsSet = new HashSet<>();
		for (FplValue value : values) {
			valuesAsSet.add(((FplInteger)value).getValue());
		}
		assertEquals(new HashSet<>(Arrays.asList(1L, 2L, 3L)), valuesAsSet);
	}

	@Test
	public void dictionaryEntries() throws Exception {
		createDictionary();
		FplList entries = (FplList) evaluate("entries", "(dict-entries dict)");
		assertEquals(3, entries.size());
		for (FplValue entry : entries) {
			FplList list = (FplList)entry;
			assertEquals(2, list.size());
			String key = ((FplString)list.get(0)).getContent();
			long value = ((FplInteger)list.get(1)).getValue();
			assertEquals(key.charAt(0), 'a' + (char)value - 1);
		}
	}
	
	private void createDictionary() throws Exception {
		evaluate("create", "(def dict { a: 1 b: 2 c: 3 })");
	}
}
