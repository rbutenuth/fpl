package de.codecentric.fpl.builtin;

import java.io.PrintStream;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * FPL "print".
 */
public class Print implements ScopePopulator {
	private FplEngine engine;

	public Print(FplEngine engine) {
		this.engine = engine;
	}

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {
		scope.define(new PrintFunction(false, "Print parameters.", engine));
		scope.define(new PrintFunction(true, "Print parameters, followed by line break.", engine));
	}

	private static class PrintFunction extends AbstractFunction {
		private boolean newline;
		private FplEngine engine;

		private PrintFunction(boolean newline, String comment, FplEngine engine) throws EvaluationException {
        super(newline ? "println" : "print", comment, "expression...");
        this.newline = newline;
        this.engine = engine;
    }

		@Override
		public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
			PrintStream systemOut = engine.getSystemOut();
			for (int i = 0; i < parameters.length; i++) {
				String string = evaluateToString(scope, parameters[i]);
				systemOut.print(string);
				if (i < parameters.length - 1) {
					systemOut.print(' ');
				}
			}
			if (newline) {
				systemOut.println();
			}
			systemOut.flush();
			return null;
		}
	}
}
