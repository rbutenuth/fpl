package de.codecentric.fpl;

import de.codecentric.fpl.datatypes.FplValue;

/**
 * Collect evaluation results in a String.
 */
public class StringResultCallback implements ResultCallback {
	private boolean continueOnException;
	private StringBuilder builder;

	public StringResultCallback(boolean stopOnException) {
		this.continueOnException = stopOnException;
		builder = new StringBuilder();
	}
	
	/**
	 * Clear collected results. 
	 */
	public void reset() {
		builder.setLength(0);
	}

	@Override
	public String toString() {
		return builder.toString();
	}
	
	@Override
	public boolean handleSuccess(FplValue result) {
		separate();
		builder.append(result == null ? "nil" : result.toString());
		newline();
		return true;
	}

	@Override
	public boolean handleException(Exception exception) {
		separate();
		builder.append(exception.getMessage());
		newline();
		if (exception instanceof EvaluationException) {
			EvaluationException ee = (EvaluationException) exception;
			StackTraceElement[] trace = ee.getStackTrace();
			for (int i = 0; i < ee.getAdded(); i++) {
				builder.append("    at ");
				builder.append(trace[i].getMethodName()).append("(");
				builder.append(trace[i].getFileName()).append(":");
				builder.append(trace[i].getLineNumber()).append(")");
				newline();
			}
		}
		return continueOnException;
	}

	private void separate() {
		if (builder.length() > 0) {
			newline();
		}
	}

	private void newline() {
		builder.append(System.lineSeparator());
	}
}
