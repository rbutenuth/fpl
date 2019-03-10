package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.parser.Position;

/**
 * Functions like "set", "set-global", "let", etc.
 */
public class Assignment {

	/**
	 * @param scope Scope to which functions should be added.
	 * @throws ScopeException Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

		scope.put(new Function("put",
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

		scope.put(new Function("put-global",
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

		scope.put(new Function("set", comment("Reassign value in scope chain. nil as value not allowed"), false,
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					return scope.replace(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.put(new Function("def",
				comment("Assign value in local scope, it must be unassigned before. nil as value not allowed"), false,
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					return scope.define(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.put(new Function("def-field",
				comment("Assign value in the next object scope, it must be unassigned before. nil as value not allowed"), false,
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
					return s.define(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.put(new Function("def-global",
				comment("Assign value in global scope, it must be unassigned before. nil as value not allowed"), false,
				"symbol", "value") {
			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					Scope s = scope;
					while (s.getNext() != null) {
						s = s.getNext();
					}
					return s.define(targetName(scope, parameters[0]), value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.put(new Function("instance", comment("Create an instce of an object."), true, "key-value-pair...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (parameters.length % 2 != 0) {
					throw new EvaluationException("Number of parameters must be even");
				}
				int keyValueCount = parameters.length / 2;
				String[] keys = new String[keyValueCount];
				FplValue[] values = new FplValue[keyValueCount];
				for (int i = 0; i < keyValueCount; i++) {
					keys[i] = Assignment.targetName(scope, parameters[i * 2]);
					values[i] = Assignment.value(scope, parameters[i * 2 + 1]);
				}
				
				FplObject object = new FplObject(Position.UNKNOWN, scope);
				for (int i = 0; i < keyValueCount; i++) {
					try {
						object.put(keys[i], values[i]);
					} catch (ScopeException e) {
						throw new EvaluationException(e.getMessage());
					}
				}
				return object;
			}
		});
	}

	static private String targetName(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return null;
		} else if (expression instanceof Symbol) {
			return ((Symbol) expression).getName();
		} else if (expression instanceof Parameter) {
			return ((Parameter) expression).getName();
		} else {
			FplValue value = expression.evaluate(scope);
			if (value instanceof Symbol) {
				return ((Symbol) value).getName();
			} else {
				throw new EvaluationException("Not a symbol: " + value);
			}
		}
	}
	
	static private FplValue value(Scope scope, FplValue expression) throws EvaluationException {
		return expression == null ? null : expression.evaluate(scope);
	}
}
