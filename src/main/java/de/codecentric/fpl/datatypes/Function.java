package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

public interface Function {

	/**
	 * Call a function (with parameters). When there are not enough parameters for
	 * the function, do Currying.
	 * 
	 * @param scope
	 *            Evaluation scope.
	 * @param parameters
	 *            The parameters of the function. Do not change the array elements!
	 * @return The result of the function (may be a Curryied function).
	 * @throws EvaluationException
	 *             If execution fails.
	 */
	public FplValue call(Scope scope, FplValue... parameters) throws EvaluationException;
}
