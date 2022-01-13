package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.AbstractFunction.evaluateToLong;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

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
 * Dictionary related functions.
 */
public class Dictionary implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("dict", "Create a new dictionary from string value pairs.", "pairs...") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters.length % 2 != 0) {
					throw new EvaluationException("Number of parameters must be even");
				}
				FplMapDictionary dict = new FplMapDictionary();
				for (int i = 0; i < parameters.length; i += 2) {
					FplValue key = evaluateToAnyNotNull(scope, parameters[i], "nil as key is not allowed");
					FplValue value = evaluateToAny(scope, parameters[i + 1]);
					dict.put(key, value);
				}
				return dict;
			}
		});

		scope.define(new AbstractFunction("sorted-dict",
				"Create a new sorted dictionary from string value pairs. \r\n"
						+ "The lambda sort takes two arguments (left, right) and must return a number:\"\r\n"
						+ " < 0 if left < right, 0 for left = right and > 0 for left > right.",
				"sort", "pairs...") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunctionOrNull(scope, parameters[0]);
				Comparator<FplValue> comparator;
				if (parameters.length % 2 != 1) {
					throw new EvaluationException("Number of parameters must be odd (sort lamda plus key value pairs)");
				}
				comparator = createFplValueComparator(scope, function);

				FplSortedDictionary dict = new FplSortedDictionary(comparator);
				for (int i = 1; i < parameters.length; i += 2) {
					FplValue key = evaluateToAnyNotNull(scope, parameters[i], "nil as key is not allowed");
					FplValue value = evaluateToAny(scope, parameters[i + 1]);
					dict.put(key, value);
				}
				return dict;
			}
		});

		scope.define(new AbstractFunction("dict-put",
				"Put a value into the scope of an object or dictionary, "
						+ "key must be a string, returns the old value associated with the key.",
				"dict", "key", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				FplValue key = evaluateToAnyNotNull(scope, parameters[1], "nil as key is not allowed");
				return dict.put(key, AbstractFunction.evaluateToAny(scope, parameters[2]));
			}
		});

		scope.define(new AbstractFunction("dict-def", "Define a value in the scope of an object or dictionary, "
				+ "key must be a string, returns the value associated with the key, original mapping must be nil.",
				"dict", "key", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				FplValue key = evaluateToAnyNotNull(scope, parameters[1], "nil as key is not allowed");
				return dict.define(key, AbstractFunction.evaluateToAny(scope, parameters[2]));
			}
		});

		scope.define(new AbstractFunction("dict-set",
				"Change a value into the scope of an object or dictionary, " + "key must be a string,"
						+ "returns the old value associated with the key, new and old value must not be nil.",
				"dict", "key", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				FplValue key = evaluateToAnyNotNull(scope, parameters[1], "nil as key is not allowed");
				return dict.replace(key, evaluateToAny(scope, parameters[2]));
			}
		});

		scope.define(new AbstractFunction("dict-get",
				"Get a value from the scope of an object or dictionary, " + "key must be a string.", "dict", "key") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				FplValue key = evaluateToAnyNotNull(scope, parameters[1], "nil as key is not allowed");
				return dict.get(key);
			}
		});

		scope.define(new AbstractFunction("dict-keys", "Get all keys of an object or dictionary as a list.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary o = evaluateToDictionary(scope, parameters[0]);
				return FplList.fromIterator(new Iterator<FplValue>() {
					Iterator<FplValue> iter = o.keySet().iterator();

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public FplValue next() {
						return iter.next();
					}
				});
			}
		});

		scope.define(
				new AbstractFunction("dict-values", "Get all values of an object or dictionary as a list.", "dict") {
					@Override
					protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
						return FplList.fromIterator(dict.values().iterator());
					}
				});

		scope.define(new AbstractFunction("dict-entries",
				"Get all entries of an object or dictionary as a list. Each entry is a list with two elements: key and value",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return FplList.fromIterator(new Iterator<FplValue>() {
					Iterator<Entry<FplValue, FplValue>> iter = dict.entrieSet().iterator();

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public FplValue next() {
						Entry<FplValue, FplValue> entry = iter.next();
						return FplList.fromValues(entry.getKey(), entry.getValue());
					}
				});
			}
		});

		scope.define(new AbstractFunction("dict-size", "The number of mappings in the dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return FplInteger.valueOf(dict.size());
			}
		});

		scope.define(new AbstractFunction("dict-peek-first-key",
				"Returns the first key (random for unsorted dictionaries) of a dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.peekFirstKey();
			}
		});

		scope.define(new AbstractFunction("dict-peek-last-key",
				"Returns the last key (random for unsorted dictionaries) of a dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.peekLastKey();
			}
		});

		scope.define(new AbstractFunction("dict-fetch-first-key",
				"Returns and removes the first key (random for unsorted dictionaries) of a dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.fetchFirstKey();
			}
		});

		scope.define(new AbstractFunction("dict-fetch-last-key",
				"Returns and removes the last key (random for unsorted dictionaries) of a dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.fetchLastKey();
			}
		});

		scope.define(new AbstractFunction("dict-fetch-first-value",
				"Returns and removes the first value (random for unsorted dictionaries) of a dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.fetchFirstValue();
			}
		});

		scope.define(new AbstractFunction("dict-fetch-last-value",
				"Returns and removes the last value (random for unsorted dictionaries) of a dictionary.", "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.fetchLastValue();
			}
		});

		scope.define(new AbstractFunction("dict-fetch-first-entry",
				"Returns and removes the first entry (random for unsorted dictionaries) of a dictionary. \r\n"
						+ "The entry is a list of key and value.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.fetchFirstEntry();
			}
		});

		scope.define(new AbstractFunction("dict-fetch-last-entry",
				"Returns and removes the last entry (random for unsorted dictionaries) of a dictionary. \r\n"
						+ "The entry is a list of key and value.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.fetchLastEntry();
			}
		});
	}

	public static Comparator<FplValue> createFplValueComparator(Scope scope, Function function) {
		Comparator<FplValue> comparator;
		if (function == null) {
			comparator = null;
		} else {
			comparator = new Comparator<FplValue>() {

				@Override
				public int compare(FplValue left, FplValue right) {
					return (int) evaluateToLong(scope, function.call(scope, //
							FplLazy.makeEvaluated(scope, left), FplLazy.makeEvaluated(scope, right)));
				}
			};
		}
		return comparator;
	}
}
