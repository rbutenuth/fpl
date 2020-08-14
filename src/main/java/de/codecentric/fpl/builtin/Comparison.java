package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Basic comparison functions.
 */
public class Comparison implements ScopePopulator {
	private static FplInteger TRUE = FplInteger.valueOf(1);

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {
		scope.define(new ComparisonFunction(CompareOperator.EQ, "Compare for equal."));
		scope.define(new ComparisonFunction(CompareOperator.NE, "Compare for not equal."));
		scope.define(new ComparisonFunction(CompareOperator.LT, "Compare left less than right."));
		scope.define(new ComparisonFunction(CompareOperator.LE, "Compare lest less or equal than right."));
		scope.define(new ComparisonFunction(CompareOperator.GT, "Compare left greater than right."));
		scope.define(new ComparisonFunction(CompareOperator.GE, "Compare left greater or equal than right."));
	}

	private enum CompareOperator {
		EQ {
			@Override
			FplValue compare(long left, long right) {
				return left == right ? TRUE : null;
			}

			@Override
			FplValue compare(double left, double right) {
				return left == right ? TRUE : null;
			}

			@Override
			FplValue compare(String left, String right) {
				return left.equals(right) ? TRUE : null;
			}

			@Override
			String symbol() {
				return "eq";
			}
		},
		NE {
			@Override
			FplValue compare(long left, long right) {
				return left != right ? TRUE : null;
			}

			@Override
			FplValue compare(double left, double right) {
				return left != right ? TRUE : null;
			}

			@Override
			FplValue compare(String left, String right) {
				return left.equals(right) ? null : TRUE;
			}

			@Override
			String symbol() {
				return "ne";
			}
		},
		LT {
			@Override
			FplValue compare(long left, long right) {
				return left < right ? TRUE : null;
			}

			@Override
			FplValue compare(double left, double right) {
				return left < right ? TRUE : null;
			}

			@Override
			FplValue compare(String left, String right) {
				return left.compareTo(right) < 0 ? TRUE : null;
			}

			@Override
			String symbol() {
				return "lt";
			}
		},
		LE {
			@Override
			FplValue compare(long left, long right) {
				return left <= right ? TRUE : null;
			}

			@Override
			FplValue compare(double left, double right) {
				return left <= right ? TRUE : null;
			}

			@Override
			FplValue compare(String left, String right) {
				return left.compareTo(right) <= 0 ? TRUE : null;
			}

			@Override
			String symbol() {
				return "le";
			}
		},
		GT {
			@Override
			FplValue compare(long left, long right) {
				return left > right ? TRUE : null;
			}

			@Override
			FplValue compare(double left, double right) {
				return left > right ? TRUE : null;
			}

			@Override
			FplValue compare(String left, String right) {
				return left.compareTo(right) > 0 ? TRUE : null;
			}

			@Override
			String symbol() {
				return "gt";
			}
		},
		GE {
			@Override
			FplValue compare(long left, long right) {
				return left >= right ? TRUE : null;
			}

			@Override
			FplValue compare(double left, double right) {
				return left >= right ? TRUE : null;
			}

			@Override
			FplValue compare(String left, String right) {
				return left.compareTo(right) >= 0 ? TRUE : null;
			}

			@Override
			String symbol() {
				return "ge";
			}
		};

		abstract FplValue compare(long left, long right);

		abstract FplValue compare(double left, double right);

		abstract FplValue compare(String left, String right);

		abstract String symbol();
	}

	private static class ComparisonFunction extends AbstractFunction {
		private CompareOperator operator;

		private ComparisonFunction(CompareOperator operator, String comment) throws EvaluationException {
			super(operator.symbol(), comment, false, "left", "right");
			this.operator = operator;
		}

		/**
		 * @see AbstractFunction.data.Function#callInternal(lang.data.Scope,
		 *      FplValue.data.LObject[])
		 */
		@Override
		public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
			FplValue left = parameters[0] == null ? null : parameters[0].evaluate(scope);
			FplValue right = parameters[1] == null ? null : parameters[1].evaluate(scope);

			if (left == null) {
				if (right == null) {
					switch (operator) {
					case EQ:
						return TRUE;
					case NE:
						return null;
					default:
						throw new EvaluationException("Comparison with null");
					}
				} else { // left == null, right != null
					switch (operator) {
					case EQ:
						return null;
					case NE:
						return TRUE;
					default:
						throw new EvaluationException("Comparison with null");
					}
				}
			} else { // left != null
				if (right == null) {
					switch (operator) {
					case EQ:
						return null;
					case NE:
						return TRUE;
					default:
						throw new EvaluationException("Comparison with null");
					}
				} else { // left != null, right != null
					return compareValues(left, right);
				}
			}
		}

		private FplValue compareValues(FplValue left, FplValue right) {
			// Precondition: left != null && right != null
			if (left instanceof FplInteger) {
				if (right instanceof FplInteger) {
					return operator.compare(((FplInteger) left).getValue(), ((FplInteger) right).getValue());
				} else if (right instanceof FplDouble) {
					return operator.compare(((FplInteger) left).getValue(), ((FplDouble) right).getValue());
				} else if (right instanceof FplString) {
					switch (operator) {
					case EQ:
						return null;
					case NE:
						return TRUE;
					default:
						return null;
					}
				} else {
					return null;
				}
			} else if (left instanceof FplDouble) {
				if (right instanceof FplInteger) {
					return operator.compare(((FplDouble) left).getValue(), ((FplInteger) right).getValue());
				} else if (right instanceof FplDouble) {
					return operator.compare(((FplDouble) left).getValue(), ((FplDouble) right).getValue());
				} else if (right instanceof FplString) {
					switch (operator) {
					case EQ:
						return null;
					case NE:
						return TRUE;
					default:
						return null;
					}
				} else {
					return null;
				}
			} else if (left instanceof FplString) {
				if (right instanceof FplInteger || right instanceof FplDouble) {
					switch (operator) {
					case EQ:
						return null;
					case NE:
						return TRUE;
					default:
						return null;
					}
				} else if (right instanceof FplString) {
					return operator.compare(((FplString) left).getContent(), ((FplString) right).getContent());
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}
}
