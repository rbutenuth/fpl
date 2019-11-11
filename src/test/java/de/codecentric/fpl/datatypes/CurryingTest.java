package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.parser.ParseException;

public class CurryingTest extends AbstractFplTest {
    @Test
    public void testNoArguments() throws ParseException, IOException, EvaluationException {
        // Result should be the function itself
        AbstractFunction f = (AbstractFunction) evaluate("plus", "( + )");
        assertEquals(2, f.getMinimumNumberOfParameters());
        assertTrue(scope.get("+") == f);
    }

    @Test
    public void testOneMissingArgument() throws ParseException, IOException, EvaluationException {
        evaluate("plus", "(put plus3 ( + 3 ))");
        AbstractFunction f = (AbstractFunction)scope.get("plus3");
        assertEquals(1, f.getMinimumNumberOfParameters());
        String[] pn = f.getParameterNames();
        assertEquals(2, pn.length);
        assertEquals("op2", pn[0]);
        assertEquals("ops...", pn[1]);
        FplInteger i = (FplInteger)evaluate("plus3", "(plus3 4)");
        assertEquals(7, i.getValue());
    }

	@Test
	public void testCurryingOfFplFunction() throws Exception {
		ListResultCallback callback = evaluateResource("currying-of-fpl-function.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(3, values.size());
		FplInteger number = (FplInteger) values.get(2);
		assertEquals(FplInteger.valueOf(1), number);
	}

	@Test
	public void testCurryingOfFplFunctionWithSymbol() throws Exception {
		ListResultCallback callback = evaluateResource("currying-of-fpl-function-with-symbol.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(5, values.size());
		FplInteger number = (FplInteger) values.get(4);
		assertEquals(FplInteger.valueOf(7), number);
	}
}
