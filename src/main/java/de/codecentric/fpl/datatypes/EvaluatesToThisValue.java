package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

/**
 * An object which evaluates to itself.
 */
public interface EvaluatesToThisValue extends FplValue {

	@Override
	public default FplValue evaluate(Scope scope) throws EvaluationException {
		return this;
	}

}
