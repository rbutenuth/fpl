package de.codecentric.fpl.builtin;

import java.util.Iterator;
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
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
				"dict-put", "Put a value into the scope of an object or dictionary, " +
						"symbol can be a symbol or a string, returns the old value associated with the symbol/key.",
				false, "dict", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplObject o = evaluateToDictionary(scope, parameters[0]);
					return o.put(Assignment.targetName(scope, parameters[1]), Assignment.value(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-def", 
				"Define a value in the scope of an object or dictionary, " +
						"symbol can be a symbol or a string, returns the value associated with the symbol/key, original mapping must be nil.",
				false, "dict", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplObject o = evaluateToDictionary(scope, parameters[0]);
					return o.define(Assignment.targetName(scope, parameters[1]),
							Assignment.value(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-set", 
				"Change a value into the scope of an object or dictionary, " +
						"symbol can be a symbol or a string," +
						"returns the old value associated with the symbol/key, new and old value must not be nil.",
				false, "dict", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					FplObject o = evaluateToDictionary(scope, parameters[0]);
					return o.replace(Assignment.targetName(scope, parameters[1]),
							Assignment.value(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-get", "Get a value from the scope of an object or dictionary, " +
				"symbol can be a symbol or a string.", false, "dict", "symbol") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplObject o = evaluateToDictionary(scope, parameters[0]);
				return o.get(Assignment.targetName(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("dict-keys", "Get all keys of an object or dictionary as a list.",
				false, "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplObject o = evaluateToDictionary(scope, parameters[0]);
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
				"Get all values of an object or dictionary as a list.", false, "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplObject o = evaluateToDictionary(scope, parameters[0]);
				return FplList.fromIterator(o.values().iterator());
			}
		});

		scope.define(new AbstractFunction("dict-entries", 
				"Get all entries of an object or dictionary as a list. Each entry is a list with two elements: key and value",
				false, "dict") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplObject o = evaluateToDictionary(scope, parameters[0]);
				return FplList.fromIterator(new Iterator<FplValue>() {
					Iterator<Entry<String, FplValue>> iter = o.entrieSet().iterator();

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
