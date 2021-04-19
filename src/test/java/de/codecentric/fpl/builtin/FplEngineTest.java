package de.codecentric.fpl.builtin;

import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;

public class FplEngineTest extends AbstractFplTest {

	@Test
	public void commentsInBuiltinFunctions() throws EvaluationException {
		for (Entry<String, FplValue> entry : scope) {
			FplValue value = entry.getValue();
            if (value instanceof AbstractFunction) {
                AbstractFunction f = (AbstractFunction)value;
                checkForComment(f);
            }
        }
	}

	private void checkForComment(AbstractFunction f) throws EvaluationException {
		String comment = f.getComment();
		if (comment.isEmpty()) {
			throw new EvaluationException("missing comment in function " + f.getName());
		}
	}
}
