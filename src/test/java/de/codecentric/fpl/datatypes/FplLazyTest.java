package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.list.FplList;

public class FplLazyTest extends AbstractFplTest {

	@Test
	public void evaluateLazyNull() throws Exception {
		FplValue lazy = FplLazy.make(scope, null);
		assertNull(lazy.evaluate(scope));
	}
	
	@Test
	public void makeEvaluatedOfEvaluatesToThis() {
		FplInteger i = FplInteger.valueOf(42);
		FplValue lazy = FplLazy.makeEvaluated(scope, i);
		assertTrue(i == lazy);
	}
	
	@Test
	public void makeEvaluatedOfLazy() throws Exception {
		FplValue lazy = FplLazy.make(scope, evaluate("quoted", "(quote (+ 1 2))"));
		FplValue lazy2 = FplLazy.makeEvaluated(scope, lazy);
		assertTrue(lazy == lazy2);
	}

	@Test
	public void makeEvaluatedOfNormalExpression() throws Exception {
		FplValue expression = FplList.fromValues(new Symbol("+"), FplInteger.valueOf(3), FplInteger.valueOf(4));
		FplValue lazy = FplLazy.makeEvaluated(scope, expression);
		assertTrue(expression == lazy.evaluate(scope));
		assertEquals("list", lazy.typeName());
	}
}
