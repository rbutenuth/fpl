package de.codecentric.fpl.datatypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * Helper for function implementation.
 */
public abstract class AbstractFunction implements Named, PositionHolder, Function {
	public static final String FPL = "fpl";
	public static final Symbol QUOTE = new Symbol("quote");

	/** name, not null, not empty */
	protected final String name;

	/**
	 * Parameter names, last one does not end with "...", even when this is a
	 * variable argument function, with mapping to parameter index
	 */
	private Map<String, Integer> parameterNameToIndex;

	private Map<String, String> parameterComments;
	
	/** Minimum number of parameters, &gt;= 0 */
	private final int minimumNumberOfParameters;

	/** Variable argument list? */
	private final boolean varArg;

	private Position position;

	private String comment;

	/**
	 * @param position       Position in source code.
	 * @param name           Not null, not empty.
	 * @param comment        Comment in markdown syntax
	 * @param parameterNames Names of the parameters. If last ends with "...",
	 *                       function is variable argument function.
	 * @throws IllegalArgumentException When a name is empty/null or in case of duplicate parameter names.
	 */
	protected AbstractFunction(Position position, String name, String comment, String... parameterNames) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("empty or null name");
		}
		if (parameterNames.length == 0) {
			varArg = false;
		} else {
			varArg = parameterNames[parameterNames.length - 1].endsWith("...");
		}
		setPosition(position);
		this.comment = comment;
		this.name = name;
		Map<String, Integer> map = new LinkedHashMap<>();
		for (int i = 0; i < parameterNames.length; i++) {
			String param = parameterNames[i];
			if (i == parameterNames.length - 1 && param.endsWith("...")) {
				param = param.substring(0, param.length() - 3);
			}
			if (map.containsKey(param)) {
				throw new IllegalArgumentException("Duplicate parameter name: " + param);
			}
			map.put(param, i);
		}
		parameterNameToIndex = Collections.unmodifiableMap(map);
		parameterComments = new HashMap<>();
		minimumNumberOfParameters = varArg ? parameterNames.length - 1 : parameterNames.length;
	}

	/**
	 * This constructor should be used for internally implemented functions only.
	 * 
	 * @param name           Not null, not empty.
	 * @param comment        Comment in markdown syntax
	 * @param parameterNames Names of the parameters. If last ends with "...",
	 *                       function is variable argument function.
	 * @throws IllegalArgumentException When a name is empty/null or in case of duplicate parameter names.
	 */
	protected AbstractFunction(String name, String comment, String... parameterNames) {
		this(Position.INTERNAL, name, comment, parameterNames);
	}

	/**
	 * Call a function (with parameters). When this method is called, the number of
	 * parameters has already been checked. When there are not enough parameters,
	 * the method is not called, instead Currying takes place.
	 * @param scope Scope for evaluation.
	 * @param parameters The parameters of the function. Do not change the array
	 *                   elements!
	 * 
	 * @return The result of the function.
	 * @throws EvaluationException If execution fails.
	 */
	protected abstract FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException;

	@Override
	public FplValue call(Scope scope, FplValue... parameters) throws EvaluationException {
		int missing = checkNumberOfParameters(parameters);
		if (missing > 0) {
			if (parameters.length == 0) {
				return this;
			} else {
				return makeCurryFunction(scope, parameters, missing);
			}
		}
		return callInternal(scope, parameters);
	}

	/**
	 * @param parameters Parameters
	 * @return Number of missing parameters, caller has to do "currying"
	 * @throws EvaluationException If number of parameters is not correct.
	 */
	private int checkNumberOfParameters(FplValue[] parameters) throws EvaluationException {
		if (parameters.length < minimumNumberOfParameters) {
			return minimumNumberOfParameters - parameters.length;
		}
		if (minimumNumberOfParameters < parameters.length) {
			if (!varArg) {
				throw new EvaluationException(
						"Function " + name + ": Expect " + minimumNumberOfParameters + " parameters but got " + parameters.length);
			}
		}
		// numberOfParameters == parameters.length
		return 0;
	}

	/**
	 * Evaluate an expression.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return Evaluated expression.
	 * @throws EvaluationException
	 */
	public static FplValue evaluateToAny(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return null;
		} else {
			return expression.evaluate(scope);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplList}.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A list.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             list.
	 */
	public static FplList evaluateToList(Scope scope, FplValue expression) throws EvaluationException {
		FplValue value = expression.evaluate(scope);
		if (value instanceof FplList) {
			return (FplList) value;
		} else {
			throw new EvaluationException("Not a list: " + value);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplList}.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A list.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             list.
	 */
	public static FplList evaluateToListIfNotAlreadyList(Scope scope, FplValue expression) throws EvaluationException {
		if (expression instanceof FplList) {
			return (FplList) expression;
		} else {
			return evaluateToList(scope, expression);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link Function}.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A Function, never <code>null</code>
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             Function.
	 */
	public static Function evaluateToFunction(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			throw new EvaluationException("Not a function: " + expression);	
		}
		FplValue value = expression.evaluate(scope);
		if (value == null) {
			throw new EvaluationException("Not a function: " + expression);
		} else if (value instanceof Function) { 
			return (Function) value;
		} else {
			throw new EvaluationException("Not a function: " + value);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link Function}.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A Function or <code>null</code>
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             Function.
	 */
	public static Function evaluateToFunctionOrNull(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return null;
		}
		FplValue value = expression.evaluate(scope);
		if (value == null) {
			return null;
		} else if (value instanceof Function) {
			return (Function) value;
		} else {
			throw new EvaluationException("Not a lambda: " + value);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplObject}.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A FplObject.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             FplObject.
	 */
	public static FplObject evaluateToDictionary(Scope scope, FplValue expression) throws EvaluationException {
		FplValue value = expression.evaluate(scope);
		if (value instanceof FplObject) {
			return (FplObject) value;
		} else {
			throw new EvaluationException("Not a dictionary: " + value);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplObject}.
	 * <code>null</code> is replaced by an empty dictionary.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A FplObject.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             FplObject.
	 */
	public static FplObject evaluateToDictionaryNullDefaultsToEmpty(Scope scope, FplValue expression)
			throws EvaluationException {
		if (expression == null) {
			return new FplObject("dict");
		}
		FplValue value = expression.evaluate(scope);
		if (value == null) {
			return new FplObject("dict");
		} else if (value instanceof FplObject) {
			return (FplObject) value;
		} else {
			throw new EvaluationException("Not a dictionary: " + value);
		}
	}

	/**
	 * Evaluate an expression and cast the result to a {@link FplObject}, check it
	 * has a parent scope (the difference between a simple dictionary and an
	 * {@link FplObject} is the parent scope).
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return A FplObject.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             FplObject.
	 */
	public static FplObject evaluateToObject(Scope scope, FplValue expression) throws EvaluationException {
		FplObject object = evaluateToDictionary(scope, expression);
		if (object.getNext() == null) {
			throw new EvaluationException("Not an object: " + object);
		}
		return object;
	}

	/**
	 * Evaluate an expression, check for boolean value. <code>null</code> (nil), the
	 * integer/double value 0, and an empty list are <code>false</code>, everything
	 * else is <code>true</code>
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return Boolean value of expression.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             boolean.
	 */
	public static boolean evaluateToBoolean(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return false;
		}
		return isTrue(expression.evaluate(scope));
	}

	public static boolean isTrue(FplValue value) {
		if (value == null) {
			return false;
		} else if (value instanceof FplList) {
			return ((FplList) value).size() > 0;
		} else if (value instanceof FplInteger) {
			FplInteger i = (FplInteger) value;
			return i.getValue() != 0;
		} else if (value instanceof FplDouble) {
			FplDouble d = (FplDouble) value;
			return d.getValue() != 0;
		} else if (value instanceof FplString) {
			return ((FplString) value).getContent().length() > 0;
		} else {
			return true;
		}
	}

	/**
	 * Evaluate an expression, convert the result to <code>long</code>.
	 * <code>nil</code> evaluates to 0.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return long value of expression.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             number.
	 */
	public static long evaluateToLong(Scope scope, FplValue expression) throws EvaluationException {
		FplNumber value = evaluateToNumber(scope, expression);
		if (value instanceof FplInteger) {
			return ((FplInteger) value).getValue();
		} else { // must be FplDouble
			return (long) ((FplDouble) value).getValue();
		}
	}

	/**
	 * Evaluate an expression, convert the result - if possible - to
	 * {@link FplNumber}. <code>nil</code> evaluates to {@link FplInteger} with
	 * value 0.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return Value of expression.
	 * @throws EvaluationException If <code>expression</code> does not evaluate to a
	 *                             number.
	 */
	public static FplNumber evaluateToNumber(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return FplInteger.valueOf(0);
		}
		FplValue value = expression.evaluate(scope);
		if (value == null) {
			return FplInteger.valueOf(0);
		} else if (value instanceof FplNumber) {
			return (FplNumber) value;
		} else {
			throw new EvaluationException("Does not evaluate to number: " + expression);
		}
	}

	/**
	 * Evaluate an expression, convert the result to <code>FplString</code>.
	 * 
	 * @param scope      Scope used for evaluation.
	 * @param expression Expression to evaluate.
	 * @return long value of expression.
	 * @throws EvaluationException
	 */
	public static String evaluateToString(Scope scope, FplValue expression) throws EvaluationException {
		if (expression == null) {
			return "nil";
		}
		FplValue value = expression.evaluate(scope);
		if (value == null) {
			return "nil";
		} else if (value instanceof FplString) {
			return ((FplString) value).getContent();
		} else {
			return value.toString();
		}
	}

	public static FplValue quote(FplValue value) throws EvaluationException {
		FplValue result;
		if (value == null || value instanceof EvaluatesToThisValue) {
			result = value;
		} else {
			result = FplList.fromValues(AbstractFunction.QUOTE, value);
		}
		return result;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public String typeName() {
		return "function";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(name).append(" (");
		for (String p : parameterNameToIndex.keySet()) {
			sb.append(p).append(" ");
		}
		// remove the space at end
		if (!parameterNameToIndex.isEmpty()) {
			sb.deleteCharAt(sb.length() - 1);
		}
		if (varArg) {
			sb.append("...");
		}
		sb.append(") <code>)");
		return sb.toString();
	}
	
	public String getComment() {
		return comment;
	}

	/**
	 * @param position Position where function is defined. May be
	 *                 {@link Position#UNKNOWN}, but never null.
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
	 *         a variable argument function. For performance reasons, the Map is not
	 *         cloned. Don't try to change, it's unmodifiable.
	 */
	public Map<String, Integer> getParameterNameToIndex() {
		return parameterNameToIndex;
	}

	public String getParameterComment(String name) {
		return parameterComments.get(name);
	}

	public void setParameterComment(String name, String comment) {
		if (comment == null) {
			throw new IllegalArgumentException("null comment not allowed");
		}
		if (!parameterNameToIndex.containsKey(name)) {
			throw new IllegalArgumentException("Unknown parameter: "  + name);
		}
		parameterComments.put(name, comment);
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
		Iterator<String> iter = parameterNameToIndex.keySet().iterator();
		// Skip the parameters where we have the values
		for (int i = 0; i < parameters.length; i++) {
			iter.next();
		}
		// Copy the names of parameters where we don't have values
		int j = 0;
		while (iter.hasNext()) {
			String name = iter.next();
			if (varArg && !iter.hasNext()) {
				name = name + "...";
			}
			curryParameterNames[j++] = name;
		}
		FplValue[] givenParameters = new FplValue[parameters.length];
		for (int k = 0; k < givenParameters.length; k++) {
			givenParameters[k] = FplLazy.make(scope, parameters[k]);
		}
		return new CurryFunction(curryName, givenParameters, curryParameterNames);
	}

	private class CurryFunction extends AbstractFunction {
		private FplValue[] givenParameters;

		private CurryFunction(String name, FplValue[] lazies, String[] parameterNames) throws EvaluationException {
			super(name, AbstractFunction.this.getComment(), parameterNames);
			this.givenParameters = lazies;
		}

		@Override
		public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
			FplValue[] allParams = new FplValue[givenParameters.length + parameters.length];
			System.arraycopy(givenParameters, 0, allParams, 0, givenParameters.length);
			System.arraycopy(parameters, 0, allParams, givenParameters.length, parameters.length);
			return AbstractFunction.this.callInternal(scope, allParams);
		}
	}
}
