package de.codecentric.fpl.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;
import de.codecentric.fpl.datatypes.Symbol;

/**
 * Just a little bit more than a {@link Map}, can be nested.
 */
public class Scope implements Iterable<Entry<String, FplValue>> {
	protected ConcurrentMap<String, FplValue> map;
	protected Scope next;

	/**
	 * Create a top level scope.
	 */
	public Scope() {
		map = new ConcurrentHashMap<>();
	}

	/**
	 * Create an inner scope.
	 * 
	 * @param next Next outer scope, not <code>null</code>.
	 */
	public Scope(Scope next) {
		this();
		if (next == null) {
			throw new IllegalArgumentException("next can't be null");
		}
		this.next = next;
	}

	/**
	 * @return Next outer scope, may be null.
	 */
	public Scope getNext() {
		return next;
	}

	protected void setNext(Scope next) {
		this.next = next;
	}

	/**
	 * Lookup a symbol, if not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found expression, may be null.
	 */
	public FplValue get(String key) {
		FplValue value = map.get(key);
		if (value != null) {
			return value;
		}
		if (next != null) {
			return next.get(key);
		}
		return null;
	}

	/**
	 * Put a key value mapping in the scope, may overwrite an existing mapping.
	 * 
	 * @param key   Name of value to lookup, not null, not empty
	 * @param value The value of the symbol, null values are allowed and will remove
	 *              the mapping.
	 * @throws ScopeException If key is empty.
	 */
	public FplValue put(String key, FplValue value) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		// null means remove
		if (value == null) {
			return map.remove(key);
		} else {
			return map.put(key, value);
		}
	}

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
	public FplValue replace(String key, FplValue newValue) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		if (newValue == null) {
			throw new ScopeException("Change does not allow null values");
		}
		FplValue oldValue;
		Scope scope = this;
		do {
			oldValue = scope.map.replace(key, newValue);
		} while (oldValue == null && (scope = scope.getNext()) != null);
		if (oldValue == null) {
			throw new ScopeException("No value with key " + key + " found");
		}
		return oldValue;
	}

	/**
	 * @param key   Name of value to change, not null, not empty
	 * @param value The new value, not <code>null</code>
	 * @return value
	 * @throws ScopeException If value did already exist.
	 */
	public FplValue define(Symbol key, FplValue value) throws ScopeException {
		checkKeyNotNullOrEmpty(key.getName());
		if (value == null) {
			throw new ScopeException("value is nil");
		}
		FplValue old = map.putIfAbsent(key.getName(), value);
		if (old != null) {
			throw new ScopeException("Duplicate key: " + key);
		}
		return value;
	}

	/**
	 * Put a key value mapping in the scope, may overwrite an existing mapping. The
	 * name is taken from the value.
	 * 
	 * @param value The value of the symbol, null values are allowed and will remove
	 *              the mapping.
	 */
	public void define(Named value) throws ScopeException {
		define(new Symbol(value.getName()), value);
	}

	@Override
	public Iterator<Entry<String, FplValue>> iterator() {
		return map.entrySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	private void checkKeyNotNullOrEmpty(String key) throws ScopeException {
		if (key == null) {
			throw new ScopeException("nil is not a valid name");
		} else if (key.isEmpty()) {
			throw new ScopeException("\"\" is not a valid name");
		}
	}
}
