package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Dictionary related functions.
 */
public class Dictionary implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("dict-put", comment("Put a value into the scope of an object or dictionary,",
				"symbol can be a symbol or a string,",
				"returns the old value associated with the symbol/key."), false, "dict", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					FplObject o = evaluateToDictionary(scope, parameters[0]);
					return o.put(Assignment.targetName(scope, parameters[1]), Assignment.value(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-def", comment("Define a value in the scope of an object or dictionary,",
				"symbol can be a symbol or a string,",
				"returns the value associated with the symbol/key, original mapping must be nil."), false, "dict", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					FplObject o = evaluateToDictionary(scope, parameters[0]);
					return o.define(Assignment.targetSymbol(scope, parameters[1]), Assignment.value(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-set", comment("Change a value into the scope of an object or dictionary,",
				"symbol can be a symbol or a string,",
				"returns the old value associated with the symbol/key, new and old value must not be nil."), false, "dict", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					FplObject o = evaluateToDictionary(scope, parameters[0]);
					return o.replace(Assignment.targetName(scope, parameters[1]), Assignment.value(scope, parameters[2]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("dict-get", comment("Get a value from the scope of an object or dictionary,",
				"symbol can be a symbol or a string."), false, "dict", "symbol") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplObject o = evaluateToDictionary(scope, parameters[0]);
				return o.get(Assignment.targetName(scope, parameters[1]));
			}
		});
	}

}
