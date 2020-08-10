package de.codecentric.fpl.parser;

/**
 * Position in the source (for error messages). Immutable class.
 */
public final class Position {
    /** Constant for cases where position is not known. */
    public final static Position UNKNOWN = new Position("<unknown>", 1, 1);
    /** Constant for internal functions */
    public final static Position INTERNAL = new Position("<internal>", 1, 1);

    private final String name;
    private final int line;
    private final int column;

    /**
     * Konstruktor.
     * @param name Name of source (filename), not null.
     * @param line Line, starting with 1.
     * @param column Column, starting with 1.
     */
    public Position(String name, int line, int column) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (line < 1) {
            throw new IllegalArgumentException("line < 1");
        }
        if (column < 1) {
            throw new IllegalArgumentException("column < 1");
        }
        this.name = name;
        this.line = line;
        this.column = column;
    }

    /**
     * @return Name of source (filename), not null.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Line, starting with 1.
     */
    public int getLine() {
        return line;
    }

    /**
     * @return Column, starting with 1.
     */
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Position[name=\"" + name + "\", line=" + line + ", column=" + column + "]";
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = column;
        result = prime * result + line;
        result = prime * result + name.hashCode();
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        Position other = (Position) obj;
        if (column != other.column) {
            return false;
        }
        if (line != other.line) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
