package de.codecentric.fpl;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * Builds the initial {@link Scope} and contains helpers for execution.
 */
public interface FplEngine {
	static final Position UNKNOWN = new Position("<unknown>", 1, 1);

	/**
	 * @return The Thread pool used by the engine.
	 */
	public ForkJoinPool getPool();

	/**
	 * @param pool The Thread pool used by the engine.
	 */
	public void setPool(ForkJoinPool pool);

	/**
	 * @return Output used by print / println.
	 */
	public PrintStream getSystemOut();

	/**
	 * @param systemOut Output used by print / println.
	 */
	public void setSystemOut(PrintStream systemOut);

	/**
	 * @return Default {@link Scope} used by the engine.
	 */
	public Scope getScope();

	/**
	 * Evaluate a source given by a {@link Reader}, uses engine scope as {@link Scope}.
	 * @param sourceName Name of the source (used for error messages)
	 * @param rd Reader
	 * @param callback For expression results, catching exceptions, and print / println.
	 * @return Results of all expressions evaluated.
	 * @throws IOException For problems with the reader.
	 */
	public List<FplValue> evaluate(String sourceName, Reader rd, ResultCallback callback) throws IOException;
	
	/** 
	 * Evaluate one expression, uses engine scope as {@link Scope}.
	 * @param expression Expression to evaluate, not <code>null</code>
	 * @return Evaluated expression.
	 * @throws EvaluationException If anything goes wrong
	 */
	public FplValue evaluate(FplValue expression) throws EvaluationException;

	/**
	 * Try to find the position in the source where this expression has been parsed.
	 * @param expression Expression may be <code>null</code> 
	 * @return Found position, may be {@link FplEngine.UNKNOWN}
	 */
	public static Position findPosition(FplValue expression) {
		if (expression == null) {
			return UNKNOWN;
		} else {
			if (expression instanceof PositionHolder) {
				return ((PositionHolder) expression).getPosition();
			} else if (expression instanceof FplList) {
				FplList list = (FplList)expression;
				for (FplValue subExpression : list) {
					Position p = findPosition(subExpression);
					if (! UNKNOWN.equals(p)) {
						return p;
					}
				}
				return UNKNOWN;
			} else {
				return UNKNOWN;
			}
		}
	}
}
