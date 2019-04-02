package de.codecentric.fpl.datatypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * Code examples:
 *
 * <pre>
 * (if nil (list 1 2 "foo") (list 3 4 "bar"))
 * </pre>
 *
 * evaluates to
 *
 * <pre>
 * (3 4 "bar")
 * </pre>
 *
 * <pre>
 * (lambda (arg) (+ arg 1)) ((lambda (arg) (+ arg 1)) 5)
 * </pre>
 *
 * <pre>
 * (def-function should-be-constant ()
 *  '(one two three))
 * </pre>
 *
 * <pre>
 * (def-function factorial (n)
 *    (if (le n 1)
 *       1
 *       (* n (factorial (- n 1)))
 *    )
 * )
 * </pre>
 */
public class FplLambda extends AbstractFunction {
	private final FplValue[] code;

	/**
	 * @param position
	 *            Position in source code.
	 * @param comment
	 *            A list of lines with comment in markdown syntax
	 * @param name
	 *            Name of the function.
	 * @param paramNames
	 *            Names of parameters, if last one ends with "...", this is a
	 *            variable list function.
	 * @param code
	 *            The Lisp code.
	 */
	public FplLambda(Position position, List<String> comment, String name, String[] paramNames, FplValue[] code)
			throws EvaluationException {
		super(position, name, comment, varArgs(paramNames), convertedParamNames(paramNames));
		Map<String, Integer> parameterMap = createParameterMap();
		this.code = compile(code, parameterMap);
	}

	private static boolean varArgs(String[] paramNames) {
		return paramNames.length > 0 && paramNames[paramNames.length - 1].endsWith("...");
	}

	private static String[] convertedParamNames(String[] paramNames) {
		if (varArgs(paramNames)) {
			String[] converted = new String[paramNames.length];
			int last = paramNames.length - 1;
			System.arraycopy(paramNames, 0, converted, 0, last);
			converted[last] = paramNames[last].substring(0, paramNames[last].length() - 3);
			return converted;
		} else {
			return paramNames;
		}
	}

	/**
	 * @see AbstractFunction.data.Function#callInternal(lang.data.Scope,
	 *      FplValue.data.LObject[])
	 */
	@Override
	public FplValue callInternal(final Scope scope, final FplValue[] parameters) throws EvaluationException {
		FplValue[] scopeParameters = new FplValue[getNumberOfParameterNames()];
		if (parameters.length > 0) {
			int lastNamedIndex = getNumberOfParameterNames() - 1;
			if (isVarArg()) {
				for (int i = 0; i < getMinimumNumberOfParameters(); i++) {
					scopeParameters[i] = makeLazy(scope, parameters[i]);
				}
				int count = parameters.length - getMinimumNumberOfParameters();
				FplValue[] varArgs = new FplValue[count];
				for (int i = 0, j = lastNamedIndex; i < count; i++, j++) {
					varArgs[i] = makeLazy(scope, parameters[j]);
				}
				scopeParameters[lastNamedIndex] = FplList.fromValues(varArgs);
			} else {
				for (int i = 0; i < parameters.length; i++) {
					scopeParameters[i] = makeLazy(scope, parameters[i]);
				}
			}
		}
		ParameterScope callScope = new ParameterScope(scope, scopeParameters);
		FplValue result = null;
		for (int i = 0; i < code.length; i++) {
			result = code[i].evaluate(callScope);
		}
		return result;
	}

	private Map<String, Integer> createParameterMap() throws EvaluationException {
		Map<String, Integer> parameterMap = new HashMap<>();
		for (int i = 0; i < getNumberOfParameterNames(); i++) {
			String name = getParameterName(i);
			if (parameterMap.containsKey(name)) {
				throw new EvaluationException("Duplicate parameter name: " + name);
			}
			parameterMap.put(name, i);
		}
		return parameterMap;
	}

	private FplValue makeLazy(Scope scope, FplValue e) {
		if (e instanceof LazyExpression || e instanceof EvaluatesToThisValue) {
			return e;
		} else {
			return new LazyExpression(scope, e);
		}
	}

	private FplValue[] compile(FplValue[] code, Map<String, Integer> parameterMap) {
		FplValue[] compiled = new FplValue[code.length];
		for (int i = 0; i < code.length; i++) {
			compiled[i] = compile(code[i], parameterMap);
		}
		return compiled;
	}
	
	private FplValue compile(FplValue code, Map<String, Integer> parameterMap) {
		if (code instanceof FplList) {
			FplList list = (FplList) code;
			FplValue[] compiled = new FplValue[list.size()];
			int i = 0;
			for (FplValue o : list) {
				compiled[i++] = compile(o, parameterMap);
			}
			return FplList.fromValues(compiled);
		} else {
			if (code instanceof Symbol) {
				Symbol s = (Symbol) code;
				Integer index = parameterMap.get(s.getName());
				if (index == null) {
					return s;
				} else {
					return new Parameter(s.getName(), index);
				}
			} else {
				return code;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(lambda (");
		for (int i = 0; i < getNumberOfParameterNames(); i++) {
			sb.append(getParameterName(i));
			if (i < getNumberOfParameterNames() - 1) {
				sb.append(' ');
			} else {
				if (isVararg()) {
					sb.append("...");
				}
			}
		}
		sb.append(") ");
		for (FplValue c : code) {
			sb.append(c.toString());
		}
		sb.append(")");
		return sb.toString();
	}
}
