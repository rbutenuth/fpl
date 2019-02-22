package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractClasspathResourceTest;
import de.codecentric.fpl.ListResultCallback;

public class ObjectTest extends AbstractClasspathResourceTest {
	private static String NL = System.lineSeparator();

	public ObjectTest() {
		super(ObjectTest.class);
	}

	@Test
	public void testEmpty() throws Exception {
		TestResults result = evaluate("empty-object.fpl");
		ListResultCallback callback = result.getCallback();
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		assertEquals("{" + NL + "}" + NL, object.toString());
	}
	
	@Test
	public void testSimple() throws Exception {
		TestResults result = evaluate("simple-object.fpl");
		ListResultCallback callback = result.getCallback();
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		FplObject v = (FplObject) values.get(0);
		assertEquals(2, v.allKeys().size());
		assertEquals("value", ((FplString)v.get("key")).getContent());
		assertEquals("another-value", ((FplString)v.get("another-key")).getContent());

		FplObject object = (FplObject) result.getEngine().getScope().get("obj");
		assertTrue(v == object);
		assertTrue(object.toString().contains("(def another-key \"another-value\")"));
		assertTrue(object.toString().contains("key: \"value\""));
	}
	
}
