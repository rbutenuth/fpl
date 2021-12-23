package de.codecentric.fpl.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

/**
 * Just a little bit more than a {@link Map}, can be nested.
 */
public class MapScope implements Scope {
	protected final String name;
	protected ConcurrentMap<String, FplValue> map;
	protected Scope next;

	/**
	 * Create a top level scope.
	 */
	public MapScope(String name) {
		map = new ConcurrentHashMap<>();
		this.name = name;
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("empty name not allowed");
		}
	}

	/**
	 * Create an inner scope.
	 * 
	 * @param next Next outer scope.
	 */
	public MapScope(String name, Scope next) {
		this(name);
		this.next = next;
	}

	/**
	 * @return Next outer scope, may be null.
	 */
	@Override
	public Scope getNext() {
		return next;
	}
	
	protected void setNext(Scope next) {
		this.next = next;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Scope createNested(String name) {
		return new MapScope(name, this);
	}
	
	/**
	 * Lookup a symbol, if not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found value, may be null.
	 */
	@Override
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

	@Override
	public FplValue put(String key, FplValue value) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		// null means remove
		if (value == null) {
			return map.remove(key);
		} else {
			return map.put(key, value);
		}
	}

	@Override
	public FplValue replace(String key, FplValue newValue) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		checkValueNotNull(newValue);
		FplValue oldValue;
		Scope scope = this;
		do {
			oldValue = scope.replaceLocal(key, newValue);
		} while (oldValue == null && (scope = scope.getNext()) != null);
		if (oldValue == null) {
			throw new ScopeException("No value with key " + key + " found");
		}
		return oldValue;
	}

	@Override
	public FplValue replaceLocal(String key, FplValue newValue) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		checkValueNotNull(newValue);
		return map.replace(key, newValue);
	}
	
	@Override
	public FplValue define(String key, FplValue value) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		checkValueNotNull(value);
		FplValue old = map.putIfAbsent(key, value);
		if (old != null) {
			throw new ScopeException("Duplicate key: " + key);
		}
		return value;
	}

	@Override
	public void define(Named value) throws ScopeException {
		define(value.getName(), value);
	}

	@Override
	public Iterator<Entry<String, FplValue>> iterator() {
		return map.entrySet().iterator();
	}
	
	@Override
	public String toString() {
		return "Scope<" + name + ">";
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<FplValue> values() {
		return map.values();
	}
	
	@Override
	public Set<Entry<String, FplValue>> entrieSet() {
		return map.entrySet();
	}

	public static void checkKeyNotNullOrEmpty(String key) throws ScopeException {
		if (key == null) {
			throw new ScopeException("nil is not a valid name");
		} else if (key.isEmpty()) {
			throw new ScopeException("\"\" is not a valid name");
		}
	}
	
	public static void checkValueNotNull(FplValue value) throws ScopeException {
		if (value == null) {
			throw new ScopeException("value is nil");
		}
	}
}
