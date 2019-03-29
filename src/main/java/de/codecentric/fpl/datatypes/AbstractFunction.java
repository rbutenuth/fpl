package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * Helper for function implementation.
 */
public abstract class AbstractFunction extends EvaluatesToThisValue implements Named, PositionHolder, Function {
	/** name, not null, not empty */
	private final String name;

	/**
	 * Parameter names, last one does not end with "...", even when this is a
	 * variable argument function
	 */
	private final String[] parameterNames;

	/** Minimum number of parameters, &gt;= 0 */
	private final int minimumNumberOfParameters;

	/** Variable argument list? */
	private final boolean varArg;

	private Position position;

	private List<String> comment;

	private final String[] parameterComments;

	/**
	 * @param position
	 *            Position in source code.
	 * @param comment
	 *            A list of lines with comments in markdown syntax
	 * @param name
	 *            Not null, not empty.
	 * @param parameterNames
	 *            Names of the parameters. If last ends with "...", function is
	 *            variable argument function.
	 */
	protected AbstractFunction(Position position, List<String> comment, String name, boolean varArg, String... parameterNames) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("empty or null name");
		}
		setPosition(position);
		this.comment = new ArrayList<>(comment);
		this.name = name;
		this.parameterNames = parameterNames.clone();
		parameterComments = new String[parameterNames.length];
		Arrays.fill(parameterComments, "");
		this.varArg = varArg;
		minimumNumberOfParameters = varArg ? parameterNames.length - 1 : parameterNames.length;
	}

	/**
	 * @param position
	 *            Position in source code.
	 * @param name
	 *            Not null, not empty.
	 * @param varArg
	 *            Is this a function with variable argument list?
	 * @param parameterNames
	 *            Names of the parameters. If last ends with "...", function is
	 *            variable argument function.
	 */
	protected AbstractFunction(Position position, String name, List<String> comment, boolean varArg, String... parameterNames) {
		this(position, comment, name, varArg, parameterNames);
	}

	/**
	 * @param name
	 *            Not null, not empty.
	 * @param varArg
	 *            Is this a function with variable argument list?
	 * @param parameterNames
	 *            Names of the parameters. If last ends with "...", function is
	 *            variable argument function.
	 */
	protected AbstractFunction(String name, List<String> comment, boolean varArg, String... parameterNames) {
		this(Position.UNKNOWN, comment, name, varArg, parameterNames);
	}

	public static List<String> comment(String... lines) {
		List<String> result = new ArrayList<>();
		for (String line : lines) {
			result.add(line);
		}
		return result;
	}

	/**
	 * Call a function (with parameters). When this method is called, the number of
	 * parameters has already been checked. When there are not enough parameters,
	 * the method is not called, instead Currying takes place.
	 * 
	 * @param scope
	 *            Evaluation scope.
	 * @param parameters
	 *            The parameters of the function. Do not change the array elements!
	 * @return The result of the function.
	 * @throws EvaluationException
	 *             If execution fails.
	 */
	protected abstract FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException;

	@Override
	public FplValue call(Scope scope, FplValue[] parameters) throws EvaluationException {
		int missing = checkNumberOfParameters(parameters);
		if (missing > 0) {
			if (parameters.length == 0) {
				return this;
			} else {
				return makeCurryFunction(scope, parameters, missing);
			}
		}
		try {
			return callInternal(scope, parameters);
		} catch (EvaluationException e) {
			e.add(new StackTraceElement(getClass().getName(), getName(), getPosition().getName(),
					getPosition().getLine()));
			throw e;
		}
	}

	/**
	 * @param parameters
	 *            Parameters
	 * @return Number of missing parameters, caller has to do "currying"
	 * @throws EvaluationException
	 *             If number of parameters is not correct.
	 */
	private int checkNumberOfParameters(FplValue[] parameters) throws EvaluationException {
		if (parameters.length < minimumNumberOfParameters) {
			return minimumNumberOfParameters - parameters.length;
		}
		if (minimumNumberOfParameters < parameters.length) {
			if (!varArg) {
				throw new EvaluationException(
						"Expect " + minimumNumberOfParameters + " parameters but got " + parameters.length);
			}
		}
		// numberOfParameters == parameters.length
		return 0;
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplList}.
	 * 
	 * @param scope
	 *            Scope used for evaluation.
	 * @param expression
	 *            Expression to evaluate.
	 * @return A list.
	 * @throws EvaluationException
	 *             If <code>expression</code> does not evaluate to a list.
	 */
	protected FplList evaluateToList(Scope scope, FplValue expression) throws EvaluationException {
		FplValue value = expression.evaluate(scope);
		if (value instanceof FplList) {
			return (FplList) value;
		} else {
			throw new EvaluationException("Not a list: " + value);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplLambda}.
	 * 
	 * @param scope
	 *            Scope used for evaluation.
	 * @param expression
	 *            Expression to evaluate.
	 * @return A FplLambda.
	 * @throws EvaluationException
	 *             If <code>expression</code> does not evaluate to a FplLambda.
	 */
	protected FplLambda evaluateToLambda(Scope scope, FplValue expression) throws EvaluationException {
		FplValue value = expression.evaluate(scope);
		if (value instanceof FplLambda) {
			return (FplLambda) value;
		} else {
			throw new EvaluationException("Not a lambda: " + value);
		}
	}

	/**
	 * Evaluate an expression, check for boolean value. <code>null</code> (nil), the
	 * integer value 0, and an empty list are <code>false</code>, everything else is
	 * <code>true</code>
	 * 
	 * @param scope
	 *            Scope used for evaluation.
	 * @param expression
	 *            Expression to evaluate.
	 * @return Boolean value of expression.
	 * @throws EvaluationException
	 *             If <code>expression</code> does not evaluate to a list.
	 */
	protected boolean evaluateToBoolean(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return false;
		}
		FplValue value = expression.evaluate(scope);
		if (value instanceof FplList) {
			return ((FplList) value).size() > 0;
		} else if (value instanceof FplInteger) {
			FplInteger i = (FplInteger) value;
			return i.getValue() != 0;
		} else {
			return false;
		}
	}

	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public String typeName() {
		return "function";
	}

	public List<String> getComment() {
		return Collections.unmodifiableList(comment);
	}

	/**
	 * @param position
	 *            Position where function is defined. May be
	 *            {@link Position#UNKNOWN}, but never null.
	 */
	public void setPosition(Position position) {
		this.position = position == null ? Position.UNKNOWN : position;
	}

	@Override
	public String getName() {
		return name;
	}

	public boolean isVarArg() {
		return varArg;
	}

	/**
	 * @return Number of parameters needed by this function. If {@link #isVararg()},
	 *         then you can provide more parameters.
	 */
	public int getMinimumNumberOfParameters() {
		return minimumNumberOfParameters;
	}

	public int getNumberOfParameterNames() {
		return minimumNumberOfParameters + (varArg ? 1 : 0);
	}

	/**
	 * @return Parameter names, last one does not end with "...", even when this is
	 *         a variable argument function
	 */
	public String[] getParameterNames() {
		return parameterNames.clone();
	}

	public String getParameterName(int index) {
		return parameterNames[index];
	}

	public String getParameterComment(int index) {
		return parameterComments[index];
	}

	public void setParameterComment(int index, String comment) {
		if (comment == null) {
			throw new IllegalArgumentException("null comment not allowed");
		}
		parameterComments[index] = comment;
	}

	/**
	 * @return Does this function accepts a variable number of parameters?
	 */
	public boolean isVararg() {
		return varArg;
	}

	public CurryFunction makeCurryFunction(Scope scope, FplValue[] parameters, int missing) throws EvaluationException {
		String curryName = name + "-curry-" + Integer.toString(missing);
		String[] curryParameterNames = new String[missing + (varArg ? 1 : 0)];
		System.arraycopy(parameterNames, parameters.length, curryParameterNames, 0, curryParameterNames.length);
		FplValue[] givenParameters = new FplValue[parameters.length];
		for (int i = 0; i < givenParameters.length; i++) {
			FplValue p = parameters[i];
			if (p instanceof EvaluatesToThisValue) {
				givenParameters[i] = p;
			} else {
				givenParameters[i] = new LazyExpression(scope, p);
			}
		}
		return new CurryFunction(curryName, givenParameters, curryParameterNames, varArg);
	}

	private class CurryFunction extends AbstractFunction {
		private FplValue[] givenParameters;

		private CurryFunction(String name, FplValue[] lazies, String[] parameterNames, boolean varArg) {
			super(name, AbstractFunction.this.getComment(), AbstractFunction.this.varArg, parameterNames);
			this.givenParameters = lazies;
		}

		@Override
		public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
			FplValue[] allParams = new FplValue[givenParameters.length + parameters.length];
			System.arraycopy(givenParameters, 0, allParams, 0, givenParameters.length);
			System.arraycopy(parameters, 0, allParams, givenParameters.length, parameters.length);
			return AbstractFunction.this.callInternal(scope, allParams);
		}
	}
}
