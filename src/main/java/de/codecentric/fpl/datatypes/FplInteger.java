package de.codecentric.fpl.datatypes;

/**
 * An FPL integer.
 */
public class FplInteger extends EvaluatesToThisValue implements FplNumber {
	private static FplInteger[] cache;
	private static final int low = -128;
	private static final int high = 127;
	static {
		cache = new FplInteger[(high - low) + 1];
		int j = low;
		for (int k = 0; k < cache.length; k++) {
			cache[k] = new FplInteger(j++);
		}
	}
	private long value;

	/**
	 * Factory method (to avoid duplicates for small values)
	 * 
	 * @param value Value.
	 */
	public static FplInteger valueOf(long value) {
		if (value >= low && value <= high) {
			return cache[(int) value - low];
		}
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
		return (int) (value ^ (value >>> 32));
	}

	@Override
	public String typeName() {
		return "integer";
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
