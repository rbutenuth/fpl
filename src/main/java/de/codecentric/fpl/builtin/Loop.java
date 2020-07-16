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
	public void populate(Scope scope) throws ScopeException {
		scope.define(new AbstractFunction("while", comment("Execute code while condition returns true."), true,
				"condition", "code...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue result = null;
				while (evaluateToBoolean(scope, parameters[0])) {
					for (int i = 1; i < parameters.length; i++) {
						result = parameters[i].evaluate(scope);
					}
				}
				return result;
			}
		});

		scope.define(new AbstractFunction("for-each",
				comment("Apply a lambda to all list elements, return last result"), false, "function", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				Function function = evaluateToFunction(scope, parameters[0]);
				try {
					FplValue result = null;
					Iterator<FplValue> iter = list.lambdaIterator(scope, function);
					while (iter.hasNext()) {
						result = iter.next();
					}
					return result;
				} catch (TunnelException e) {
					throw e.getTunnelledException();
				}
			}
		});

		scope.define(new AbstractFunction("map",
				comment("Apply a lambda to all list elements and return list with applied elements"), false, "function",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				try {
					return FplList.fromIterator(list.lambdaIterator(scope, function), list.size());
				} catch (TunnelException e) {
					throw e.getTunnelledException();
				}
			}
		});

		scope.define(new AbstractFunction("reduce",
				comment("Reduce a list to one value. The function must accept two parameters: "
						+ "accumulator and value. It must return the \"reduction\" of accumulator and value."),
				false, "function", "accumulator", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplValue accumulator = parameters[1].evaluate(scope);
				FplList list = evaluateToList(scope, parameters[2]);
				for (FplValue value : list) {
					accumulator = function.call(scope, new FplValue[] { accumulator, value });
				}
				return accumulator;
			}
		});

		scope.define(new AbstractFunction("filter", comment("Filter a list elements."), false, "func", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				Function function = evaluateToFunction(scope, parameters[0]);
				Iterator<FplValue> iter = list.iterator();
				List<FplValue> results = new ArrayList<>();
				while (iter.hasNext()) {
					FplValue value = iter.next();
					if (evaluateToBoolean(scope, function.call(scope, new FplValue[] { value }))) {
						results.add(value);
					}
				}
				return FplList.fromValues(results);
			}
		});

		// flatten
		// reduce
		// map-object
		// not here: sequential, parallel
	}
}
