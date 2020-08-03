package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Basic logic functions. <code>FplInteger(0)</code>, <code>FplDouble(0.0)</code>, <code>()</code>
 * and <code>nil</code> are <code>false</code>, everything else is true.
 */
public class Conditional implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException {

    	scope.define(new AbstractFunction("if-else", //
    			comment("Evaluate condition, if true, return evaluated if-part, otherwise evaluated else-part."),
    			false, "condition", "if-part", "else-part") {
            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                if (evaluateToBoolean(scope, parameters[0])) {
                    // The "if" clause
                    return parameters[1] == null ? null : parameters[1].evaluate(scope);
                } else {
                	return parameters[2] == null ? null : parameters[2].evaluate(scope);
                }
            }
        });

    	scope.define(new AbstractFunction("if", //
    			comment("Evaluate condition, if true, return evaluated if-part, otherwise nil."),
    			false, "condition", "if-part") {
            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                if (evaluateToBoolean(scope, parameters[0])) {
                    // The "if" clause
                    return parameters[1] == null ? null : parameters[1].evaluate(scope);
                } else {
                	return null;
                }
            }
        });

    	scope.define(new AbstractFunction("throw", //
    			comment("Throw an exception."),
    			false, "message") {
            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                String message = evaluateToString(scope, parameters[0]);
                throw new EvaluationException(message);
            }
        });
	}

}
