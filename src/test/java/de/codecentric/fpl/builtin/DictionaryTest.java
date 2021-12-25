package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplSortedDictionary;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class DictionaryTest extends AbstractFplTest {
	@Test
	public void dict() throws Exception {
		FplObject dict = (FplObject) evaluate("dict", "(dict \"a\" 1 \"b\" (+ 1 1) \"c\" 3)");
		assertNotNull(dict);
		assertEquals(1, ((FplInteger) dict.get("a")).getValue());
		assertEquals(2, ((FplInteger) dict.get("b")).getValue());
		assertEquals(3, ((FplInteger) dict.get("c")).getValue());
	}

	@Test
	public void dictNumberOfParametersMustBeEven() throws Exception {
		try {
			evaluate("dict", "(dict \"a\" 1 \"b\" 2 \"c\")");
			fail("Exception missing");
		} catch (EvaluationException e) {
			assertEquals("Number of parameters must be even", e.getMessage());
		}

	}

	@Test
	public void dictBadName() throws Exception {
		try {
			evaluate("dict", "(dict \"\" 1 \"b\" 2)");
			fail("Exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}

	@Test
	public void dictionaryPutAndGet() throws Exception {
		FplObject obj = (FplObject) evaluate("create", "(def obj (dict))");
		assertNull(evaluate("put", "(dict-put obj \"name\" 42)"));
		assertEquals(FplInteger.valueOf(42), obj.get("name"));
		assertEquals(FplInteger.valueOf(42), evaluate("get", "(dict-get obj \"name\")"));
	}

	@Test
	public void dictionaryDefineAndSetAndGet() throws Exception {
		FplObject obj = (FplObject) evaluate("create", "(def obj (dict))");
		assertEquals(FplInteger.valueOf(42), evaluate("def", "(dict-def obj \"name\" 42)"));
		assertEquals(FplInteger.valueOf(42), obj.get("name"));
		assertEquals(FplInteger.valueOf(42), evaluate("set", "(dict-set obj \"name\" 43)"));
		assertEquals(FplInteger.valueOf(43), obj.get("name"));
		assertEquals(FplInteger.valueOf(43), evaluate("get", "(dict-get obj \"name\")"));
	}

	@Test
	public void redefineFails() throws Exception {
		evaluate("create", "(def obj (dict))");
		evaluate("def", "(dict-def obj \"name\" 42)");
		try {
			evaluate("def", "(dict-def obj \"name\" 43)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Duplicate key: name", e.getMessage());
		}
	}

	@Test
	public void setOnNotExistingKeyFails() throws Exception {
		evaluate("create", "(def obj (dict))");
		try {
			evaluate("set", "(dict-set obj \"name\" 43)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No value with key name found", e.getMessage());
		}
	}

	@Test
	public void objectPutWithEmptyNameFails() throws Exception {
		try {
			evaluate("create", "(def obj (dict))");
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
		FplList keys = (FplList) evaluate("keys", "(dict-keys test-dict)");
		assertEquals(3, keys.size());
		Set<String> keysAsSet = new HashSet<>();
		for (FplValue key : keys) {
			keysAsSet.add(((FplString) key).getContent());
		}
		assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")), keysAsSet);
	}

	@Test
	public void dictionaryValues() throws Exception {
		createDictionary();
		FplList values = (FplList) evaluate("values", "(dict-values test-dict)");
		assertEquals(3, values.size());
		Set<Long> valuesAsSet = new HashSet<>();
		for (FplValue value : values) {
			valuesAsSet.add(((FplInteger) value).getValue());
		}
		assertEquals(new HashSet<>(Arrays.asList(1L, 2L, 3L)), valuesAsSet);
	}

	@Test
	public void dictionaryEntries() throws Exception {
		createDictionary();
		FplList entries = (FplList) evaluate("entries", "(dict-entries test-dict)");
		assertEquals(3, entries.size());
		for (FplValue entry : entries) {
			FplList list = (FplList) entry;
			assertEquals(2, list.size());
			String key = ((FplString) list.get(0)).getContent();
			long value = ((FplInteger) list.get(1)).getValue();
			assertEquals(key.charAt(0), 'a' + (char) value - 1);
		}
	}

	@Test
	public void sortedDictionary() throws Exception {
		FplSortedDictionary dict = (FplSortedDictionary) evaluate("sorted-dict",
				"(def sd (sorted-dict (lambda (a b) (if-else (lt a b) -1 (if-else (gt a b) 1 0))) \"b\" \"b\"))");
		evaluate("def a", "(dict-def sd \"a\" \"a\")");
		assertEquals(2, dict.keySet().size());
	}

	@Test
	public void sortedDictionaryWithNaturalStringOrder() throws Exception {
		FplSortedDictionary dict = (FplSortedDictionary) evaluate("sorted-dict",
				"(def sd (sorted-dict nil \"b\" \"b\"))");
		evaluate("def a", "(dict-def sd \"a\" \"a\")");
		assertEquals(2, dict.keySet().size());
	}

	@Test
	public void sortedDictionaryWrongNumberOfParameters() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("sorted-dict", "(sorted-dict (lambda (a b) (if-else (lt a b) -1 (if-else (gt a b) 1 0))) 42)");
		});
	}

	@Test
	public void sortedDictionaryWithBadKey() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> { evaluate("sorted-dict",
				"(def sd (sorted-dict (lambda (a b) (if-else (lt a b) -1 (if-else (gt a b) 1 0))) \"\" \"b\"))");
		});
		assertEquals("\"\" is not a valid name", e.getMessage());
	}

	@Test
	public void sortedDictionaryComparatorFails() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			FplSortedDictionary dict = (FplSortedDictionary) evaluate("sorted-dict", "(sorted-dict (lambda (a b) (/ 1 0)))");
			dict.put("a", new FplString("a"));
		});
		assertEquals("java.lang.ArithmeticException: / by zero", e.getMessage());
	}

	private void createDictionary() throws Exception {
		evaluate("create", "(def test-dict (dict \"a\" 1 \"b\" 2 \"c\" 3 ))");
	}
}
