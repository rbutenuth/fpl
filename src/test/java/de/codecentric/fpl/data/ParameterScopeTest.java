package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;

public class ParameterScopeTest {
	Scope outer;
	ParameterScope inner;

	@Before
	public void before() {
		outer = new MapScope("outer");
		int i = 0;
		Map<String, Integer> parameterNames = new LinkedHashMap<>();
		parameterNames.put("a", i++);
		parameterNames.put("b", i++);
		FplValue[] parameters = { new FplString("foo"), new Symbol("bar") };
		inner = new ParameterScope("inner", outer, parameterNames, parameters);
	}

	@After
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

	@Test(expected = ScopeException.class)
	public void putNullKey() throws ScopeException {
		inner.put(null, new FplString("foo"));
	}

	@Test(expected = ScopeException.class)
	public void putEmptyKey() throws ScopeException {
		inner.put("", new FplString("foot"));
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
	public void parameterTypeName() {
		Parameter p = new Parameter(new Symbol("foo"), 0);
		assertEquals("parameter", p.typeName());
	}
}
