package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

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
	
	@Test
	public void unnamedTest() throws Exception {
		FplValue value = new Function() {
			
			@Override
			public FplValue call(Scope scope, FplValue... parameters) throws EvaluationException {
				throw new EvaluationException("bum");
			}
			
			@Override
			public String typeName() {
				return "bad-value";
			}
			
			@Override
			public FplValue evaluate(Scope scope) throws EvaluationException {
				throw new RuntimeException("bäm");
			}
		};
		scope.define("value", value);
		EvaluationException e = assertThrows(EvaluationException.class, () -> { FplList.fromValue(value).evaluate(scope); });
		assertEquals("bäm", e.getMessage());
	}

}
