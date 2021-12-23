package de.codecentric.fpl.builtin;

import java.util.Iterator;
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDictionary;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

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
				FplObject dict = new FplObject("dict");
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

	}

}
