package de.codecentric.fpl.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDictionary;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplLazy;
import de.codecentric.fpl.datatypes.FplMapDictionary;
import de.codecentric.fpl.datatypes.FplSortedDictionary;
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
								result = evaluateToAny(scope, parameters[i]);
							}
						}
						return result;
					}
				});

		scope.define(new AbstractFunction("for-each", "Apply a lambda to all list elements, return last result",
				"lambda", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				Function function = evaluateToFunction(scope, parameters[0]);
				FplValue result = null;
				Iterator<FplValue> iter = list.iterator();
				while (iter.hasNext()) {
					result = function.call(scope, FplLazy.makeEvaluated(scope, iter.next()));
				}
				return result;
			}
		});

		scope.define(new AbstractFunction("from-to",
				"Apply a lambda to all numbers from start (inclusive) to end (exclusive). "
						+ "Start and end must be numbers.\n"
						+ "End may be smaller then start, in this case to sequence of numbers is decreasing.\n"
						+ "The lambda must accept one parameter, the current number.\n"
						+ "Result is the result of the last lambda evaluation.",
				"lambda", "start", "end") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				long start = evaluateToLong(scope, parameters[1]);
				long end = evaluateToLong(scope, parameters[2]);
				long delta = end >= start ? 1 : -1;
				FplValue result = null;
				while (start != end) {
					result = function.call(scope, new FplValue[] { FplInteger.valueOf(start) });
					start += delta;
				}
				return result;
			}
		});

		scope.define(new AbstractFunction("from-to-inclusive",
				"Apply a lambda to all numbers from start (inclusive) to end (inclusive). "
						+ "Start and end must be numbers.\n"
						+ "End may be smaller then start, in this case to sequence of numbers is decreasing.\n"
						+ "The lambda must accept one parameter, the current number.\n"
						+ "Result is the result of the last lambda evaluation.",
				"lambda", "start", "end") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				long start = evaluateToLong(scope, parameters[1]);
				long end = evaluateToLong(scope, parameters[2]);
				long delta = end >= start ? 1 : -1;
				end += delta;
				FplValue result = null;
				while (start != end) {
					result = function.call(scope, new FplValue[] { FplInteger.valueOf(start) });
					start += delta;
				}
				return result;
			}
		});

		scope.define(new AbstractFunction("map-sequence",
				"Apply a lambda to all numbers from start (inclusive) to end (exclusive). Start and end must be numbers.\n"
						+ "End must not be less than start.\n"
						+ "The lambda must accept one parameter, the current number.\n"
						+ "Result is a list of the applied lambda for all the numbers in the sequence.\n",
				"lambda", "start", "end") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				long start = evaluateToLong(scope, parameters[1]);
				long end = evaluateToLong(scope, parameters[2]);
				if (start > end) {
					throw new EvaluationException("start > end");
				}
				if (start == end) {
					return FplList.EMPTY_LIST;
				} else {
					return FplList.fromIterator(new Iterator<FplValue>() {
						long current = start;

						@Override
						public boolean hasNext() {
							return current < end;
						}

						@Override
						public FplValue next() {
							return function.call(scope, new FplValue[] { FplInteger.valueOf(current++) });
						}
					}, (int) (end - start));
				}
			}
		});

		scope.define(new AbstractFunction("reduce-sequence",
				"Reduce a sequence of numbers from `start` (inclusive) to `end` (exclusive) to one value. "
				+ "The lambda must accept two parameters: "
				+ "`accumulator` and `value`. It must return the \"reduction\" of accumulator and value.\n"
				+ "",
				"lambda", "acc", "start", "end") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplValue accumulator = evaluateToAny(scope, parameters[1]);
				long start = evaluateToLong(scope, parameters[2]);
				long end = evaluateToLong(scope, parameters[3]);
				long delta = end >= start ? 1 : -1;
				while (start != end) {
					accumulator = function.call(scope, new FplValue[] { FplLazy.makeEvaluated(scope, accumulator), FplInteger.valueOf(start) });
					start += delta;
				}
				return accumulator;
			}
		});

		scope.define(new AbstractFunction("map",
				"Apply a lambda to all list elements and return list with applied elements", "lambda", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				return list.map(new java.util.function.Function<FplValue, FplValue>() {

					@Override
					public FplValue apply(FplValue value) {
						return function.call(scope, FplLazy.makeEvaluated(scope, value));
					}
				});
			}
		});

		scope.define(
				new AbstractFunction("sort",
						"Sort a list. The lambda takes two arguments (left, right) and must return a number:"
								+ " < 0 if left < right, 0 for left = right and > 0 for left > right.",
						"lambda", "list") {

					@Override
					public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						Function function = evaluateToFunction(scope, parameters[0]);
						FplList list = evaluateToList(scope, parameters[1]);
						FplValue[] values = new FplValue[list.size()];
						Iterator<FplValue> iter = list.iterator();
						int i = 0;
						while (iter.hasNext()) {
							values[i++] = iter.next();
						}
						Arrays.sort(values, new Comparator<FplValue>() {

							@Override
							public int compare(FplValue left, FplValue right) {
								return (int) evaluateToLong(scope, function.call(scope,
										FplLazy.makeEvaluated(scope, left), FplLazy.makeEvaluated(scope, right)));
							}
						});

						return FplList.fromIterator(new Iterator<FplValue>() {
							int i = 0;

							@Override
							public boolean hasNext() {
								return i < values.length;
							}

							@Override
							public FplValue next() {
								return values[i++];
							}
						}, values.length);
					}
				});

		scope.define(new AbstractFunction("flat-map",
				"Apply a lambda to all list elements, the result of the lambda must be a list. Return list with applied elements of all returned lists.",
				"function", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				FplList f = list.flatMap(new java.util.function.Function<FplValue, FplList>() {

					@Override
					public FplList apply(FplValue value) {
						FplValue applied = function.call(scope, FplLazy.makeEvaluated(scope, value));
						if (applied instanceof FplList) {
							return (FplList) applied;
						} else {
							throw new EvaluationException("Not a list: " + applied);
						}
					}
				});
				return f;
			}
		});

		scope.define(new AbstractFunction("map-to-dict",
				"Apply a lambda to all list elements and return a dictionary. The dictionary is build from the results "
						+ "of the lambdas. The first must return the key as string, the second a value (any type). "
						+ "When the key is an empty string, the second lambda is not called and nothing is put to the dictionary. "
						+ "Adding to the dictionary is done by put, so mappings may overwrite each other or even remove mappings, "
						+ "when value is nil. " + "The first lambda receives a list element as parameter. "
						+ "The second lambda receives two parameters: The first is the previous value contained in the dictionary for the given "
						+ "key (may be nil if no mapping exists), the second the list element to be mapped.",
				"key-lambda", "value-lambda", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function keyLambda = evaluateToFunction(scope, parameters[0]);
				Function valueLambda = evaluateToFunction(scope, parameters[1]);
				FplList list = evaluateToList(scope, parameters[2]);
				FplMapDictionary dict = new FplMapDictionary();
				fillDictionaryWithMappedList(scope, keyLambda, valueLambda, list, dict);
				return dict;
			}
		});

		scope.define(new AbstractFunction("map-to-sorted-dict",
				"Apply a lambda to all list elements and return a sorted dictionary. The dictionary is build from the results "
						+ "of the lambdas. The first must return the key as string, the second a value (any type). "
						+ "When the key is an empty string, the second lambda is not called and nothing is put to the dictionary. "
						+ "Adding to the dictionary is done by put, so mappings may overwrite each other or even remove mappings, "
						+ "when value is nil. " + "The first lambda receives a list element as parameter. "
						+ "The second lambda receives two parameters: The first is the previous value contained in the dictionary for the given "
						+ "The third lambda controls the sorting of the dictionary. It takes two arguments (left, right) and must return a number: "
						+ "< 0 if left < right, 0 for left = right and > 0 for left > right. \n"
						+ "key (may be nil if no mapping exists), the second the list element to be mapped. When the thirs lambda is nil, "
						+ "natural string ordering is used. ",
				"key-lambda", "value-lambda", "sort-lambda", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function keyLambda = evaluateToFunction(scope, parameters[0]);
				Function valueLambda = evaluateToFunction(scope, parameters[1]);
				Function sortLambda = evaluateToFunctionOrNull(scope, parameters[2]);
				FplList list = evaluateToList(scope, parameters[3]);
				FplSortedDictionary dict = new FplSortedDictionary(Dictionary.createFplValueComparator(scope, sortLambda));
				fillDictionaryWithMappedList(scope, keyLambda, valueLambda, list, dict);
				return dict;
			}
		});

		scope.define(new AbstractFunction("reduce",
				"Reduce a list to one value. The function must accept two parameters: "
						+ "accumulator and value. It must return the \"reduction\" of accumulator and value.",
				"function", "accumulator", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplValue accumulator = evaluateToAny(scope, parameters[1]);
				FplList list = evaluateToList(scope, parameters[2]);
				for (FplValue value : list) {
					accumulator = function.call(scope, FplLazy.makeEvaluated(scope, accumulator),
							FplLazy.makeEvaluated(scope, value));
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
					if (isTrue(function.call(scope, FplLazy.makeEvaluated(scope, value)))) {
						results.add(value);
					}
				}
				return FplList.fromValues(results); // TODO: Change to
													// fromIterator to avoid
													// copying
			}
		});

		scope.define(new AbstractFunction("combine",
				"Take two lists as input, call a lambda with two parameters (elemenbt from first and second list) "
				+ "and return a list with the result of this lambda. In case the lists have different "
				+ "length, stop when the shorter list ends.",
				"lambda", "list-1", "list-2") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list1 = evaluateToList(scope, parameters[1]);
				FplList list2 = evaluateToList(scope, parameters[2]);
				int size = Math.min(list1.size(), list2.size());
				return FplList.fromIterator(new Iterator<FplValue>() {
					Iterator<FplValue> iterator1 = list1.iterator();
					Iterator<FplValue> iterator2 = list2.iterator();
					int i;

					@Override
					public boolean hasNext() {
						return i < size;
					}

					@Override
					public FplValue next() {
						FplValue value1 = iterator1.next();
						FplValue value2 = iterator2.next();
						i++;
						return function.call(scope, FplLazy.makeEvaluated(scope, value1), FplLazy.makeEvaluated(scope, value2));
					}
				}, size);
			}
		});

		scope.define(new AbstractFunction("split-by",
				"Split a list into a list of several lists. Each time the lambda returns true, a new list is started. "
				+ "The lambda is called with two arguments: A counter (starting at 0) and a list element. The result "
				+ "of the lambda for the call of the first list element is ignored.",
				"lambda", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				Iterator<FplValue> iterator = list.iterator();
				return FplList.fromIterator(new Iterator<FplValue>() {
					int counter = 0;
					FplValue nextElement;
					boolean nextElementValid;
					
					{
						if (iterator.hasNext()) {
							nextElement = iterator.next();
							nextElementValid = true;
							// The result for the first element is not needed, but we still ahve to call the lambda.
							function.call(scope, FplInteger.valueOf(counter++), FplLazy.makeEvaluated(scope, nextElement));
						} else {
							nextElementValid = false;
						}
					}

					@Override
					public boolean hasNext() {
						return nextElementValid;
					}

					@Override
					public FplValue next() {
						return computeNextSubList(iterator);
					}

					private FplList computeNextSubList(Iterator<FplValue> iterator) {
						int fromIndex = counter -1;
						int toIndex = fromIndex;
						boolean endReached = false;
						while (!endReached) {
							if (iterator.hasNext()) {
								nextElement = iterator.next();
								nextElementValid = true;
								endReached = isTrue(function.call(scope, FplInteger.valueOf(counter++), FplLazy.makeEvaluated(scope, nextElement)));
							} else {
								nextElement = null;
								nextElementValid = false;
								endReached = true;
							}
							toIndex++;
						}
						return list.subList(fromIndex, toIndex);
					}
				});
			}
		});

		scope.define(new AbstractFunction("group-by",
				"Convert a list in a dictionary of lists. The key is the result of the lambda."
				+ "The lambda is called with two arguments: A counter (starting at 0) and a list element. When the "
				+ "result of the lambda is nil, the corresponding element is ignored.",
				"lambda", "list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				FplDictionary dict = new FplMapDictionary();
				int i = 0;
				for (FplValue value : list) {
					FplValue key = function.call(scope, FplInteger.valueOf(i), FplLazy.makeEvaluated(scope, value));
					if (key != null) {
						FplList old = (FplList)dict.get(key);
						if (old == null) {
							dict.put(key, FplList.fromValue(value));
						} else {
							dict.put(key, old.addAtEnd(value)); 
						}
					}
					i++;
				}
				
				return dict;
			}
		});
	}

	private static void fillDictionaryWithMappedList(Scope scope, Function keyLambda, Function valueLambda, FplList list,
			FplDictionary dict) {
		for (FplValue value : list) {
			FplValue key = keyLambda.call(scope, FplLazy.makeEvaluated(scope, value));
			if (key != null) {
				FplValue old = dict.get(key);
				FplValue valueLambdaResult = valueLambda.call(scope, //
						FplLazy.makeEvaluated(scope, old), FplLazy.makeEvaluated(scope, value));
				dict.put(key, valueLambdaResult);
			}
		}
	}
}
