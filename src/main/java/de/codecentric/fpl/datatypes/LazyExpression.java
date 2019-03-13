package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

/**
 * A lazy expression, which will be evaluated on demand.
 */
public class LazyExpression implements FplValue {
    private Scope scope;
    private FplValue originalExpression;

    /**
     * @param scope Scope for evaluation, not null.
     * @param originalExpression The expression to be evaluated, may be null.
     */
    public LazyExpression(Scope scope, FplValue originalExpression) {
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
    public FplValue evaluate(Scope unusedScope) throws EvaluationException {
        return originalExpression.evaluate(scope);
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
