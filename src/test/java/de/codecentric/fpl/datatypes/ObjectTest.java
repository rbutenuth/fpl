package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractClasspathResourceTest;
import de.codecentric.fpl.ListResultCallback;

public class ObjectTest extends AbstractClasspathResourceTest {

	public ObjectTest() {
		super(ObjectTest.class);
	}

	@Test
	public void testSimple() throws Exception {
		TestResults result = evaluate("simple-object.fpl");
		ListResultCallback callback = result.getCallback();
		assertFalse(callback.hasException());
		List<FplValue> values = callback.getResults();
		assertEquals(1, values.size());
		FplObject v = (FplObject) values.get(0);
		assertEquals(1, v.allKeys().size());
		assertEquals("value", ((FplString)v.get("key")).getContent());
	}
}
