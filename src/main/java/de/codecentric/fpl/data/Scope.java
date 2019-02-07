package de.codecentric.fpl.data;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

/**
 * For parameters or key value mappings.
 */
/**
 * @author butenuth
 *
 */
public interface Scope {
	/**
	 * @return Next outer scope, may be null.
	 */
	public Scope getNext();

	/**
	 * Lookup a symbol, if not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found expression, may be null.
	 */
	public FplValue get(String key);

	/**
	 * Put a key value mapping in the scope, may overwrite an existing mapping.
	 * 
	 * @param key   Name of value to lookup, not null, not empty
	 * @param value The value of the symbol, null values are allowed and will remove the mapping.
	 * @throws EvaluationException If scope is sealed (see {@link #isSealed()}).
	 */
	public void put(String key, FplValue value) throws EvaluationException;

	/**
	 * Put a key value mapping in the scope, may overwrite an existing mapping. The
	 * name is taken from the value.
	 * 
	 * @param value The value of the symbol, null values are allowed and will remove the mapping.
	 * @throws EvaluationException If scope is sealed (see {@link #isSealed()}).
	 */
	public default void put(Named value) throws EvaluationException {
		put(value.getName(), value);
	}

	/**
	 * Search scopes starting from this one via {@link #getNext()}. If a
	 * {@link #isSealed()} scope is found, put the value in the one before (can be
	 * this scope).
	 * 
	 * @param key   Name of value to lookup, not null, not empty
	 * @param value The value of the symbol, null values are allowed.
	 * @throws EvaluationException If this scope is sealed.
	 */
	public default void putGlobal(String key, FplValue value) throws EvaluationException {
		Scope chain = this;
		while (chain.getNext() != null && !chain.getNext().isSealed()) {
			chain = chain.getNext();
		}
		chain.put(key, value);
	}

	/**
	 * Search a value through the chain of Scopes and change it's value. Fails if
	 * the value is not found or the Scope with the value is sealed.
	 * 
	 * @param key      Name of value to change, not null, not empty
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws EvaluationException If scope is sealed or value is not found.
	 */
	public default FplValue changeWithSearch(String key, FplValue newValue) throws EvaluationException {
		if (newValue == null) {
			throw new EvaluationException("change does not allow null values");
		}
        for (Scope chain = this; chain != null; chain = chain.getNext()) {
        	FplValue oldValue = chain.get(key);
        	if (oldValue != null) {
        		chain.put(key, newValue);
        		return oldValue;
        	}
        }
		throw new EvaluationException("No value with key " + key + " found in scope");
	}
	
	/**
	 * Change a value in this scope, do not search outer scopes. Fails if
	 * the value is not found or the Scope with the value is sealed.
	 * 
	 * @param key      Name of value to change, not null, not empty
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws EvaluationException If scope is sealed or value is not found.
	 */
	public FplValue change(String key, FplValue newValue) throws EvaluationException;

	/**
	 * @param key      Name of value to change, not null, not empty
	 * @param value The new value, not <code>null</code>
	 * @throws EvaluationException Is scope is sealed or value did already exist.
	 */
	public void define(String key, FplValue value) throws EvaluationException; 
	
	/**
	 * @return Is this scope read only?
	 */
	public boolean isSealed();
}
