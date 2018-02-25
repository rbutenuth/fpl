package de.codecentric.fpl.datatypes;

/**
 * An FPL integer.
 */
public class FplInteger extends Atom {
    private long value;

    /**
     * Factory method (to avoid duplicates for small values)
     * @param value Value.
     */
    public static FplInteger valueOf(long value) {
    	// TODO: create an array for small values
    	return new FplInteger(value);
    }
    
    private FplInteger(long value) {
        this.value = value;
    }

    /**
     * @return Value.
     */
    public long getValue() {
        return value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int)(value ^ (value >>> 32));
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
        FplInteger other = (FplInteger) obj;
        return value == other.value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Long.toString(value);
    }
}
