package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Basic logic functions. <code>FplInteger(0)</code>,
 * <code>FplDouble(0.0)</code>, <code>()</code> and <code>nil</code> are
 * <code>false</code>, everything else is true.
 */
public class ControlStructures implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("if-else", //
				comment("Evaluate condition, if true, return evaluated if-part, otherwise evaluated else-part."), false,
				"condition", "if-part", "else-part") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (evaluateToBoolean(scope, parameters[0])) {
					// The "if" clause
					return parameters[1] == null ? null : parameters[1].evaluate(scope);
				} else {
					return parameters[2] == null ? null : parameters[2].evaluate(scope);
				}
			}
		});

		scope.define(new AbstractFunction("if", //
				comment("Evaluate condition, if true, return evaluated if-part, otherwise nil."), false, "condition",
				"if-part") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (evaluateToBoolean(scope, parameters[0])) {
					// The "if" clause
					return parameters[1] == null ? null : parameters[1].evaluate(scope);
				} else {
					return null;
				}
			}
		});

		scope.define(new AbstractFunction("sequential",
				comment("Evaluate the parameters, return value of last parameter."), true, "element") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue value = null;
				for (int i = 0; i < parameters.length; i++) {
					value = evaluateToAny(scope, parameters[i]);
				}
				return value;
			}
		});

		scope.define(new AbstractFunction("throw", //
				comment("Throw an exception."), false, "message") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String message = evaluateToString(scope, parameters[0]);
				throw new EvaluationException(message);
			}
		});

		scope.define(new AbstractFunction("try-catch", //
				comment("."), false, "try", "catch-function") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Function catchFunction = evaluateToFunction(scope, parameters[1]);
				try {
					return parameters[0].evaluate(scope);
				} catch (EvaluationException e) {
					StackTraceElement[] javaStackTrace = e.getStackTrace();
					FplList[]  fplStackTrace = new FplList[javaStackTrace.length];
					for (int i = 0; i < javaStackTrace.length; i++) {
						if (AbstractFunction.FPL.equals(javaStackTrace[i].getClassName())) {
							FplValue[] entry = new FplValue[3];
							entry[0] = new FplString(javaStackTrace[i].getFileName());
							entry[1] = FplInteger.valueOf(javaStackTrace[i].getLineNumber());
							entry[2] = new FplString(javaStackTrace[i].getMethodName());
							fplStackTrace[i] = FplList.fromValues(entry);
						}
					}
					FplValue[] catcherParameters = new FplValue[2];
					catcherParameters[0] = new FplString(e.getMessage());
					catcherParameters[1] = quote(FplList.fromValues(fplStackTrace));
					return catchFunction.call(scope, catcherParameters);
				}
			}
		});
	}

}
