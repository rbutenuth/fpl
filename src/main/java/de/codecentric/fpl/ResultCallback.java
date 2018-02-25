package de.codecentric.fpl;

import de.codecentric.fpl.datatypes.FplValue;

/**
 * Called whenever an expression has been evaluated.
 * (Remember: There can be several in one source.)
 */
public interface ResultCallback {

	/**
	 * @param result The result of the evaluation.
	 * @return Shall we go on with evaluation?
	 */
	public boolean handleSuccess(FplValue result);
	
	/**
	 * @param exception The exception occurred.
	 * @return Shall we go on with evaluation?
	 */
	public boolean handleException(Exception exception);
}
