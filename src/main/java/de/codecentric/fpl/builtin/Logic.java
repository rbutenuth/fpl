package de.codecentric.fpl.builtin;

import java.util.List;

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
	public void populate(Scope scope) throws ScopeException {

		scope.define(new LogicFunction("and", comment("Logic and of parameters.")));
		scope.define(new LogicFunction("or", comment("Logic or of parameters.")));
		scope.define(new LogicFunction("xor", comment("Logic xor of parameters.")));
		scope.define(new LogicFunction("not", comment("Logic not of parameter.")));

		scope.define(new AbstractFunction("is-symbol", comment("Is expression a symbol?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof Symbol ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-integer", comment("Is expression an integer?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof FplInteger ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-double", comment("Is expression a double?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof FplDouble ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-string", comment("Is expression a string?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof FplString ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-list", comment("Is expression a list?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof FplList ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-object", comment("Is expression an object?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof FplObject ? L_TRUE : null;
			}
		});

		scope.define(new AbstractFunction("is-function", comment("Is expression a function?"), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return parameters[0].evaluate(scope) instanceof AbstractFunction ? L_TRUE : null;
			}
		});
	}

	private static class LogicFunction extends AbstractFunction {

		/**
		 * @param op Operator: "and", "or", etc.
		 */
		private LogicFunction(String op, List<String> comment) {
			super(op, comment, !op.equals("not"), "expression");
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
				case "xor":
					current ^= next;
					break;
				}
			}
			return current ? L_TRUE : null;
		}
	}
}