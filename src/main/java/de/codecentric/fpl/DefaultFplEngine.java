package de.codecentric.fpl;

import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ForkJoinPool;

import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;

/**
 * Builds the initial {@link Scope} and contains helpers for execution.
 */
public class DefaultFplEngine implements FplEngine {
	private ForkJoinPool pool;
	private PrintStream systemOut;
	private Scope scope;

	public DefaultFplEngine() throws ScopeException, EvaluationException {
		systemOut = System.out;
		pool = ForkJoinPool.commonPool();
		scope = new MapScope("global");

		Iterator<ScopePopulator> iterator = ServiceLoader.load(ScopePopulator.class).iterator();
		while (iterator.hasNext()) {
			iterator.next().populate(this);
		}
	}

	@Override
	public ForkJoinPool getPool() {
		return pool;
	}

	@Override
	public void setPool(ForkJoinPool pool) {
		this.pool = pool;
	}

	@Override
	public PrintStream getSystemOut() {
		return systemOut;
	}

	@Override
	public void setSystemOut(PrintStream systemOut) {
		this.systemOut = systemOut;
	}

	@Override
	public Scope getScope() {
		return scope;
	}

	@Override
	public List<FplValue> evaluate(String sourceName, Reader rd, ResultCallback callback) {
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
						results.add(expression);
						continueEvaluation = callback.handleSuccess(expression);
					} else {
						continueEvaluation = false;
					}
				} catch (EvaluationException e) {
					Position p = FplEngine.findPosition(expression);
					e.add(new StackTraceElement(AbstractFunction.FPL, "top-level", p.getName(), p.getLine()));
					continueEvaluation = callback.handleException(e);
				} catch (Exception e) {
					continueEvaluation = callback.handleException(e);
				}
			} while (continueEvaluation);
		}
		return results;
	}

	@Override
	public FplValue evaluate(FplValue expression) throws EvaluationException {
		return expression.evaluate(scope);
	}
}
