package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

/**
 * An object which evaluates to itself.
 */
public class EvaluatesToThisValue implements FplValue {

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		return this;
	}

}
