package de.codecentric.fpl.builtin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Loop functions.
 */
public class Loop implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {
		scope.define(
				new AbstractFunction("while", "Execute code while condition returns true.", "condition", "code...") {
					@Override
					public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						FplValue result = null;
						while (evaluateToBoolean(scope, parameters[0])) {
							for (int i = 1; i < parameters.length; i++) {
								result = parameters[i].evaluate(scope);
							}
						}
						return result;
					}
				});

		scope.define(new AbstractFunction("for-each", "Apply a lambda to all list elements, return last result",
				"function", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				Function function = evaluateToFunction(scope, parameters[0]);
				FplValue result = null;
				Iterator<FplValue> iter = list.iterator();
				while (iter.hasNext()) {
					result = function.call(scope, new FplValue[] { AbstractFunction.quote(iter.next()) });
				}
				return result;
			}
		});

		scope.define(new AbstractFunction("map",
				"Apply a lambda to all list elements and return list with applied elements", "function", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				try {
					return list.map(new java.util.function.Function<FplValue, FplValue>() {

						@Override
						public FplValue apply(FplValue value) {
							try {
								return function.call(scope, new FplValue[] { AbstractFunction.quote(value) });
							} catch (EvaluationException e) {
								throw new TunnelException(e);
							}
						}
					});
				} catch (TunnelException e) {
					throw e.getTunnelledException();
				}
			}
		});

		scope.define(new AbstractFunction("flat-map",
				"Apply a lambda to all list elements, the result of the lambda must be a list. Return list with applied elements of all returned lists.",
				"function", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				try {
					return list.flatMap(new java.util.function.Function<FplValue, FplList>() {

						@Override
						public FplList apply(FplValue value) {
							try {
								FplValue applied = function.call(scope,
										new FplValue[] { AbstractFunction.quote(value) });
								if (applied instanceof FplList) {
									return (FplList) applied;
								} else {
									throw new EvaluationException("Not a list: " + applied);
								}
							} catch (EvaluationException e) {
								throw new TunnelException(e);
							}
						}
					});
				} catch (TunnelException e) {
					throw e.getTunnelledException();
				}
			}
		});

		scope.define(new AbstractFunction("reduce",
				"Reduce a list to one value. The function must accept two parameters: "
						+ "accumulator and value. It must return the \"reduction\" of accumulator and value.",
				"function", "accumulator", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplValue accumulator = parameters[1].evaluate(scope);
				FplList list = evaluateToList(scope, parameters[2]);
				for (FplValue value : list) {
					accumulator = function.call(scope, accumulator, AbstractFunction.quote(value));
				}
				return accumulator;
			}
		});

		scope.define(new AbstractFunction("filter", "Filter a list elements.", "func", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				Iterator<FplValue> iter = list.iterator();
				List<FplValue> results = new ArrayList<>();
				while (iter.hasNext()) {
					FplValue value = iter.next();
					if (evaluateToBoolean(scope, function.call(scope, AbstractFunction.quote(value)))) {
						results.add(value);
					}
				}
				return FplList.fromValues(results);
			}
		});
	}
}
