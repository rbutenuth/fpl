package de.codecentric.fpl.builtin;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;

/**
 * FPL "print".
 */
public class Print implements ScopePopulator {
	private FplEngine engine;

	public Print(FplEngine engine) {
		this.engine = engine;
	}

	@Override
	public void populate(Scope scope) throws ScopeException {
		scope.define(new PrintFunction(false, comment("Print parameters."), engine));
		scope.define(new PrintFunction(true, comment("Print parameters, followed by line break."), engine));
	}

	private static class PrintFunction extends AbstractFunction {
		private boolean newline;
		private FplEngine engine;

		private PrintFunction(boolean newline, List<String> comment, FplEngine engine) {
        super(newline ? "println" : "print", comment, true, "expression");
        this.newline = newline;
        this.engine = engine;
    }

		@Override
		public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
			for (int i = 0; i < parameters.length; i++) {
				FplValue value = parameters[i].evaluate(scope);
				engine.getSystemOut().print(value);
				if (i < parameters.length - 1) {
					engine.getSystemOut().print(' ');
				}
			}
			if (newline) {
				engine.getSystemOut().println();
			}
			return null;
		}
	}
}
