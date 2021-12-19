package de.codecentric.fpl.data;

/**
 * For failures around {@link Scopes}
 */
public class ScopeException extends RuntimeException {
	private static final long serialVersionUID = -5325999097230010519L;

	public ScopeException(String message) {
		super(message);
	}
	
	public ScopeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScopeException(Throwable cause) {
		super(cause);
	}
}
