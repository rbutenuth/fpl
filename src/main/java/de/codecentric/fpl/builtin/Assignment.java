package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Symbol;

/**
 * Functions like "set", "set-global", "let", etc.
 */
public class Assignment {

	/**
	 * @param scope
	 *            Scope to which functions should be added.
	 * @throws ScopeException
	 *             Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

		scope.put(new AssignmentFunction("put",
				comment("Assign symbol to evluated value in current scope, deletes if value is null"), "symbol", "value") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String name = targetName(scope, parameters[0]);
				try {
					scope.put(name, value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e);
				}
				return value(scope, parameters[1]);
			}
		});

		scope.put(new AssignmentFunction("put-global",
				comment("Assign symbol to evluated value in global scope, deletes if value is null"), "symbol", "value") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String name = targetName(scope, parameters[0]);
				try {
					scope.putGlobal(name, value(scope, parameters[1]));
				} catch (ScopeException e) {
					throw new EvaluationException(e);
				}
				return value(scope, parameters[1]);
			}
		});

	}

	private static abstract class AssignmentFunction extends Function {

		protected AssignmentFunction(String name, List<String> comment, String... parameterNames) {
			super(name, comment, false, parameterNames);
		}

		protected String targetName(Scope scope, FplValue expression) throws EvaluationException {
			if (expression instanceof Symbol) {
				return ((Symbol) expression).getName();
			} else {
				FplValue value = expression.evaluate(scope);
				if (value instanceof Symbol) {
					return ((Symbol) value).getName();
				} else {
					throw new EvaluationException("Not a symbol: " + value);
				}
			}
		}

		protected FplValue value(Scope scope, FplValue expression) throws EvaluationException {
			return expression == null ? null : expression.evaluate(scope);
		}
	}
}
