package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

/**
 * A lazy expression, which will be evaluated on demand.
 */
public class FplLazy implements FplValue {
    private Scope scope;
    private FplValue originalExpression;
    private boolean evaluated;
    private FplValue value;

    /**
     * @param scope Scope for evaluation, not null.
     * @param originalExpression The expression to be evaluated, may be null.
     */
	public static FplValue make(Scope scope, FplValue originalExpression) {
		if (originalExpression instanceof FplLazy || originalExpression instanceof EvaluatesToThisValue) {
			return originalExpression;
		} else {
			return new FplLazy(scope, originalExpression);
		}
	}

    private FplLazy(Scope scope, FplValue originalExpression) {
        if (scope == null) {
            throw new NullPointerException("scope");
        }
        this.scope = scope;
        this.originalExpression = originalExpression;
    }

    /**
     * @see FplValue.data.LObject#evaluateResource(lang.data.Scope)
     */
    @Override
    public synchronized FplValue evaluate(Scope unusedScope) throws EvaluationException {
    	if (!evaluated) {
    		evaluated = true;
    		value = originalExpression == null ? null : originalExpression.evaluate(scope);
    	}
    	return value;
    }
    
    @Override
    public String typeName() {
    	return originalExpression.typeName();
    }

    /**
     * @return The expression provided in the constructor.
     */
    public FplValue getOriginalExpression() {
        return originalExpression;
    }
}
