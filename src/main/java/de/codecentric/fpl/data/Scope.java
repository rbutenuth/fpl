package de.codecentric.fpl.data;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

/**
 * Just a little bit more than a {@link Map}, can be nested.
 */
public class Scope {
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
     * @param next Next outer scope.
     */
    public Scope(Scope next) {
        this();
        this.next = next;
    }

	/**
	 * @return Next outer scope, may be null.
	 */
    public Scope getNext() {
        return next;
    }

    protected void initNext(Scope next) {
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
	 * @param value The value of the symbol, null values are allowed and will remove the mapping.
	 * @throws ScopeException If key is empty.
	 */
    public FplValue put(String key, FplValue value) throws ScopeException {
        checkKeyNotEmpty(key);
        // null means remove
        if (value == null) {
        	return map.remove(key);
        } else {
        	return map.put(key, value);
        }
    }

	/**
	 * Put a key value mapping in the scope, may overwrite an existing mapping. The
	 * name is taken from the value.
	 * 
	 * @param value The value of the symbol, null values are allowed and will remove the mapping.
	 * @throws ScopeException If scope is sealed (see {@link #isSealed()}).
	 */
	public void put(Named value) throws ScopeException {
		put(value.getName(), value);
	}

	/**
	 * Replace a value in this scope, if not there, search through scope chain until
	 * value is found in a not sealed scope.
	 * 
	 * @param key      Name of value to change, not null, not empty
	 * @param newValue The new value, not <code>null</code>
	 * @return The old value.
	 * @throws ScopeException If value is not found in this scope or in the scope chain.
	 */
	public FplValue replace(String key, FplValue newValue) throws ScopeException {
        if (key == null || key.length() == 0) {
            throw new ScopeException("key null or empty");
        }
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
	 * @param key      Name of value to change, not null, not empty
	 * @param value The new value, not <code>null</code>
	 * @return value
	 * @throws ScopeException Is scope is sealed or value did already exist.
	 */
	public FplValue define(String key, FplValue value) throws ScopeException {
        checkKeyNotEmpty(key);
        if (value == null) {
        	throw new ScopeException("value is nil");
        }
        FplValue old = map.putIfAbsent(key, value);
        if (old != null) {
        	throw new ScopeException("Duplicate key: " + key);
        }
        return value;
	}
	
    /**
     * @return All keys from this scope, ordered by natural {@link String} order.
     */
    public SortedSet<String> allKeys() {
        SortedSet<String> keySet = new TreeSet<>(map.keySet());
        keySet.addAll(map.keySet());
        return keySet;
    }

    private void checkKeyNotEmpty(String key) throws ScopeException {
        if (key == null || key.length() == 0) {
            throw new ScopeException("nil is not a valid name");
        }
	}
}
