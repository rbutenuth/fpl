package de.codecentric.fpl;


/**
 * Helper class to wrap any {@link Throwable} in an {@link EvaluationException}.
 */
public class ExceptionWrapper {
	
	private ExceptionWrapper() {
		// do not instantiate
	}
	
	/**
	 * Wrap all exceptions - except {@link EvaluationException}s - into {@link EvaluationException}.
	 * @param <T> Return type of the executed lambda
	 * @param executable Executed lambda
	 * @return The return value of the lambda
	 * @throws EvaluationException For any {@link Throwable} within the lambda.
	 */
	public static <T> T wrapException(Executable<T> executable) throws EvaluationException {
		try {
			return executable.execute();
		} catch (Throwable t) {
			if (t instanceof EvaluationException) {
				throw (EvaluationException)t;
			} else {
				throw new EvaluationException(t);
			}
		}
	}
}
