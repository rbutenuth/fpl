package de.codecentric.fpl.datatypes.list;

import java.util.Iterator;

public interface SizedIterator<E> extends Iterator<E> {
	
	/**
	 * @return Number of elements this instance will return.
	 */
	public int size();

}
