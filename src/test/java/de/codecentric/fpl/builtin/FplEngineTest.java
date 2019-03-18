package de.codecentric.fpl.builtin;

import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;

public class FplEngineTest extends AbstractFplTest {

	@Test
	public void testCommentsInBuiltinFunctions() throws EvaluationException {
		for (Entry<String, FplValue> entry : scope) {
			FplValue value = entry.getValue();
            if (value instanceof AbstractFunction) {
                AbstractFunction f = (AbstractFunction)value;
                checkForComments(f);
            }
        }
	}

	private void checkForComments(AbstractFunction f) throws EvaluationException {
		List<String> comment = f.getComment();
		if (comment.isEmpty()) {
			throw new EvaluationException("missing comment in function " + f.getName());
		}
	}
}
