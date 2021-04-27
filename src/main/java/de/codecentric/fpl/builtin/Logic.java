package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Basic logic functions. <code>LInteger(0)</code> and <code>null</code> are
 * false, everything else is true.
 */
public class Logic implements ScopePopulator {
	private final static FplInteger L_TRUE = FplInteger.valueOf(1);

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new LogicFunction("and", "Logic and of parameters."));
		scope.define(new LogicFunction("or", "Logic or of parameters."));
		scope.define(new LogicFunction("xor", "Logic xor of parameters."));
		scope.define(new LogicFunction("not", "Logic not of parameter."));

		scope.define(new AbstractFunction("is-symbol", "Is expression a symbol?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof Symbol ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-integer", "Is expression an integer?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof FplInteger ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-double", "Is expression a double?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof FplDouble ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-number", "Is expression an integer or double?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue value = evaluateToAny(scope, parameters[0]);
				return (value instanceof FplInteger || value instanceof FplDouble) ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-string", "Is expression a string?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof FplString ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-list", "Is expression a list?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof FplList ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-object", "Is expression an object?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof FplObject ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-function", "Is expression a function?", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return evaluateToAny(scope, parameters[0]) instanceof AbstractFunction ? L_TRUE : null;
			}
		});
	}

	private static class LogicFunction extends AbstractFunction {

		/**
		 * @param op Operator: "and", "or", etc.
		 */
		private LogicFunction(String op, String comment) throws EvaluationException {
			super(op, comment, "expression...");
		}

		/**
		 * @see AbstractFunction.data.Function#callInternal(lang.data.Scope,
		 *      FplValue.data.LObject[])
		 */
		@Override
		public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
			if (getName().equals("not")) {
				return evaluateToBoolean(scope, parameters[0]) ? null : L_TRUE;
			}
			boolean current = getName().equals("and");
			for (FplValue parameter : parameters) {
				boolean next = evaluateToBoolean(scope, parameter);
				switch (getName()) {
				case "and":
					current &= next;
					if (!current) {
						return null;
					}
					break;
				case "or":
					current |= next;
					if (current) {
						return L_TRUE;
					}
					break;
				default: // "xor":
					current ^= next;
					break;
				}
			}
			return current ? L_TRUE : null;
		}
	}
}