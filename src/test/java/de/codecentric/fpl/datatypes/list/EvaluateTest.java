package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;

public class EvaluateTest extends AbstractFplTest {

	private static final String NL = System.lineSeparator();

	@Test
	public void notAFunction() throws Exception {
		try {
			evaluate("error", "(1 2)");
		} catch (EvaluationException e) {
			assertEquals("Not a function: 1", e.getMessage());
		}
	}

	@Test
	public void listToStringObjectAndNil() throws Exception {
		FplValue list = evaluate("list", "(list nil 1 (dict))");
		assertEquals("(nil 1 {" + NL + "}" + NL + ")", list.toString());
	}

}
