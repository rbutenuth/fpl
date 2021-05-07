package de.codecentric.fpl.builtin;

import java.util.ArrayList;
import java.util.List;

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
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("if-else", //
				"Evaluate condition, if true, return evaluated if-part, otherwise evaluated else-part.", "condition",
				"if-part", "else-part") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (evaluateToBoolean(scope, parameters[0])) {
					// The "if" clause
					return evaluateToAny(scope, parameters[1]);
				} else {
					return evaluateToAny(scope, parameters[2]);
				}
			}
		});

		scope.define(new AbstractFunction("if", //
				"Evaluate condition, if true, return evaluated if-part, otherwise nil.", "condition", "if-part") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (evaluateToBoolean(scope, parameters[0])) {
					// The "if" clause
					return evaluateToAny(scope, parameters[1]);
				} else {
					return null;
				}
			}
		});

		scope.define(new AbstractFunction("cond", //
				"Handle condition expression pairs. When condition is true, expression is executed and result returned. "
				+ "When number of parameters is not even, last one is an expression which is evaluated if all conditions are false.", "condition",
				"expression...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				int i = 0;
				while (i < parameters.length - 1) {
					if (evaluateToBoolean(scope, parameters[i])) {
						return evaluateToAny(scope, parameters[i + 1]);
					}
					i += 2;
				}
				return evaluateToAny(scope, parameters[i]);
			}
		});

		scope.define(new AbstractFunction("sequential", "Evaluate the parameters, return value of last parameter.",
				"element...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue value = null;
				for (int i = 0; i < parameters.length; i++) {
					value = evaluateToAny(scope, parameters[i]);
				}
				return value;
			}
		});

		scope.define(new AbstractFunction("scope", "Evaluate the parameters within a new scope, return value of last parameter.",
				"element...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Scope localScope = scope.createNested("scope");
				FplValue value = null;
				for (int i = 0; i < parameters.length; i++) {
					value = evaluateToAny(localScope, parameters[i]);
				}
				return value;
			}
		});

		scope.define(new AbstractFunction("synchronized", "Evaluate the parameters, return value of last parameter.",
				"monitor", "element...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue monitor = evaluateToAny(scope, parameters[0]);
				FplValue value = null;
				synchronized (monitor) {
					for (int i = 1; i < parameters.length; i++) {
						value = evaluateToAny(scope, parameters[i]);
					}
				}
				return value;
			}
		});

		scope.define(new AbstractFunction("throw", //
				"Throw an exception.", "message") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String message = evaluateToString(scope, parameters[0]);
				throw new EvaluationException(message);
			}
		});

		scope.define(new AbstractFunction("throw-with-id", //
				"Throw an exception.", "message", "id") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String message = evaluateToString(scope, parameters[0]);
				long id = evaluateToLong(scope, parameters[1]);
				throw new EvaluationException(message, (int) id);
			}
		});

		scope.define(new AbstractFunction("try-catch", //
				"Evaluate the given `expression` and return the result. "
						+ "In case of an exception, call `catch-function` and return its result.",
				"expression", "catch-function") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function catchFunction = evaluateToFunctionOrNull(scope, parameters[1]);
				try {
					return parameters[0].evaluate(scope);
				} catch (EvaluationException e) {
					return callCatcher(scope, e, catchFunction);
				}
			}
		});

		scope.define(new AbstractFunction("try-with", //
				"Open `resources`, evaluate (and return the value of) an `expression`, catch exceptions.", "resources",
				"expression", "catch-function") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Scope localScope = scope.createNested("try-with");
				Function catchFunction = null;
				List<Resource> resources = new ArrayList<>();
				try {
					catchFunction = evaluateToFunctionOrNull(scope, parameters[2]);
					for (FplValue resource : evaluateToListIfNotAlreadyList(scope, parameters[0])) {
						// Example for rl: (a (open "a") (lambda (x) (close x))
						FplList rl = (FplList) resource;
						if (rl.size() != 3) {
							throw new EvaluationException("resource must have size 3, but has size " + rl.size());
						}
						String target = Assignment.targetName(localScope, rl.get(0));
						FplValue value = rl.get(1).evaluate(localScope);
						Function function = evaluateToFunction(localScope, rl.get(2));
						localScope.define(target, value);
						resources.add(new Resource(value, function));
					}
					return parameters[1].evaluate(localScope);
				} catch (EvaluationException e) {
					return callCatcher(localScope, e, catchFunction);
				} catch (ScopeException e) {
					return callCatcher(localScope, new EvaluationException(e.getMessage(), e), catchFunction);
				} finally {
					for (int i = resources.size() - 1; i >= 0; i--) {
						Resource r = resources.get(i);
						try {
							r.function.call(localScope, new FplValue[] { r.value });
						} catch (EvaluationException e) {
							// ignore
						}
					}
				}
			}

			class Resource {
				final FplValue value;
				final Function function;

				Resource(FplValue value, Function function) {
					this.value = value;
					this.function = function;
				}
			}
		});

	}

	private FplValue callCatcher(Scope scope, EvaluationException e, Function catchFunction)
			throws EvaluationException {
		if (catchFunction == null) {
			throw e;
		} else {
			StackTraceElement[] javaStackTrace = e.getStackTrace();
			List<FplList> fplStackTrace = new ArrayList<>();
			for (int i = 0; i < javaStackTrace.length; i++) {
				if (AbstractFunction.FPL.equals(javaStackTrace[i].getClassName())) {
					FplValue[] entry = new FplValue[3];
					entry[0] = new FplString(javaStackTrace[i].getFileName());
					entry[1] = FplInteger.valueOf(javaStackTrace[i].getLineNumber());
					entry[2] = new FplString(javaStackTrace[i].getMethodName());
					fplStackTrace.add(FplList.fromValues(entry));
				}
			}
			FplValue[] catcherParameters = new FplValue[3];
			catcherParameters[0] = new FplString(e.getMessage());
			catcherParameters[1] = FplInteger.valueOf(e.getId());
			catcherParameters[2] = AbstractFunction.quote(FplList.fromValues((fplStackTrace)));
			return catchFunction.call(scope, catcherParameters);
		}
	}

}
