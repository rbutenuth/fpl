package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

/**
 * The base for all data types.
 */
public interface FplValue {
    /**
     * Evaluate the expression in the given context of a {@link Scope}.
     * @param scope Context information, not null.
     * @return Result of evaluation.
     * @throws EvaluationException If evaluation fails.
     */
    public FplValue evaluate(Scope scope) throws EvaluationException;
    
    /**
     * @return Readable type, e.g. "string".
     */
    public String typeName();
}
