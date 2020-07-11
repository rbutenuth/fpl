package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<String> commentLines;

    /**
     * @param name Name of the symbol, not null, not empty String.
     */
    public Symbol(String name) {
        this(name, null, Collections.emptyList());
    }

    /**
     * @param name Name of the symbol, not null, not empty String.
     * @param position Position, <code>null</code> will be replaced with {@link Position#UNKNOWN}.
     * @param commentLines The comments found in the source before this symbol
     */
    public Symbol(String name, Position position, List<String> commentLines) {
        if (name == null) {
            throw new IllegalArgumentException("name null");
        }
        this.name = name;
        this.position = position == null ? Position.UNKNOWN : position;
        this.commentLines = new ArrayList<>(commentLines);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    public List<String> getCommentLines() {
    	return Collections.unmodifiableList(commentLines);
    }
    
    /**
     * Look up a symbol in given scope, follow chain of scopes.
     * @param scope Where we start with lookup.
     * @return value of the symbol (may be null)
     */
    @Override
    public FplValue evaluate(Scope scope) throws EvaluationException {
        return scope.get(name);
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
