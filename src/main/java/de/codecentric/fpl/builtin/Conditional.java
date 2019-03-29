package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.AbstractFunction.comment;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;

/**
 * Basic logic functions. <code>LInteger(0)</code> and <code>null</code> are false, everything else is true.
 */
public class Conditional {

    /**
     * @param scope Scope to which functions should be added.
     * @throws ScopeException Should not happen on initialization.
     */
    public static void put(Scope scope) throws ScopeException {

    	scope.put(new AbstractFunction("if-else", //
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

    	scope.put(new AbstractFunction("if", //
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
        // TODO: some sort of switch/case
    }
}
