package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.AbstractFunction.comment;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.FplWrapper;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Lisp "lambda".
 */
public class Lambda {

	/**
	 * @param scope Scope to which functions should be added.
	 * @throws ScopeException Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

		scope.put(new AbstractFunction("lambda", comment("Create an anonymous function."), false, "parameter-list", "code...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (!(parameters[0] instanceof FplList)) {
					throw new EvaluationException("First parameter must be a list of symbols.");
				}
				FplValue[] code = new FplValue[parameters.length - 1];
				System.arraycopy(parameters, 1, code, 0, code.length);
				return lambda(new Symbol("lambda"), parameters[0], code);
			}
		});

		// Example:
		// (def-function factorial (n)
		// (if (le n 1)
		// 1
		// (* n (factorial (- n 1)))
		// )
		// )
		scope.put(new AbstractFunction("def-function", comment("Define a function."), true, "name", "parameter-list", "code...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (!(parameters[0] instanceof Symbol)) {
					throw new EvaluationException("Expect symbol, got: " + parameters[0]);
				}
				Symbol name = (Symbol) parameters[0];
				FplValue[] code = new FplValue[parameters.length - 2];
				System.arraycopy(parameters, 2, code, 0, code.length);
				FplLambda result = lambda(name, parameters[1], code);
				try {
					scope.define(name.getName(), result);
				} catch (ScopeException e) {
					throw new EvaluationException(e);
				}
				return result;
			}
		});

		scope.put(new AbstractFunction("eval", comment("Evaluate expression."), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue expression = parameters[0].evaluate(scope);
				return value(scope, expression);
			}
		});

		scope.put(new AbstractFunction("get", comment("Get value."), false, "symbol") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return parameters[0].evaluate(scope);
			}
		});

		scope.put(new AbstractFunction("type-of", comment("Return type of argument as string"), false, "value") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue expression = value(scope, parameters[0]);
				return expression == null ? null : new FplString(expression.typeName());
			}
		});

		scope.put(new AbstractFunction("java-instance", comment("Create an instce of a Java wrapper object."), true, "class...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue fplClass = parameters[0].evaluate(scope);
				if (fplClass instanceof FplString) {
					Object[] methodParams = new Object[parameters.length - 1];
					for (int i = 0; i < methodParams.length; i++) {
						methodParams[i] = value(scope, parameters[i + 1]);
					}
					return new FplWrapper(((FplString)fplClass).getContent(), methodParams);
				} else {
					throw new EvaluationException("Expect string, but got " + fplClass.typeName());
				}
			}
		});
	}

	private static FplLambda lambda(Symbol name, FplValue paramListValues, FplValue[] code)
			throws EvaluationException {
		FplList paramList = createParamList(paramListValues);
		String[] paramNames = new String[paramList.size()];
		String[] paramComments = new String[paramList.size()];
		int i = 0;
		for (FplValue p : paramList) {
			Symbol s = (Symbol) p;
			paramNames[i] = s.getName();
			paramComments[i] = joinLines(s.getCommentLines());
			i++;
		}
		FplLambda result = new FplLambda(name.getPosition(), name.getCommentLines(), name.getName(), paramNames,
				code);
		for (i = 0; i < paramComments.length; i++) {
			result.setParameterComment(i, paramComments[i]);
		}
		return result;
	}

	private static FplList createParamList(FplValue paramListValues) throws EvaluationException {
		if (!(paramListValues instanceof FplList)) {
			throw new EvaluationException("Expect parameter list, got: " + paramListValues);
		}
		FplList paramList = (FplList) paramListValues;
		for (FplValue p : paramList) {
			if (!(p instanceof Symbol)) {
				throw new EvaluationException("Parameter " + p + " is not a symbol.");
			}
		}
		return paramList;
	}

	static private FplValue value(Scope scope, FplValue expression) throws EvaluationException {
		return expression == null ? null : expression.evaluate(scope);
	}
	
	private static String joinLines(List<String> commentLines) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < commentLines.size(); i++) {
			sb.append(commentLines.get(i).trim());
			if (i < commentLines.size() - 1) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}
}
