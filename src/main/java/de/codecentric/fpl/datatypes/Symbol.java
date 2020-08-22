package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.parser.Position;

/**
 * An FPL symbol.
 */
public class Symbol implements Named, PositionHolder {
    private final String name;
    private final Position position;
    private final String comment;

    /**
     * @param name Name of the symbol, not null, not empty String.
     */
    public Symbol(String name) {
        this(name, null, "");
    }

    /**
     * @param name Name of the symbol, not null, not empty String.
     * @param position Position, <code>null</code> will be replaced with {@link Position#UNKNOWN}.
     * @param comment The comments found in the source before this symbol
     */
    public Symbol(String name, Position position, String comment) {
        if (name == null) {
            throw new IllegalArgumentException("name null");
        }
        this.name = name;
        this.position = position == null ? Position.UNKNOWN : position;
        this.comment = comment == null ? "" : comment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    public String getComment() {
    	return comment;
    }
    
    /**
     * Look up a symbol in given scope, follow chain of scopes.
     * @param scope Where we start with lookup.
     * @return value of the symbol (may be null)
     */
    @Override
    public FplValue evaluate(Scope scope) throws EvaluationException {
        FplValue value = scope.get(name);
        if (value instanceof LazyExpression) {
        	return value.evaluate(scope);
        } else {
        	return value;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Symbol other = (Symbol) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

	@Override
	public String typeName() {
		return "symbol";
	}
}
