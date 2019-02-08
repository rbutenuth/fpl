package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import java.util.Iterator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Basic list functions and quote.
 */
public class ListFunctions {

	/**
	 * @param scope
	 *            Scope to which functions should be added.
	 * @throws ScopeException
	 *             Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

		scope.put(new Function("quote", comment("Don't evaluate the argument, return it as is."), false, "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (parameters[0] instanceof Parameter) {
					return ((Parameter) parameters[0]).quote(scope);
				} else {
					return parameters[0];
				}
			}
		});

		scope.put(new Function("size", comment("Number of elements in a list."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToList(scope, parameters[0]).size());
			}
		});

		scope.put(new Function("list", comment("Make a list out of the parameters."), true, "element") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue[] values = new FplValue[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					values[i] = parameters[i].evaluate(scope);
				}
				return new FplList(values);
			}
		});

		scope.put(new Function("first", comment("Return first element of the list."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).first();
			}
		});

		scope.put(new Function("last", comment("Return last element of the list."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).last();
			}
		});

		scope.put(new Function("rest", comment("Return list without the first element."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeFirst();
			}
		});

		scope.put(new Function("cons", comment("Return a new list with expression added in front of the given list."),
				false, "expression", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[1]).addAtStart(parameters[0].evaluate(scope));
			}
		});

		scope.put(new Function("add", comment("Return a new list with expression added at the end of the given list."),
				false, "list", "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).addAtEnd(parameters[1].evaluate(scope));
			}
		});

		scope.put(new Function("append", comment("Append two lists."), false, "list-a", "list-b") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).append(evaluateToList(scope, parameters[1]));
			}
		});

		scope.put(new Function("map", comment("Apply a lambda to all list elements."), false, "list", "func") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[0]);
				FplValue val = parameters[1].evaluate(scope);
				if (!(val instanceof FplFunction)) {
					throw new EvaluationException("Second parameter of map must be function.");
				}
				FplFunction func = (FplFunction) val;
				FplValue[] results = new FplValue[list.size()];
				int i = 0;
				Iterator<FplValue> iter = list.iterator();
				while (iter.hasNext()) {
					FplValue input = iter.next();
					FplValue output = func.call(scope, new FplValue[] { input });
					results[i++] = output;
				}
				return new FplList(results);
			}
		});
	}
}
