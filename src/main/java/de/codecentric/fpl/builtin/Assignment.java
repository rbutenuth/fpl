package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.ExceptionWrapper.wrapException;
import static de.codecentric.fpl.datatypes.AbstractFunction.evaluateToAny;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Functions like "set", "set-global", "let", etc.
 */
public class Assignment implements ScopePopulator {
	@Override
	public void populate(FplEngine engine) throws ScopeException, EvaluationException {
		Scope scope = engine.getScope();
		
		scope.define(new AbstractFunction("put",
				"Assign symbol to evluated value in current scope, deletes if value is null", "symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return put(scope, targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("match-put",
				"Do pattern matching in combination with deconstruction of lists. "
						+ "It's useful when you call functions which return a list of results. "
						+ "It returns if the match is successful, so you can combine it with `cond`.",
				"list-with-symbols", "list-with-values") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList target = evaluateToListIfNotAlreadyList(scope, parameters[0]);
				FplList value = evaluateToList(scope, parameters[1]);
				if (isMatch(target, value)) {
					putMatch(scope, target, value);
					return FplInteger.valueOf(1);
				} else {
					return FplInteger.valueOf(0);
				}
			}

			private boolean isMatch(FplList target, FplList value) {
				if (target.size() != value.size()) {
					return false;
				}
				for (int i = 0; i < target.size(); i++) {
					FplValue t = target.get(i);
					FplValue v = value.get(i);
					if (t instanceof FplList) {
						if (v instanceof FplList) {
							if (!isMatch((FplList) t, (FplList) v)) {
								return false;
							}
						} else {
							return false;
						}
					} else {
						// Just make sure that everything which is not list is a symbol.
						valueToSymbol(target.get(i));
					}

				}
				return true;
			}

			private void putMatch(Scope scope, FplList target, FplList value) {
				for (int i = 0; i < target.size(); i++) {
					FplValue t = target.get(i);
					FplValue v = value.get(i);
					if (t instanceof FplList) {
						putMatch(scope, (FplList) t, (FplList) v);
					} else {
						put(scope, ((Symbol) t).getName(), v);
					}
				}
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

		scope.define(new AbstractFunction("set", "Reassign value in scope chain. nil as value not allowed", "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return wrapException(() -> {
					return scope.replace(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				});
			}
		});

		scope.define(new AbstractFunction("def",
				"Assign value in current scope, it must be unassigned before. nil as value not allowed", "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return wrapException(() -> {
					return scope.define(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				});
			}
		});

		scope.define(new AbstractFunction("def-field",
				"Assign value in the next object scope, it must be unassigned before. nil as value not allowed",
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return wrapException(() -> {
					Scope s = scope;
					while (!(s instanceof FplObject) && s != null) {
						s = s.getNext();
					}
					if (s == null) {
						throw new EvaluationException("No object found");
					}
					return s.define(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				});
			}
		});

		scope.define(new AbstractFunction("def-global",
				"Assign value in global scope, it must be unassigned before. nil as value not allowed", "symbol",
				"value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return wrapException(() -> {
					Scope s = scope;
					while (s.getNext() != null) {
						s = s.getNext();
					}
					return s.define(targetName(scope, parameters[0]), evaluateToAny(scope, parameters[1]));
				});
			}
		});
	}

	public static FplValue put(Scope scope, String name, FplValue value) throws EvaluationException {
		return wrapException(() -> {
			return scope.put(name, value);
		});
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
