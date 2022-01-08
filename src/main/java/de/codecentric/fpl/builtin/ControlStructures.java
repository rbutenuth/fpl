package de.codecentric.fpl.builtin;

import java.util.ArrayList;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.PipelineScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;
import static de.codecentric.fpl.builtin.Assignment.targetName;

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
				return i < parameters.length ? evaluateToAny(scope, parameters[i]) : null;
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

		scope.define(new AbstractFunction("scope", "Evaluate the expression within a new scope, return value of last expression.",
				"expression...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... expressions) throws EvaluationException {
				Scope localScope = new MapScope("scope", scope);
				FplValue value = null;
				for (int i = 0; i < expressions.length; i++) {
					value = evaluateToAny(localScope, expressions[i]);
				}
				return value;
			}
		});

		scope.define(new AbstractFunction("pipeline", "Evaluate the expressions within a new scope, return value of last expression. "
				+ "The evaluation result of the expressions is bound to the symbol given as parameter pipe-key.",
				"pipe-key", "expression...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String pipeKey = targetName(scope, parameters[0]);
				PipelineScope localScope = new PipelineScope(pipeKey, scope);
				for (int i = 1; i < parameters.length; i++) {
					localScope.set(evaluateToAny(localScope, parameters[i]));
				}
				return localScope.get();
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
					return evaluateToAny(scope, parameters[0]);
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
				Scope localScope = new MapScope("try-with", scope);
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
						FplValue value = evaluateToAny(localScope, rl.get(1));
						Function function = evaluateToFunction(localScope, rl.get(2));
						localScope.define(target, value);
						resources.add(new Resource(value, function));
					}
					return evaluateToAny(localScope, parameters[1]);
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
			FplList stackTrace = filteredStacktrace(e.getStackTrace());
			FplValue[] catcherParameters = new FplValue[3];
			catcherParameters[0] = new FplString(e.getMessage());
			catcherParameters[1] = FplInteger.valueOf(e.getId());
			catcherParameters[2] = AbstractFunction.quote(stackTrace);
			return catchFunction.call(scope, catcherParameters);
		}
	}

	private FplList filteredStacktrace(StackTraceElement[] javaStackTrace) {
		List<FplList> fplStackTrace = new ArrayList<>();
		for (int i = 0; i < javaStackTrace.length; i++) {
			if (AbstractFunction.FPL.equals(javaStackTrace[i].getClassName())) {
				fplStackTrace.add(FplList.fromValues(
						new FplString(javaStackTrace[i].getFileName()),
						FplInteger.valueOf(javaStackTrace[i].getLineNumber()),
						new FplString(javaStackTrace[i].getMethodName())));
			}
		}
		return FplList.fromValues(fplStackTrace);
	}

}
