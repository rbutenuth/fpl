package de.codecentric.fpl.builtin;

import java.util.Iterator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.FplWrapper;
import de.codecentric.fpl.datatypes.Parameter;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * The functional part of FPL.
 */
public class Lambda implements ScopePopulator {

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("lambda", "Create an anonymous function.", "parameter-list", "code...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList params = evaluateToListIfNotAlreadyList(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				FplValue[] code = new FplValue[parameters.length - 1];
				System.arraycopy(parameters, 1, code, 0, code.length);
				return lambda("lambda", position, FplValue.comments(parameters[0]), params, code, scope);
			}
		});

		scope.define(new AbstractFunction("lambda-dynamic", "Create an anonymous function.", "parameter-list", //
				"code-list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList paramList = evaluateToList(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				return lambda("lambda", position, FplValue.comments(parameters[0]), paramList, codeFromExpression(scope, parameters[1]), scope);
			}
		});

		scope.define(new AbstractFunction("def-function", "Define a function.", "name", "parameter-list", "code...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name = Assignment.targetName(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				FplList paramList = evaluateToListIfNotAlreadyList(scope, parameters[1]);
				FplValue[] code = new FplValue[parameters.length - 2];
				System.arraycopy(parameters, 2, code, 0, code.length);
				try {
					return defineFunction(scope, name, position, FplValue.comments(parameters[0]), paramList, code);
				} catch (IllegalArgumentException e) {
					throw new EvaluationException(e.getMessage(), e);
				}
			}
		});

		scope.define(new AbstractFunction("def-function-dynamic", "Define a function.", "name", //
				"parameter-list", "code-list") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name = evaluateToString(scope, parameters[0]);
				Position position = FplValue.position(parameters[0]);
				FplList paramList = evaluateToList(scope, parameters[1]);
				FplValue[] code = codeFromExpression(scope, parameters[2]);
				return defineFunction(scope, name, position, FplValue.comments(parameters[0]), paramList, code);
			}
		});

		scope.define(new AbstractFunction("eval", "Evaluate expression.", "expression") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue expression = parameters[0].evaluate(scope);
				return Assignment.value(scope, expression);
			}
		});

		scope.define(new AbstractFunction("type-of", "Return type of argument as string", "value") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue expression = Assignment.value(scope, parameters[0]);
				return expression == null ? null : new FplString(expression.typeName());
			}
		});

		scope.define(new AbstractFunction("java-instance", "Create an instance of a Java wrapper object by calling a constructor.", //
				"class...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String name = evaluateToString(scope, parameters[0]);
				Object[] methodParams = new Object[parameters.length - 1];
				for (int i = 0; i < methodParams.length; i++) {
					methodParams[i] = Assignment.value(scope, parameters[i + 1]);
				}
				return new FplWrapper(name, methodParams);
			}
		});

		scope.define(new AbstractFunction("java-class", "Create a handle to call static methods of a Java class.", //
				"class") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return new FplWrapper(evaluateToString(scope, parameters[0]));
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

	private FplLambda lambda(String name, Position position, String comment, FplList paramList, FplValue[] code, Scope scope) throws EvaluationException {
		String[] paramNames = new String[paramList.size()];
		String[] paramComments = new String[paramNames.length];
		int i = 0;
		for (FplValue v : paramList) {
			Symbol s;
			if (v instanceof Symbol) {
				s = (Symbol)v;
			} else if (v instanceof Parameter) {
				s = new Symbol(((Parameter)v).getName());
			} else if (v instanceof FplString) {
				s = new Symbol(((FplString)v).getContent());
			} else {
				throw new EvaluationException("Parameter " + v + " is not a symbol.");
			}
			paramNames[i] = s.getName();
			paramComments[i] = s.getComment();
			i++;
		}
		FplLambda result = new FplLambda(position, name, comment, paramNames, code, scope);
		for (i = 0; i < paramComments.length; i++) {
			result.setParameterComment(withoutVarArgsPostfix(paramNames[i]), paramComments[i]);
		}
		return result;
	}

	private String withoutVarArgsPostfix(String name) {
		return name.endsWith("...") ? name.substring(0, name.length() - 3) : name;
	}

	private FplLambda defineFunction(Scope scope, String name, Position position, String comment, FplList paramList, FplValue[] code)
			throws EvaluationException {
		FplLambda result = lambda(name, position, comment, paramList, code, scope);
		try {
			scope.define(name, result);
		} catch (ScopeException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
		return result;
	}
}
