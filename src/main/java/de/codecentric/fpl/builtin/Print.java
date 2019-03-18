package de.codecentric.fpl.builtin;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;

/**
 * Lisp "print".
 */
public class Print extends AbstractFunction {
    private boolean newline;
	private FplEngine engine;

    /**
     * @param scope Scope to which functions should be added.
     * @param engine The interpreter to which we belong.
     * @throws ScopeException Should not happen on initialization.
     */
    public static void put(Scope scope, FplEngine engine) throws ScopeException {
    	scope.put(new Print(false, comment("Print parameters."), engine));
    	scope.put(new Print(true, comment("Print parameters, followed by line break."), engine));
    }

    private Print(boolean newline, List<String> comment, FplEngine engine) {
        super(newline ? "println" : "print", comment, true, "expression");
        this.newline = newline;
        this.engine = engine;
    }

    @Override
    public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
        for (int i = 0; i < parameters.length; i++) {
            FplValue value = parameters[i].evaluate(scope);
            engine.getSystemOut().print(value);
            if (i < parameters.length - 1) {
            	engine.getSystemOut().print(' ');
            }
        }
        if (newline) {
        	engine.getSystemOut().println();
        }
        return null;
    }
}
