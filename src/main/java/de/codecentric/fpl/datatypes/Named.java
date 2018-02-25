package de.codecentric.fpl.datatypes;

public interface Named extends FplValue {

    /**
     * @return Name, never <code>null</code>, never empty.
     */
	public String getName();
}
