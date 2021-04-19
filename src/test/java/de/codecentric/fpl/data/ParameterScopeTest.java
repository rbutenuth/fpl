package de.codecentric.fpl.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.TestFunction;

public class ParameterScopeTest {
	Scope outer;
	ParameterScope inner;

	@BeforeEach
	public void before() {
		outer = new MapScope("outer");
		int i = 0;
		Map<String, Integer> parameterNames = new LinkedHashMap<>();
		parameterNames.put("a", i++);
		parameterNames.put("b", i++);
		FplValue[] parameters = { new FplString("foo"), new Symbol("bar") };
		inner = new ParameterScope("inner", outer, parameterNames, parameters);
	}

	@AfterEach
	public void after() {
		outer = null;
		inner = null;
	}

	@Test
	public void emptyScope() {
		assertEquals(new FplString("foo"), inner.getParameter(0));
		assertNull(outer.get("foo"));
	}

	@Test
	public void nesting() {
		assertTrue(inner.getNext() == outer);
	}

	@Test
	public void putInner() throws ScopeException {
		inner.put("foot", new FplString("baz"));
		assertEquals(new FplString("baz"), inner.get("foot"));
		assertNull(outer.get("foot"));
	}

	@Test
	public void putNullKey() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			inner.put(null, new FplString("foo"));
		});
	}

	@Test
	public void putEmptyKey() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			inner.put("", new FplString("foot"));
		});
	}

	@Test
	public void tryToChangeParameterValue() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			inner.put("a", new FplString("foot"));
		});
	}

	@Test
	public void changeInner() throws ScopeException {
		outer.put("key", new FplString("oldValue"));
		FplValue old = inner.replace("key", new FplString("newValue"));
		assertEquals("\"oldValue\"", old.toString());
		assertEquals("\"newValue\"", inner.get("key").toString());
		assertEquals("\"newValue\"", outer.get("key").toString());
	}

	@Test
	public void define() throws ScopeException {
		FplString euro = new FplString("€");
		inner.define("euro", euro);
		assertEquals(euro, inner.get("euro"));
	}

	@Test
	public void defineNamed() throws ScopeException, EvaluationException {
		TestFunction tf = new TestFunction("test-fun", false, "a", "b");
		inner.define(tf);
		assertTrue(tf == inner.get("test-fun"));
	}

	@Test
	public void entrySet() throws ScopeException {
		outer.put("d", new FplString("d"));
		inner.put("c", new FplString("c"));
		Set<Entry<String, FplValue>> set = inner.entrieSet();
		Iterator<Entry<String, FplValue>> iterator = set.iterator();
		checkIteratorContent(iterator);
	}

	@Test
	public void entryIterator() throws ScopeException {
		outer.put("d", new FplString("d"));
		inner.put("c", new FplString("c"));
		Iterator<Entry<String, FplValue>> iterator = inner.iterator();
		checkIteratorContent(iterator);
	}

	private void checkIteratorContent(Iterator<Entry<String, FplValue>> iterator) {
		int count = 0;
		while (iterator.hasNext()) {
			Entry<String, FplValue> entry = iterator.next();
			if (entry.getKey().equals("a")) {
				assertEquals("foo", ((FplString) entry.getValue()).getContent());
			}
			if (entry.getKey().equals("b")) {
				assertEquals(new Symbol("bar"), entry.getValue());
			}
			if (entry.getKey().equals("c")) {
				assertEquals("c", ((FplString) entry.getValue()).getContent());
			}
			count++;
		}
		assertEquals(3, count);
	}

	@Test
	public void keySet() throws ScopeException {
		outer.put("d", new FplString("d"));
		inner.put("c", new FplString("c"));
		Set<String> set = inner.keySet();
		assertEquals(3, set.size());
		assertTrue(set.contains("a"));
		assertTrue(set.contains("b"));
		assertTrue(set.contains("c"));
	}

	@Test
	public void values() throws ScopeException {
		outer.put("d", new FplString("d"));
		inner.put("c", new FplString("c"));
		Collection<FplValue> values = inner.values();
		assertEquals(3, values.size());
		assertTrue(values.contains(new FplString("foo")));
		assertTrue(values.contains(new Symbol("bar")));
		assertTrue(values.contains(new FplString("c")));
	}

	@Test
	public void parameterTypeName() {
		Parameter p = new Parameter(new Symbol("foo"), 0);
		assertEquals("parameter", p.typeName());
	}
}
