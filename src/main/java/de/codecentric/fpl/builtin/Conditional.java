package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

/**
 * Basic logic functions. <code>LInteger(0)</code> and <code>null</code> are false, everything else is true.
 */
public class Conditional {

    /**
     * @param scope Scope to which functions should be added.
     * @throws EvaluationException Should not happen on initialization.
     */
    public static void put(Scope scope) throws EvaluationException {

    	scope.put(new Function("if", //
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

        // TODO: some sort of switch/case
    }
}
