package de.codecentric.fpl.builtin;

import java.util.Iterator;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.FplWrapper;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * The functional part of FPL.
 */
public class Lambda implements ScopePopulator {

	@Override
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("lambda", comment("Create an anonymous function."), true, "parameter-list",
				"code...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList params = evaluateToListIfNotAlreadyList(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				FplValue[] code = new FplValue[parameters.length - 1];
				System.arraycopy(parameters, 1, code, 0, code.length);
				return lambda("lambda", position, FplValue.comments(parameters[0]), params, code);
			}
		});

		scope.define(new AbstractFunction("lambda-dynamic", comment("Create an anonymous function."), true, //
				"parameter-list", "code-list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList paramList = evaluateToList(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				return lambda("lambda", position, FplValue.comments(parameters[0]), paramList, codeFromExpression(scope, parameters[1]));
			}
		});

		scope.define(new AbstractFunction("def-function", comment("Define a function."), true, "name", "parameter-list",
				"code...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name = Assignment.targetName(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				FplList paramList = evaluateToListIfNotAlreadyList(scope, parameters[1]);
				FplValue[] code = new FplValue[parameters.length - 2];
				System.arraycopy(parameters, 2, code, 0, code.length);
				return defineFunction(scope, name, position, FplValue.comments(parameters[0]), paramList, code);
			}
		});

		scope.define(new AbstractFunction("def-function-dynamic", comment("Define a function."), true, //
				"name", "parameter-list", "code-list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name = evaluateToString(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				FplList paramList = evaluateToList(scope, parameters[1]);
				FplValue[] code = codeFromExpression(scope, parameters[2]);
				return defineFunction(scope, name, position, FplValue.comments(parameters[0]), paramList, code);
			}
		});

		scope.define(new AbstractFunction("eval", comment("Evaluate expression."), false, "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue expression = parameters[0].evaluate(scope);
				return Assignment.value(scope, expression);
			}
		});

		scope.define(new AbstractFunction("type-of", comment("Return type of argument as string"), false, "value") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue expression = Assignment.value(scope, parameters[0]);
				return expression == null ? null : new FplString(expression.typeName());
			}
		});

		scope.define(new AbstractFunction("java-instance", comment("Create an instance of a Java wrapper object by calling a constructor."), //
				true, "class...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name;
				if (parameters[0] instanceof Symbol) {
					name = ((Symbol) parameters[0]).getName();
				} else {
					FplValue fplClass = parameters[0].evaluate(scope);
					if (fplClass instanceof FplString) {
						name = ((FplString) fplClass).getContent();
					} else {
						throw new EvaluationException("Expect string or symbol, but got " + fplClass.typeName());
					}
				}
				Object[] methodParams = new Object[parameters.length - 1];
				for (int i = 0; i < methodParams.length; i++) {
					methodParams[i] = Assignment.value(scope, parameters[i + 1]);
				}
				return new FplWrapper(name, methodParams);
			}
		});

		scope.define(new AbstractFunction("java-class", comment("Create a handle to call static methods of a Java class."), //
				false, "class") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name;
				if (parameters[0] instanceof Symbol) {
					name = ((Symbol) parameters[0]).getName();
				} else {
					FplValue fplClass = parameters[0].evaluate(scope);
					if (fplClass instanceof FplString) {
						name = ((FplString) fplClass).getContent();
					} else {
						throw new EvaluationException("Expect string or symbol, but got " + fplClass.typeName());
					}
				}
				return new FplWrapper(name);
			}
		});
	}
	
	private FplValue[] codeFromExpression(Scope scope, FplValue codeExpression) throws EvaluationException {
		FplList codeList = AbstractFunction.evaluateToList(scope, codeExpression);

		FplValue[] code = new FplValue[codeList.size()];
		Iterator<FplValue> codeIter = codeList.iterator();
		int i = 0;
		while (codeIter.hasNext()) {
			code[i++] = codeIter.next();
		}
		return code;
	}

	private FplLambda lambda(String name, Position position, List<String> commentLines, FplList paramList, FplValue[] code) throws EvaluationException {
		String[] paramNames = new String[paramList.size()];
		String[] paramComments = new String[paramNames.length];
		int i = 0;
		for (FplValue p : paramList) {
			Symbol s;
			if (p instanceof Symbol) {
				s = (Symbol)p;
			} else if (p instanceof FplString) {
				s = new Symbol(((FplString)p).getContent());
			} else {
				throw new EvaluationException("Parameter " + p + " is not a symbol.");
			}
			paramNames[i] = s.getName();
			paramComments[i] = joinLines(s.getCommentLines());
			i++;
		}
		FplLambda result = new FplLambda(position, commentLines, name, paramNames, code);
		for (i = 0; i < paramComments.length; i++) {
			result.setParameterComment(i, paramComments[i]);
		}
		return result;
	}

	private FplLambda defineFunction(Scope scope, String name, Position position, List<String> commentLines, FplList paramList, FplValue[] code)
			throws EvaluationException {
		FplLambda result = lambda(name, position, commentLines, paramList, code);
		try {
			scope.define(name, result);
		} catch (ScopeException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
		return result;
	}

	private String joinLines(List<String> commentLines) {
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
