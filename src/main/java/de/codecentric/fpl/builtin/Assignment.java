package de.codecentric.fpl.builtin;

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
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("put",
				comment("Assign symbol to evluated value in current scope, deletes if value is null"), false, "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					return scope.put(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("put-global",
				comment("Assign symbol to evluated value in global scope, deletes if value is null"), false, "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Scope global = scope;
				while (global.getNext() != null) {
					global = global.getNext();
				}
				try {
					return global.put(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("set", comment("Reassign value in scope chain. nil as value not allowed"),
				false, "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					return scope.replace(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("def",
				comment("Assign value in current scope, it must be unassigned before. nil as value not allowed"), false,
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					return scope.define(targetSymbol(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("def-field", comment(
				"Assign value in the next object scope, it must be unassigned before. nil as value not allowed"), false,
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					Scope s = scope;
					while (!(s instanceof FplObject) && s != null) {
						s = s.getNext();
					}
					if (s == null) {
						throw new EvaluationException("No object found");
					}
					return s.define(targetSymbol(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("def-global",
				comment("Assign value in global scope, it must be unassigned before. nil as value not allowed"), false,
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					Scope s = scope;
					while (s.getNext() != null) {
						s = s.getNext();
					}
					return s.define(targetSymbol(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});
	}

	static public String targetName(Scope scope, FplValue expression) throws EvaluationException {
		return targetSymbol(scope, expression).getName();
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
	static public Symbol targetSymbol(Scope scope, FplValue expression) throws EvaluationException {
		if (expression instanceof Symbol) {
			return (Symbol) expression;
		} else if (expression instanceof Parameter) {
			throw new EvaluationException("Parameter " + expression + " can't be a target.");
		} else {
			if (expression == null) {
				throw new EvaluationException("nil is not a valid name");
			}
			FplValue value = expression.evaluate(scope);
			if (value instanceof Symbol) {
				return (Symbol) value;
			} else if (value instanceof FplString) {
				String s = ((FplString) value).getContent();
				return new Symbol(s);
			} else {
				throw new EvaluationException("Not a symbol or string: " + value);
			}
		}
	}

	/**
	 * @param scope      For the evaluation
	 * @param expression Expression to evaluate
	 * @return <code>null</code> for nil input, otherwise evaluated expression.
	 * @throws EvaluationException
	 */
	static public FplValue value(Scope scope, FplValue expression) throws EvaluationException {
		return expression == null ? null : expression.evaluate(scope);
	}
}
