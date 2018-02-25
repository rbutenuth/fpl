package de.codecentric.fpl.data;

import de.codecentric.fpl.parser.Position;

/**
 * For all elements which have information about there position in the source code.
 */
public interface PositionHolder {
    /**
     * @return Position where this element is defined. May be {@link Position#UNKNOWN}, but never null.
     */
    public Position getPosition();
}
