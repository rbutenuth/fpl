package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

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

	public static Position position(FplValue value) {
		if (value instanceof Symbol) {
			return ((Symbol)value).getPosition();
		}
		if (value instanceof FplList) {
			for (FplValue v : (FplList)value) {
				Position p = position(v);
				if (!Position.UNKNOWN.equals(p)) {
					return p;
				}
			}
		}
		return Position.UNKNOWN;
	}
	
	public static String comments(FplValue value) {
		if (value instanceof Symbol) {
			return ((Symbol)value).getComment();
		}
		return "";
	}
}
