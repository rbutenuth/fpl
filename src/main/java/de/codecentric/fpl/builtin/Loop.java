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
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplSortedDictionary;
import de.codecentric.fpl.datatypes.FplString;
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
				FplObject dict = new FplObject("dict");
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
				FplSortedDictionary dict = new FplSortedDictionary(Dictionary.createStringSortComparator(scope, sortLambda));
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
	}

	private static void fillDictionaryWithMappedList(Scope scope, Function keyLambda, Function valueLambda, FplList list,
			FplDictionary dict) {
		for (FplValue value : list) {
			FplValue keyLambdaResult = keyLambda.call(scope, FplLazy.makeEvaluated(scope, value));
			String key;
			if (keyLambdaResult == null) {
				key = "";
			} else if (keyLambdaResult instanceof FplString) {
				key = ((FplString) keyLambdaResult).getContent();
			} else {
				key = keyLambdaResult.toString();
			}
			if (!key.isEmpty()) {
				FplValue old = dict.get(key);
				FplValue valueLambdaResult = valueLambda.call(scope, //
						FplLazy.makeEvaluated(scope, old), FplLazy.makeEvaluated(scope, value));
				dict.put(key, valueLambdaResult);
			}
		}
	}
}
