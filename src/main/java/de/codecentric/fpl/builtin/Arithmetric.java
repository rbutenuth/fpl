package de.codecentric.fpl.builtin;

import java.math.BigInteger;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;

/**
 * Basic arithmetic functions.
 */
public class Arithmetric implements ScopePopulator {

	@Override
	public void populate(Scope scope) throws ScopeException {
		scope.define(new ArithmetricFunction(ArithmeticOperator.PLUS));
		scope.define(new ArithmetricFunction(ArithmeticOperator.MINUS));
		scope.define(new ArithmetricFunction(ArithmeticOperator.TIMES));
		scope.define(new ArithmetricFunction(ArithmeticOperator.DIVIDE));
		scope.define(new ArithmetricFunction(ArithmeticOperator.MODULO));
		scope.define(new ArithmetricFunction(ArithmeticOperator.EXP));
	}

	private enum ArithmeticOperator {
		PLUS("+", "Add values.") {
			@Override
			double execute(double left, double right) {
				return left + right;
			}

			@Override
			long execute(long left, long right) {
				return left + right;
			}
		},
		MINUS("-", "Unary minus or subtract from first.") {
			@Override
			double execute(double left, double right) {
				return left - right;
			}

			@Override
			long execute(long left, long right) {
				return left - right;
			}
		},
		TIMES("*", "Multiply values.") {
			@Override
			double execute(double left, double right) {
				return left * right;
			}

			@Override
			long execute(long left, long right) {
				return left * right;
			}
		},
		DIVIDE("/", "Divide first value by following values.") {
			@Override
			double execute(double left, double right) {
				return left / right;
			}

			@Override
			long execute(long left, long right) {
				return left / right;
			}
		},
		MODULO("%", "Modulo of first value by following values.") {
			@Override
			double execute(double left, double right) {
				return left % right;
			}

			@Override
			long execute(long left, long right) {
				return left % right;
			}
		},
		EXP("**", "Exponentiation of first value by following values.") {
			@Override
			double execute(double left, double right) {
				return Math.pow(left, right);
			}

			@Override
			long execute(long left, long right) {
				return new BigInteger(Long.toString(left)).pow((int) right).longValue();
			}
		};

		private String name;
		private String comment;

		ArithmeticOperator(String name, String comment) {
			this.name = name;
			this.comment = comment;
		}

		abstract double execute(double left, double right);

		abstract long execute(long left, long right);

		@Override
		public String toString() {
			return name;
		}
	}

	private static class ArithmetricFunction extends AbstractFunction {
		private final ArithmeticOperator op;

		/**
		 * @param op Operator: +, -, *, /, %, **
		 */
		ArithmetricFunction(ArithmeticOperator op) {
			super(op.name, comment(op.comment), true,
					op == ArithmeticOperator.MINUS ? new String[] { "op" } : new String[] { "op1", "op2", "ops" });
			this.op = op;
		}

		/**
		 * @see AbstractFunction.data.Function#callInternal(lang.data.Scope,
		 *      FplValue.data.LObject[])
		 */
		@Override
		public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
			try {
				FplValue value = parameters[0] == null ? null : parameters[0].evaluate(scope);
				boolean isDouble;
				long intAccumulator = 0;
				double doubleAccumulator = 0;
				if (value instanceof FplInteger) {
					isDouble = false;
					intAccumulator = ((FplInteger) value).getValue();
				} else if (value instanceof FplDouble) {
					isDouble = true;
					doubleAccumulator = ((FplDouble) value).getValue();
				} else {
					throw new EvaluationException(
							op + " does not work on " + (value == null ? "nil" : value.getClass().getSimpleName()));
				}
				if (op == ArithmeticOperator.MINUS && parameters.length == 1) {
					// unary minus
					if (isDouble) {
						return new FplDouble(-doubleAccumulator);
					} else {
						return FplInteger.valueOf(-intAccumulator);
					}
				}
				for (int i = 1; i < parameters.length; i++) {
					long intNext = 0;
					double doubleNext = 0;
					value = parameters[i] == null ? null : parameters[i].evaluate(scope);
					if (value instanceof FplInteger) {
						if (isDouble) {
							doubleNext = ((FplInteger) value).getValue();
						} else {
							intNext = ((FplInteger) value).getValue();
						}
					} else if (value instanceof FplDouble) {
						if (!isDouble) {
							// Switch to double mode
							isDouble = true;
							doubleAccumulator = intAccumulator;
						}
						doubleNext = ((FplDouble) value).getValue();
					} else {
						throw new EvaluationException(
								op + " does not work on " + (value == null ? "nil" : value.getClass().getSimpleName()));
					}

					if (isDouble) {
						doubleAccumulator = op.execute(doubleAccumulator, doubleNext);
					} else { // long
						intAccumulator = op.execute(intAccumulator, intNext);
					}
				}
				return isDouble ? new FplDouble(doubleAccumulator) : FplInteger.valueOf(intAccumulator);
			} catch (ArithmeticException ae) {
				throw new EvaluationException(ae);
			}
		}
	}
}
