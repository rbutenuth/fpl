package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;

public class ParameterTest {
	private Parameter parameter = new Parameter(new Symbol("foo"), 0);
	
	@Test
	public void getNameReturnsName() {
		assertEquals("foo", parameter.getName());
	}
	
	@Test
	public void toStringReturnsName() {
		assertEquals("foo", parameter.toString());
	}
	
	@Test
	public void getSymbolReturnsSymbol() {
		assertEquals(new Symbol("foo"), parameter.getSymbol());
	}
	
	@Test void inNestedScope() {
		Map<String, Integer> nameToIndex = new HashMap<>();
		nameToIndex.put("foo", 0);
		ParameterScope paramScope = new ParameterScope("p-scope", null, nameToIndex, new FplValue[] { new FplString("baz") });
		Scope scope = new MapScope("inner", paramScope);
		assertEquals(new FplString("baz"), parameter.evaluate(scope));
	}

	@Test void inScopeWithoutOterParameterScope() {
		Scope scope = new MapScope("inner", null);
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			parameter.evaluate(scope);
		});
		assertEquals("not nested in ParameterScope", e.getMessage());
	}
}
