package de.codecentric.fpl.builtin;

import java.util.Iterator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDictionary;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Basic list functions and quote.
 */
public class ListFunctions implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("quote", "Don't evaluate the argument, return it as is.", "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters[0] instanceof Parameter) {
					return ((Parameter) parameters[0]).quote(scope);
				} else {
					return parameters[0];
				}
			}
		});

		scope.define(new AbstractFunction("size", "Number of elements in a list.", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToList(scope, parameters[0]).size());
			}
		});

		scope.define(new AbstractFunction("is-empty",
				"Is the list/string/object empty? (nil is considered as empty, too)", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				boolean empty;
				FplValue value = evaluateToAny(scope, parameters[0]);
				if (value == null) {
					empty = true;
				} else if (value instanceof FplList) {
					empty = ((FplList) value).isEmpty();
				} else if (value instanceof FplString) {
					empty = ((FplString) value).getContent().isEmpty();
				} else if (value instanceof FplDictionary) {
					empty = ((FplDictionary) value).keySet().isEmpty();
				} else {
					throw new EvaluationException("is-empty is not defined for type " + value.typeName());
				}
				return FplInteger.valueOf(empty ? 1 : 0);
			}
		});

		scope.define(new AbstractFunction("list", "Make a list out of the parameters.", "element...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters.length == 0) {
					return FplList.EMPTY_LIST;
				} else {
					return FplList.fromIterator(new Iterator<FplValue>() {
						int i = 0;

						@Override
						public boolean hasNext() {
							return i < parameters.length;
						}

						@Override
						public FplValue next() {
							return evaluateToAny(scope, parameters[i++]);
						}
					}, parameters.length);
				}
			}
		});

		scope.define(new AbstractFunction("first", "Return first element of the list.", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).first();
			}
		});

		scope.define(new AbstractFunction("last", "Return last element of the list.", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).last();
			}
		});

		scope.define(new AbstractFunction("remove-first", "Return list without the first element.", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeFirst();
			}
		});

		scope.define(new AbstractFunction("remove-last", "Return list without the last element.", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).removeLast();
			}
		});

		scope.define(new AbstractFunction("add-front",
				"Return a new list with expression added in front of the given list.", "expression", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[1]).addAtStart(evaluateToAny(scope, parameters[0]));
			}
		});

		scope.define(new AbstractFunction("add-end",
				"Return a new list with expression added at the end of the given list.", "list", "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).addAtEnd(evaluateToAny(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("append", "Append two lists.", "list-a", "list-b") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).append(evaluateToList(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("lower-half", "Return the lower half of a list (opposite to upper-half).",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).lowerHalf();
			}
		});

		scope.define(new AbstractFunction("upper-half", "Return the upper half of a list (opposite to lower-half).",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).upperHalf();
			}
		});

		scope.define(new AbstractFunction("get-element",
				"Return the element at position pos (counted from 0 ) from the given list.", "list", "pos") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).get((int) evaluateToLong(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("set-element",
				"Replace the element at position pos (counted from 0 ) from the given list.", "list", "pos", "element") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).set((int) evaluateToLong(scope, parameters[1]), evaluateToAny(scope, parameters[2]));
			}
		});

//		scope.define(new AbstractFunction("replace-elements",
//				"Replace some elements from this list with elements of another list. The second list "
//				+ "must not have the same number of elements as are removed from the original list. "
//				+ "from is the index of the first replaced element, `new-elements` is the list "
//				+ "with the new elements, `num-replaced` elements will be removed.",
//				"list", "from", "new-elements", "num-replaced") {
//			@Override
//			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
//				FplList list = evaluateToList(scope, parameters[0]);
//				int from = (int)evaluateToLong(scope, parameters[1]);
//				FplList newElements = evaluateToList(scope, parameters[2]);
//				int numReplaced = (int)evaluateToLong(scope, parameters[3]);
//				return list.patch(from, newElements, numReplaced);
//			}
//		});

		scope.define(new AbstractFunction("sub-list",
				"Return a part from the given list, including start, excluding end (counted from 0).", "list", "start",
				"end") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToList(scope, parameters[0]).subList(((int) evaluateToLong(scope, parameters[1])),
						((int) evaluateToLong(scope, parameters[2])));
			}
		});

		// iterator with lambda: See Loop.java
	}
}
