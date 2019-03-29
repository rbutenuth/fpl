package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.AbstractFunction.comment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Loop functions.
 */
public class Loop {

	/**
	 * @param scope Scope to which functions should be added.
	 * @throws ScopeException Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

        scope.put(new AbstractFunction("while", comment("Execute code while condition returns true."), true, "condition", "code...") {
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

		scope.put(new AbstractFunction("for-each", comment("Apply a lambda to all list elements, return last result"),
				false, "function", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				FplLambda function = evaluateToLambda(scope, parameters[0]);
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

		scope.put(new AbstractFunction("map",
				comment("Apply a lambda to all list elements and return list with applied elements"), false, "function",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				FplLambda function = evaluateToLambda(scope, parameters[0]);
				try {
					return FplList.fromIterator(list.lambdaIterator(scope, function), list.size());
				} catch (TunnelException e) {
					throw e.getTunnelledException();
				}
			}
		});

		scope.put(new AbstractFunction("filter", comment("Filter a list elements."), false, "func", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				FplLambda function = evaluateToLambda(scope, parameters[0]);
				Iterator<FplValue> iter = list.iterator();
				List<FplValue> results = new ArrayList<>();
				while (iter.hasNext()) {
					FplValue value = iter.next();
					if (evaluateToBoolean(scope, function.call(scope, new FplValue[] { value }))) {
						results.add(value);
					}
				}
				return new FplList(results);
			}
		});
		
		// flatten
		// reduce
		// map-object
		// not here: sequential, parallel
	}
}
