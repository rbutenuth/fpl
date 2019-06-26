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
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("quote", comment("Don't evaluate the argument, return it as is."), false,
				"expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (parameters[0] instanceof Parameter) {
					return ((Parameter) parameters[0]).quote(scope);
				} else {
					return parameters[0];
				}
			}
		});

		scope.define(new AbstractFunction("size", comment("Number of elements in a list."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToList(scope, parameters[0]).size());
			}
		});

		scope.define(new AbstractFunction("list", comment("Make a list out of the parameters."), true, "element") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue[] values = new FplValue[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					values[i] = parameters[i].evaluate(scope);
				}
				return FplList.fromValues(values);
			}
		});

		scope.define(new AbstractFunction("first", comment("Return first element of the list."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).first();
			}
		});

		scope.define(new AbstractFunction("last", comment("Return last element of the list."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).last();
			}
		});

		scope.define(new AbstractFunction("remove-first", comment("Return list without the first element."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeFirst();
			}
		});

		scope.define(new AbstractFunction("remove-last", comment("Return list without the last element."), false, "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeLast();
			}
		});

		scope.define(new AbstractFunction("add-front",
				comment("Return a new list with expression added in front of the given list."), false, "expression",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[1]).addAtStart(parameters[0].evaluate(scope));
			}
		});

		scope.define(new AbstractFunction("add-end",
				comment("Return a new list with expression added at the end of the given list."), false, "list",
				"expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).addAtEnd(parameters[1].evaluate(scope));
			}
		});

		scope.define(new AbstractFunction("append", comment("Append two lists."), false, "list-a", "list-b") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).append(evaluateToList(scope, parameters[1]));
			}
		});
	}
}
