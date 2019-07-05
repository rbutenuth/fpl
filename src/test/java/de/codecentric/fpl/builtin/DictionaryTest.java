package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;

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

}
