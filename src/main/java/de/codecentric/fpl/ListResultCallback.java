package de.codecentric.fpl;

import java.util.ArrayList;
import java.util.List;

import de.codecentric.fpl.datatypes.FplValue;

/**
 * Collect results in a {@link List}, stop on first exception.
 */
public class ListResultCallback implements ResultCallback {
	private List<FplValue> results = new ArrayList<>();
	private Exception exception;
	
	@Override
	public boolean handleSuccess(FplValue result) {
		results.add(result);
		return true;
	}

	@Override
	public boolean handleException(Exception exception) {
		this.exception = exception;
		return false;
	}

	/**
	 * @return Has an exception occurred during execution?
	 */
	public boolean hasException() {
		return exception != null;
	}
	
	/**
	 * @return Occurred exception or <code>null</code> when none occured.
	 */
	public Exception getException() {
		return exception;
	}
	
	/**
	 * @return Copy of the {@link List} with the results.
	 */
	public List<FplValue> getResults() {
		return new ArrayList<FplValue>(results);
	}
}
