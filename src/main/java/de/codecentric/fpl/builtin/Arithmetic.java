package de.codecentric.fpl.builtin;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

/**
 * Basic arithmetic functions.
 */
public class Arithmetic extends Function {
	private final char op;

	/**
	 * @param scope
	 *            Scope to which functions should be added.
	 * @throws ScopeException
	 *             Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {
		scope.put(new Arithmetic('+', comment("Add values.")));
		scope.put(new Arithmetic('-', comment("Unary minus or subtract from first.")));
		scope.put(new Arithmetic('*', comment("Multiply values.")));
		scope.put(new Arithmetic('/', comment("Divide first value by following values.")));
		scope.put(new Arithmetic('%', comment("Modulo of first value by following values.")));
		scope.put(new Arithmetic('^', comment("Exponentiation of first value by following values.")));
	}

	/**
	 * @param op
	 *            Operator: +, -, *, /, %, ^
	 */
	private Arithmetic(char op, List<String> comment) {
		super("" + op, comment, true, op == '-' ? new String[] { "op" } : new String[] { "op1", "op2", "ops" });
		this.op = op;
	}

	/**
	 * @see lang.data.Function#callInternal(lang.data.Scope,
	 *      FplValue.data.LObject[])
	 */
	@Override
	public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
		try {
			FplValue value = parameters[0].evaluate(scope);
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
				throw new EvaluationException(op + " does not work on " + value.getClass().getSimpleName());
			}
			if (op == '-' && parameters.length == 1) {
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
				value = parameters[i].evaluate(scope);
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
					throw new EvaluationException(op + " does not work on " + value.getClass().getSimpleName());
				}

				if (isDouble) {
					switch (op) {
					case '+':
						doubleAccumulator += doubleNext;
						break;
					case '-':
						doubleAccumulator -= doubleNext;
						break;
					case '*':
						doubleAccumulator *= doubleNext;
						break;
					case '/':
						doubleAccumulator /= doubleNext;
						break;
					case '%':
						doubleAccumulator %= doubleNext;
						break;
					case '^':
						doubleAccumulator = Math.round(Math.pow(doubleAccumulator, doubleNext));
						break;
					}
				} else { // BigInteger
					switch (op) {
					case '+':
						intAccumulator = intAccumulator + intNext;
						break;
					case '-':
						intAccumulator = intAccumulator - intNext;
						break;
					case '*':
						intAccumulator = intAccumulator * intNext;
						break;
					case '/':
						intAccumulator = intAccumulator / intNext;
						break;
					case '%':
						intAccumulator = intAccumulator % intNext;
						break;
					case '^':
						intAccumulator = intAccumulator ^ intNext;
						break;
					}
				}
			}
			return isDouble ? new FplDouble(doubleAccumulator) : FplInteger.valueOf(intAccumulator);
		} catch (ArithmeticException ae) {
			throw new EvaluationException(ae);
		}
	}

}
