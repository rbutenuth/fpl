package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplFunction;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
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
    	
    	scope.put(new Function("lambda", comment("Create an anonymous function."), false, "parameter-list", "code...") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                if (!(parameters[0] instanceof FplList)) {
                    throw new EvaluationException("First parameter must be a list of symbols.");
                }
                FplValue[] code = new FplValue[parameters.length - 1];
                System.arraycopy(parameters, 1, code, 0, code.length);
                return lambda(this, new Symbol("lambda"), parameters[0], code);
            }
        });

        // Example:
        // (defun factorial (n)
        //    (if (le n 1)
        //       1
        //       (* n (factorial (- n 1)))
        //    )
        // )
    	scope.put(new Function("defun", comment("Define a function."), true, "name", "parameter-list", "code...") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                if (!(parameters[0] instanceof Symbol)) {
                    throw new EvaluationException("Expect symbol, got: " + parameters[0]);
                }
                Symbol name = (Symbol)parameters[0];
                FplValue[] code = new FplValue[parameters.length - 2];
                System.arraycopy(parameters, 2, code, 0, code.length);
                FplFunction result = lambda(this, name, parameters[1], code);
                try {
					scope.define(name.getName(), result);
				} catch (ScopeException e) {
					throw new EvaluationException(e);
				}
                return result;
            }
        });

    	scope.put(new Function("eval", comment("Evaluate expression."), false, "expression") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                FplValue value = parameters[0].evaluate(scope);
                return value == null ? value : value.evaluate(scope);
            }
        });
    }
    
    private static FplFunction lambda(Function f, Symbol name, FplValue paramListValues, FplValue[] code) throws EvaluationException {
    	FplList paramList = createParamList(f, paramListValues);
		String[] paramNames = new String[paramList.size()];
		String[] paramComments = new String[paramList.size()];
		int i = 0;
		for (FplValue p : paramList) {
		    Symbol s = (Symbol)p;
		    paramNames[i] = s.getName();
		    paramComments[i] = joinLines(s.getCommentLines());
		    i++;
		}
        FplFunction result = new FplFunction(name.getPosition(), name.getCommentLines(), name.getName(), paramNames, code);
        for (i = 0; i < paramComments.length; i++) {
        	result.setParameterComment(i, paramComments[i]);
        }
    	return result;
    }

    private static FplList createParamList(Function f, FplValue paramListValues) throws EvaluationException {
        if (!(paramListValues instanceof FplList)) {
            throw new EvaluationException("Expect parameter list, got: " + paramListValues);
        }
		FplList paramList = (FplList)paramListValues;
		for (FplValue p : paramList) {
		    if (!(p instanceof Symbol)) {
		        throw new EvaluationException("Parameter " + p + " is not a symbol.");
		    }
		}		
		return paramList;
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
