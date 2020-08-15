package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Basic list functions and quote.
 */
public class ListFunctions implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("quote", "Don't evaluate the argument, return it as is.", false,
				"expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters[0] instanceof Parameter) {
					return ((Parameter) parameters[0]).quote(scope);
				} else {
					return parameters[0];
				}
			}
		});

		scope.define(new AbstractFunction("size", "Number of elements in a list.", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToList(scope, parameters[0]).size());
			}
		});

		scope.define(new AbstractFunction("list", "Make a list out of the parameters.", true, "element") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue[] values = new FplValue[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					values[i] = evaluateToAny(scope, parameters[i]);
				}
				return FplList.fromValues(values);
			}
		});

		scope.define(new AbstractFunction("first", "Return first element of the list.", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).first();
			}
		});

		scope.define(new AbstractFunction("last", "Return last element of the list.", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).last();
			}
		});

		scope.define(new AbstractFunction("remove-first", "Return list without the first element.", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeFirst();
			}
		});

		scope.define(new AbstractFunction("remove-last", "Return list without the last element.", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeLast();
			}
		});

		scope.define(new AbstractFunction("add-front", 
				"Return a new list with expression added in front of the given list.", false, "expression",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[1]).addAtStart(parameters[0].evaluate(scope));
			}
		});

		scope.define(new AbstractFunction("add-end", 
				"Return a new list with expression added at the end of the given list.", false, "list",
				"expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).addAtEnd(parameters[1].evaluate(scope));
			}
		});

		scope.define(new AbstractFunction("append", "Append two lists.", false, "list-a", "list-b") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).append(evaluateToList(scope, parameters[1]));
			}
		});
		
		scope.define(new AbstractFunction("lower-half", "Return the lower half of a list (opposite to upper-half).", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).lowerHalf();
			}
		});

		scope.define(new AbstractFunction("upper-half", "Return the upper half of a list (opposite to lower-half).", false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).upperHalf();
			}
		});

		scope.define(new AbstractFunction("get-element",
				"Return the element at position pos (counted from 0 ) from the given list.", false, "list", "pos") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).get((int)evaluateToLong(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("sub-list",
				"Return a part from the given list, including start, excluding end (counted from 0).", false, "list", "start", "end") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).subList(((int)evaluateToLong(scope, parameters[1])), ((int)evaluateToLong(scope, parameters[2])));
			}
		});

		// iterator with lambda: See Loop.java
	}
}
