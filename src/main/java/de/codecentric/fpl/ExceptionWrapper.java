package de.codecentric.fpl;


/**
 * Helper class to wrap any {@link Throwable} in an {@link EvaluationException}.
 */
public class ExceptionWrapper {

	private ExceptionWrapper() {
		// do not instantiate
	}

	/**
	 * Wrap all exceptions - except {@link EvaluationException}s - into
	 * {@link EvaluationException}.
	 * 
	 * @param <T>        Return type of the executed lambda
	 * @param callable Executed lambda
	 * @return The return value of the lambda
	 * @throws EvaluationException For any {@link Throwable} within the lambda.
	 */
	public static <T> T wrapException(Callable<T> callable) throws EvaluationException {
		try {
			return callable.execute();
		} catch (Throwable t) {
			if (t instanceof EvaluationException) {
				throw (EvaluationException) t;
			}
			if (t.getCause() instanceof EvaluationException) {
				throw (EvaluationException) t.getCause();
			}
			throw new EvaluationException(t);
		}
	}
	
	/**
	 * Wrap all exceptions - except {@link EvaluationException}s - into
	 * {@link EvaluationException}.
	 * 
	 * @param executable Executed lambda
	 * @return The return value of the lambda
	 * @throws EvaluationException For any {@link Throwable} within the lambda.
	 */
	public static void wrapException(Executable executable) throws EvaluationException {
		try {
			executable.execute();
		} catch (Throwable t) {
			if (t instanceof EvaluationException) {
				throw (EvaluationException) t;
			}
			if (t.getCause() instanceof EvaluationException) {
				throw (EvaluationException) t.getCause();
			}
			throw new EvaluationException(t);
		}
	}
}
