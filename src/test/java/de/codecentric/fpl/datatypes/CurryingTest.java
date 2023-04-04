package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.parser.ParseException;

public class CurryingTest extends AbstractFplTest {
    @Test
    public void noArguments() throws ParseException, EvaluationException {
        // Result should be the function itself
        AbstractFunction f = (AbstractFunction) evaluate("plus", "( + )");
        assertEquals(2, f.getMinimumNumberOfParameters());
        assertTrue(scope.get("+") == f);
    }

    @Test
    public void oneMissingArgument() throws ParseException, EvaluationException {
        evaluate("plus", "(put plus3 ( + 3 ))");
        AbstractFunction f = (AbstractFunction)scope.get("plus3");
        assertEquals(1, f.getMinimumNumberOfParameters());
        Set<String> pn = f.getParameterNameToIndex().keySet();
        assertEquals(2, pn.size());
        Iterator<String> pnIter = pn.iterator();
        assertTrue(pnIter.hasNext());
        assertEquals("op2", pnIter.next());
        assertTrue(pnIter.hasNext());
        assertEquals("ops", pnIter.next());
        assertFalse(pnIter.hasNext());
        FplInteger i = (FplInteger)evaluate("plus3", "(plus3 4)");
        assertEquals(7, i.getValue());
    }

	@Test
	public void curryingOfFplFunction() throws Exception {
		ListResultCallback callback = evaluateResource("currying-of-fpl-function.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(3, values.size());
		FplInteger number = (FplInteger) values.get(2);
		assertEquals(FplInteger.valueOf(1), number);
	}

	@Test
	public void curryingOfFplFunctionWithSymbol() throws Exception {
		ListResultCallback callback = evaluateResource("currying-of-fpl-function-with-symbol.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(5, values.size());
		FplInteger number = (FplInteger) values.get(4);
		assertEquals(FplInteger.valueOf(7), number);
	}
}
