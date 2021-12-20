package de.codecentric.fpl;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import de.codecentric.fpl.builtin.Arithmetic;
import de.codecentric.fpl.builtin.Assignment;
import de.codecentric.fpl.builtin.ClassAndObject;
import de.codecentric.fpl.builtin.Comparison;
import de.codecentric.fpl.builtin.ControlStructures;
import de.codecentric.fpl.builtin.Dictionary;
import de.codecentric.fpl.builtin.InputOutput;
import de.codecentric.fpl.builtin.Lambda;
import de.codecentric.fpl.builtin.ListFunctions;
import de.codecentric.fpl.builtin.Logic;
import de.codecentric.fpl.builtin.Loop;
import de.codecentric.fpl.builtin.Parallel;
import de.codecentric.fpl.builtin.Print;
import de.codecentric.fpl.builtin.StringFunctions;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;

/**
 * Builds the initial {@link Scope} and contains helpers for execution.
 */
public class FplEngine {
	private static final Position UNKNOWN = new Position("<unknown>", 1, 1);
	private ForkJoinPool pool;
	private PrintStream systemOut;
	private Scope scope;

	public FplEngine() throws ScopeException, EvaluationException {
		systemOut = System.out;
		scope = createDefaultScope();
		pool = ForkJoinPool.commonPool();
	}

	/**
	 * @return A new {@link Scope} with no outer scope and all built in functions.
	 * @throws ScopeException Should not happen on initialization.
	 */
	public Scope createDefaultScope() throws ScopeException, EvaluationException {
		Scope scope = new MapScope("global");

		new Print(this).populate(scope);
		new Assignment().populate(scope);
		new Arithmetic().populate(scope);
		new Logic().populate(scope);
		new ListFunctions().populate(scope);
		new StringFunctions().populate(scope);
		new Comparison().populate(scope);
		new ControlStructures().populate(scope);
		new Parallel(this).populate(scope);
		new Loop().populate(scope);
		new Lambda().populate(scope);
		new ClassAndObject().populate(scope);
		new Dictionary().populate(scope);
		new InputOutput(this).populate(scope);

		return scope;
	}

	public ForkJoinPool getPool() {
		return pool;
	}

	public void setPool(ForkJoinPool pool) {
		this.pool = pool;
	}

	public PrintStream getSystemOut() {
		return systemOut;
	}

	public void setSystemOut(PrintStream systemOut) {
		this.systemOut = systemOut;
	}

	public Scope getScope() {
		return scope;
	}

	public List<FplValue> evaluate(String sourceName, Reader rd, ResultCallback callback) throws IOException {
		List<FplValue> results = new ArrayList<>();
		boolean continueEvaluation = true;
		try (Parser parser = new Parser(new Scanner(sourceName, rd))) {
			do {
				FplValue expression = null;
				try {
					if (parser.hasNext()) {
						expression = parser.next();
						if (expression != null) {
							expression = expression.evaluate(scope);
						}
						continueEvaluation = callback.handleSuccess(expression);
					} else {
						continueEvaluation = false;
					}
				} catch (EvaluationException e) {
					Position p = findPosition(expression);
					e.add(new StackTraceElement(AbstractFunction.FPL, "top-level", p.getName(), p.getLine()));
					continueEvaluation = callback.handleException(e);
				} catch (Exception e) {
					continueEvaluation = callback.handleException(e);
				}
			} while (continueEvaluation);
		}
		return results;
	}

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
