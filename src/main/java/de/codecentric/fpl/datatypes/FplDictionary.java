package de.codecentric.fpl.datatypes;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import de.codecentric.fpl.data.ScopeException;

/**
 * Common base for unsorted and sorted maps and objects.
 */
public interface FplDictionary {

	/**
	 * @param key Name of value to lookup
	 * @return The found value, may be null.
	 */
	public FplValue get(String key);
	
	/**
	 * Put a key value mapping in the dictionary, may overwrite an existing mapping.
	 * 
	 * @param key   Name of value to store, not null, not empty
	 * @param value The value, null values are allowed and will remove the mapping.
	 * @throws ScopeException If key is empty.
	 */
	public FplValue put(String key, FplValue value) throws ScopeException;
	
	/**
	 * @param key   Name of value to define, not null, not empty
	 * @param value The new value, not <code>null</code>
	 * @return value
	 * @throws ScopeException If value did already exist.
	 */
	public FplValue define(String key, FplValue value) throws ScopeException;
	
	/**
	 * Replace a value in this dictionary.
	 * 
	 * @param key      Name of value to change, not null, not empty
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws ScopeException If value is not found.
	 */
	public FplValue replace(String key, FplValue newValue) throws ScopeException;
	
	/**
	 * @return Ordered keys.
	 */
	public Set<String> keySet();
	
	/**
	 * @return Ordered values.
	 */
	public Collection<FplValue> values();
	
	/**
	 * @return Ordered entries.
	 */
	public Set<Entry<String, FplValue>> entrieSet();
}
