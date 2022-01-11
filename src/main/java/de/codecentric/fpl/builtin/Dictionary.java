package de.codecentric.fpl.builtin;

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
import de.codecentric.fpl.datatypes.FplMapDictionary;
import de.codecentric.fpl.datatypes.FplSortedDictionary;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;
import static de.codecentric.fpl.datatypes.AbstractFunction.evaluateToLong;

/**
 * Dictionary related functions.
 */
public class Dictionary implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction(
				"dict", "Create a new dictionary from string value pairs.",
				"pairs...") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters.length % 2 != 0) {
					throw new EvaluationException("Number of parameters must be even");
				}
				FplMapDictionary dict = new FplMapDictionary();
				for (int i = 0; i < parameters.length; i += 2) {
					String key = evaluateToString(scope, parameters[i]);
					FplValue value = evaluateToAny(scope, parameters[i+1]);
					try {
						dict.put(key, value);
					} catch (ScopeException e) {
						throw new EvaluationException(e.getMessage(), e);
					}
				}
				return dict;
			}
		});

		scope.define(new AbstractFunction(
				"sorted-dict", "Create a new sorted dictionary from string value pairs. \r\n"
				+ "The lambda sort takes two arguments (left, right) and must return a number:\"\r\n"
				+ " < 0 if left < right, 0 for left = right and > 0 for left > right.",
				"sort", "pairs...") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunctionOrNull(scope, parameters[0]);
				Comparator<String> comparator;
				if (parameters.length % 2 != 1) {
					throw new EvaluationException("Number of parameters must be odd (sort lamda plus key value pairs)");
				}
				comparator = createStringSortComparator(scope, function);

				FplSortedDictionary dict = new FplSortedDictionary(comparator);
				for (int i = 1; i < parameters.length; i += 2) {
					String key = evaluateToString(scope, parameters[i]);
					FplValue value = evaluateToAny(scope, parameters[i+1]);
					try {
						dict.put(key, value);
					} catch (ScopeException e) {
						throw new EvaluationException(e.getMessage(), e);
					}
				}
				return dict;
			}
		});

		scope.define(new AbstractFunction(
				"dict-put", "Put a value into the scope of an object or dictionary, " +
						"key must be a string, returns the old value associated with the key.",
				"dict", "key", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.put(evaluateToString(scope, parameters[1]), AbstractFunction.evaluateToAny(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-def", 
				"Define a value in the scope of an object or dictionary, " +
						"key must be a string, returns the value associated with the key, original mapping must be nil.",
				"dict", "key", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.define(evaluateToString(scope, parameters[1]),
							AbstractFunction.evaluateToAny(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-set", 
				"Change a value into the scope of an object or dictionary, " +
						"key must be a string," +
						"returns the old value associated with the key, new and old value must not be nil.",
				"dict", "key", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.replace(evaluateToString(scope, parameters[1]),
							AbstractFunction.evaluateToAny(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-get", "Get a value from the scope of an object or dictionary, " +
				"key must be a string.", "dict", "key") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return dict.get(evaluateToString(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("dict-keys", "Get all keys of an object or dictionary as a list.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary o = evaluateToDictionary(scope, parameters[0]);
				return FplList.fromIterator(new Iterator<FplValue>() {
					Iterator<String> iter = o.keySet().iterator();

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public FplValue next() {
						return new FplString(iter.next());
					}
				});
			}
		});

		scope.define(new AbstractFunction("dict-values", 
				"Get all values of an object or dictionary as a list.", "dict") {
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
					Iterator<Entry<String, FplValue>> iter = dict.entrieSet().iterator();

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public FplValue next() {
						Entry<String, FplValue> entry = iter.next();
						return FplList.fromValues(new FplString(entry.getKey()), entry.getValue());
					}
				});
			}
		});

		scope.define(new AbstractFunction("dict-size", 
				"The number of mappings in the dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
				return FplInteger.valueOf(dict.size());
			}
		});

		scope.define(new AbstractFunction("dict-peek-first-key", 
				"Returns the first key (random for unsorted dictionaries) of a dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return new FplString(dict.peekFirstKey());
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-peek-last-key", 
				"Returns the last key (random for unsorted dictionaries) of a dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return new FplString(dict.peekLastKey());
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-fetch-first-key", 
				"Returns and removes the first key (random for unsorted dictionaries) of a dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return new FplString(dict.fetchFirstKey());
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-fetch-last-key", 
				"Returns and removes the last key (random for unsorted dictionaries) of a dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return new FplString(dict.fetchLastKey());
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-fetch-first-value", 
				"Returns and removes the first value (random for unsorted dictionaries) of a dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.fetchFirstValue();
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-fetch-last-value", 
				"Returns and removes the last value (random for unsorted dictionaries) of a dictionary.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.fetchLastValue();
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-fetch-first-entry", 
				"Returns and removes the first entry (random for unsorted dictionaries) of a dictionary. \r\n"
				+ "The entry is a list of key and value.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.fetchFirstEntry();
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-fetch-last-entry", 
				"Returns and removes the last entry (random for unsorted dictionaries) of a dictionary. \r\n"
				+ "The entry is a list of key and value.",
				"dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplDictionary dict = evaluateToDictionary(scope, parameters[0]);
					return dict.fetchLastEntry();
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});
	}

	public static Comparator<String> createStringSortComparator(Scope scope, Function function) {
		Comparator<String> comparator;
		if (function == null) {
			comparator = null;
		} else {
			comparator = new Comparator<String>() {
				
				@Override
				public int compare(String left, String right) {
					return (int) evaluateToLong(scope,
							function.call(scope, // 
									new FplString(left),
									new FplString(right)));
				}
			};
		}
		return comparator;
	}

}
