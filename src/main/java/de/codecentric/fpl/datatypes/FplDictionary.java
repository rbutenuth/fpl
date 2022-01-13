package de.codecentric.fpl.datatypes;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Common base for unsorted and sorted maps.
 */
public interface FplDictionary extends Iterable<Entry<FplValue, FplValue>>, FplValue {

	/**
	 * @param key to lookup
	 * @return The found value, may be null.
	 */
	public FplValue get(FplValue key);
	
	/**
	 * Put a key value mapping in the dictionary, may overwrite an existing mapping.
	 * 
	 * @param key Key for the value to store, not null, not empty (if String)
	 * @param value The value, null values are allowed and will remove the mapping.
	 * @throws ScopeException If key is empty.
	 */
	public FplValue put(FplValue key, FplValue value) throws ScopeException;
	
	/**
	 * @param key Key for the value to store, not null, not empty (if String)
	 * @param value The new value, not <code>null</code>
	 * @return value
	 * @throws ScopeException If value did already exist.
	 */
	public FplValue define(FplValue key, FplValue value) throws ScopeException;
	
	/**
	 * Replace a value in this dictionary.
	 * 
	 * @param key Key for the value to store, not null, not empty (if String)
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws ScopeException If value is not found.
	 */
	public FplValue replace(FplValue key, FplValue newValue) throws ScopeException;
	
	/**
	 * @return Set of keys, ordered when dictionary is ordered.
	 */
	public Set<FplValue> keySet();
	
	/**
	 * @return Values, ordered when dictionary is ordered.
	 */
	public Collection<FplValue> values();
	
	/**
	 * @return Entries, ordered when dictionary is ordered.
	 */
	public Set<Entry<FplValue, FplValue>> entrieSet();

	/**
	 * @return Number of mappings in this dictionary.
	 */
	public int size();
	
	/**
	 * @return Returns the first key from the dictionary. In case
	 * of an unsorted dictionary, a random key is returned.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplValue peekFirstKey() throws ScopeException;

	/**
	 * @return Returns the last key from the dictionary. In case
	 * of an unsorted dictionary, a random key is returned.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplValue peekLastKey() throws ScopeException;

	/**
	 * @return Returns and removes the first key from the dictionary. In case
	 * of an unsorted dictionary, a random key is returned/removed.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplValue fetchFirstKey() throws ScopeException;

	/**
	 * @return Returns and removes the last key from the dictionary. In case
	 * of an unsorted dictionary, a random key is returned/removed.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplValue fetchLastKey() throws ScopeException;
	
	/**
	 * @return Returns and removes the first value from the dictionary. In case
	 * of an unsorted dictionary, a random value is returned/removed.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplValue fetchFirstValue() throws ScopeException;

	/**
	 * @return Returns and removes the last value from the dictionary. In case
	 * of an unsorted dictionary, a random value is returned/removed.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplValue fetchLastValue() throws ScopeException;

	/**
	 * @return Returns and removes the first entry from the dictionary. In case
	 * of an unsorted dictionary, a random entry is returned/removed.
	 * The list contains the key and the value.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplList fetchFirstEntry() throws ScopeException;

	/**
	 * @return Returns and removes the last entry from the dictionary. In case
	 * of an unsorted dictionary, a random entry is returned/removed.
	 * The list contains the key and the value.
	 * @throws ScopeException When dictionary is empty.
	 */
	public FplList fetchLastEntry() throws ScopeException;

	public static String toString(Map<FplValue, FplValue> map) {
		final String NL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<FplValue, FplValue> entry : map.entrySet()) {
			sb.append(NL).append("    ");
			sb.append(entry.getKey()).append(": ");
			FplValue v = entry.getValue();
			// Be careful: Cycles in data structures can lead to endless recursion...
			if (v instanceof FplList) {
				sb.append("<list>");
			} else if (v instanceof FplDictionary) {
				sb.append("<dictionary>");
			} else if (v instanceof FplObject) {
				sb.append("<object>");
			} else {
				sb.append(v.toString());
			}
		}
		sb.append(NL).append("}").append(NL);

		return sb.toString();
	}
}
