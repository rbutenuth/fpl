package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;

/**
 * Functions like "set", "set-global", "let", etc.
 */
public class Assignment {

	/**
	 * @param scope Scope to which functions should be added.
	 * @throws ScopeException Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

		scope.put(new AssignmentFunction("put",
				comment("Assign symbol to evluated value in current scope, deletes if value is null"), "symbol",
				"value") {
			@Override
			protected void scopeAction(Scope scope, String name, FplValue value) throws ScopeException {
				scope.put(name, value);
			}
		});

		scope.put(new AssignmentFunction("put-global",
				comment("Assign symbol to evluated value in global scope, deletes if value is null"), "symbol",
				"value") {
			@Override
			protected void scopeAction(Scope scope, String name, FplValue value) throws ScopeException {
				scope.putGlobal(name, value);
			}
		});

		scope.put(new AssignmentFunction("set", comment("Reassign value in scope chain. nil as value not allowed"),
				"symbol", "value") {
			@Override
			protected void scopeAction(Scope scope, String name, FplValue value) throws ScopeException {
				scope.change(name, value);
			}
		});

		scope.put(new AssignmentFunction("def",
				comment("Assign value in local scope, it must be unassigned before. nil as value not allowed"),
				"symbol", "value") {
			@Override
			protected void scopeAction(Scope scope, String name, FplValue value) throws ScopeException {
				scope.define(name, value);
			}
		});
	}

	private static abstract class AssignmentFunction extends Function {

		protected AssignmentFunction(String name, List<String> comment, String... parameterNames) {
			super(name, comment, false, parameterNames);
		}

		@Override
		public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
			try {
				scopeAction(scope, targetName(scope, parameters[0]), value(scope, parameters[1]));
			} catch (ScopeException e) {
				throw new EvaluationException(e);
			}
			return value(scope, parameters[1]);
		}
			
		protected abstract void scopeAction(Scope scope, String name, FplValue value) throws ScopeException;
	}
	
	static String targetName(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			throw new EvaluationException("nil not valid name for assignment");
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
	
	static FplValue value(Scope scope, FplValue expression) throws EvaluationException {
		return expression == null ? null : expression.evaluate(scope);
	}
}
