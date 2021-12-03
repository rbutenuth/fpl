package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;

public class FplAbstractFunctionTest extends AbstractFplTest {

	@Test
	public void nullStaysNull() throws EvaluationException {
		assertTrue(null == AbstractFunction.quote(null));
	}
	
	@Test
	public void quoteOfEvaluatesToThis() throws Exception {
		FplValue i = FplInteger.valueOf(42);
		assertTrue(i == AbstractFunction.quote(i));
	}
}
