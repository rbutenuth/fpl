package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.AbstractFunction.evaluateToAny;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;

/**
 * Functions like "set", "set-global", "let", etc.
 */
public class Assignment implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("put", 
				"Assign symbol to evluated value in current scope, deletes if value is null", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return put(scope,targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("put-global", 
				"Assign symbol to evluated value in global scope, deletes if value is null", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Scope global = scope;
				while (global.getNext() != null) {
					global = global.getNext();
				}
				return put(global, targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("set", "Reassign value in scope chain. nil as value not allowed",
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					return scope.replace(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("def", 
				"Assign value in current scope, it must be unassigned before. nil as value not allowed", "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					return scope.define(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("def-field",  
				"Assign value in the next object scope, it must be unassigned before. nil as value not allowed", "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					Scope s = scope;
					while (!(s instanceof FplObject) && s != null) {
						s = s.getNext();
					}
					if (s == null) {
						throw new EvaluationException("No object found");
					}
					return s.define(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("def-global", 
				"Assign value in global scope, it must be unassigned before. nil as value not allowed", "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					Scope s = scope;
					while (s.getNext() != null) {
						s = s.getNext();
					}
					return s.define(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});
	}

	public static FplValue put(Scope scope, String name, FplValue value) throws EvaluationException {
		try {
			return scope.put(name, value);
		} catch (ScopeException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}
	
	/**
	 * Compute the name for an assignment. For a symbol/parameter it's the
	 * symbol/parameter name. For other expressions, evaluate it. Result must be a
	 * {@link FplString} or {@link Symbol}. Return the content/name.
	 * 
	 * @param scope      For the evaluation.
	 * @param expression Expression resulting in the name.
	 * @return see description.
	 * @throws EvaluationException
	 */
	static public String targetName(Scope scope, FplValue expression) throws EvaluationException {
		if (expression instanceof Symbol) {
			return ((Symbol) expression).getName();
		} else if (expression instanceof Parameter) {
			throw new EvaluationException("Parameter " + expression + " can't be a target.");
		} else {
			if (expression == null) {
				throw new EvaluationException("nil is not a valid name");
			}
			FplValue value = evaluateToAny(scope, expression);
			if (value instanceof Symbol) {
				return ((Symbol) value).getName();
			} else if (value instanceof FplString) {
				return ((FplString) value).getContent();
			} else {
				throw new EvaluationException("Not a symbol or string: " + value);
			}
		}
	}
}
