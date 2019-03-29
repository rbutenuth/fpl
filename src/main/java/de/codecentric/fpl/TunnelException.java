package de.codecentric.fpl;

/**
 * Helper for all cases where you can't throw an {@link EvaluationException}.
 */
public class TunnelException extends RuntimeException {
	private static final long serialVersionUID = -6820018957334974325L;
	private EvaluationException tunnelledException;

	public TunnelException(EvaluationException tunnelledException) {
		this.tunnelledException = tunnelledException;
	}
	
	public EvaluationException getTunnelledException() {
		return tunnelledException;
	}
}
