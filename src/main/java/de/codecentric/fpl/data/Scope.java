package de.codecentric.fpl.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

public interface Scope extends Iterable<Entry<String, FplValue>> {
	/**
	 * @return Next outer scope, may be null.
	 */
	public Scope getNext();
	
	public String getName();
	
	/**
	 * @return A new nested <code>Scope</code> where {@link #getNext()} will return <code>this</code>
	 */
	public Scope createNested(String name);
	
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
	 * @param key   Name of value to store, not null, not empty
	 * @param value The value, null values are allowed and will remove the mapping.
	 * @throws ScopeException If key is empty.
	 */
	public FplValue put(String key, FplValue value) throws ScopeException;

	/**
	 * Replace a value in this scope, if not there, search through scope chain until
	 * value is found.
	 * 
	 * @param key      Name of value to change, not null, not empty
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws ScopeException If value is not found in this scope or in the scope
	 *                        chain.
	 */
	public FplValue replace(String key, FplValue newValue) throws ScopeException;

	/**
	 * Replace a value in this scope. Do not walk scope chain.
	 * 
	 * @param key      Name of value to change, not null, not empty
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws ScopeException If value is not found in this scope or in the scope
	 *                        chain.
	 */
	public FplValue replaceLocal(String key, FplValue newValue) throws ScopeException;

	/**
	 * @param key   Name of value to define, not null, not empty
	 * @param value The new value, not <code>null</code>
	 * @return value
	 * @throws ScopeException If value did already exist.
	 */
	public FplValue define(String key, FplValue value) throws ScopeException;

	/**
	 * Put a key value mapping in the scope, may overwrite an existing mapping. The
	 * name is taken from the value.
	 * 
	 * @param value The value of the symbol, null values are allowed and will remove
	 *              the mapping.
	 */
	public void define(Named value) throws ScopeException;

	@Override
	public Iterator<Entry<String, FplValue>> iterator();
	
	public Set<String> keySet();

	public Collection<FplValue> values();
	
	public Set<Entry<String, FplValue>> entrieSet();
}
