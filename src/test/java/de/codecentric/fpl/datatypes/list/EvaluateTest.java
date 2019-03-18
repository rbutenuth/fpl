package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;

public class EvaluateTest extends AbstractFplTest {

	@Test
	public void testNotAFunction() throws Exception {
		try {
			evaluate("error", "(1 2)");
		} catch (EvaluationException e) {
			assertEquals("Not a function: 1", e.getMessage());
		}
	}
}
