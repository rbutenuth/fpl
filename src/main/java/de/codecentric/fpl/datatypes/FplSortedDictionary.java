package de.codecentric.fpl.datatypes;

import static de.codecentric.fpl.data.MapScope.checkKeyNotNullOrEmpty;
import static de.codecentric.fpl.data.MapScope.checkValueNotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.codecentric.fpl.data.ScopeException;

/**
 * Similar to {@link FplObject}, but sorted.
 */
public class FplSortedDictionary implements FplDictionary, FplValue, EvaluatesToThisValue {
	private TreeMap<String, FplValue> map;

	public FplSortedDictionary(Comparator<String> comparator) {
		if (comparator == null) {
			map = new TreeMap<>();
		} else {
			map = new TreeMap<>(comparator);
		}
	}
	
	@Override
	public synchronized FplValue get(String key) {
		return map.get(key);
	}

	@Override
	public synchronized FplValue put(String key, FplValue value) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		// null means remove
		if (value == null) {
			return map.remove(key);
		} else {
			return map.put(key, value);
		}
	}

	@Override
	public synchronized FplValue define(String key, FplValue value) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		checkValueNotNull(value);
		FplValue old = map.putIfAbsent(key, value);
		if (old != null) {
			throw new ScopeException("Duplicate key: " + key);
		}
		return value;
	}

	@Override
	public synchronized FplValue replace(String key, FplValue newValue) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		checkValueNotNull(newValue);
		FplValue oldValue = map.replace(key, newValue);
		if (oldValue == null) {
			throw new ScopeException("No value with key " + key + " found");
		}
		return oldValue;
	}

	@Override
	public synchronized Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public synchronized Collection<FplValue> values() {
		return map.values();
	}

	@Override
	public synchronized Set<Entry<String, FplValue>> entrieSet() {
		return map.entrySet();
	}

	@Override
	public String typeName() {
		return "sorted-dictionary";
	}
}
