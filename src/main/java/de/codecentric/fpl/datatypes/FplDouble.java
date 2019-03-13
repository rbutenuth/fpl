package de.codecentric.fpl.datatypes;

/**
 * An FPL double.
 */
public class FplDouble extends EvaluatesToThisValue {
    private double value;

    /**
     * @param value Value.
     */
    public FplDouble(double value) {
        this.value = value;
    }

    /**
     * @return Value.
     */
    public double getValue() {
        return value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ temp >>> 32);
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
        FplDouble other = (FplDouble) obj;
        return value == other.value;
    }

	@Override
	public String typeName() {
		return "double";
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Double.toString(value);
    }
}
