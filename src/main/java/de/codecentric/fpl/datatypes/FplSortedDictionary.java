package de.codecentric.fpl.datatypes;

import static de.codecentric.fpl.data.Scope.checkKeyNotNullOrEmpty;
import static de.codecentric.fpl.data.Scope.checkValueNotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.list.FplList;

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
	public int size() {
		return map.size();
	}
	
	@Override
	public synchronized String peekFirstKey() {
		checkNotEmpty();
		return map.firstKey();
	}

	@Override
	public synchronized String peekLastKey() {
		checkNotEmpty();
		return map.lastKey();
	}
	
	@Override
	public synchronized String fetchFirstKey() {
		checkNotEmpty();
		String key = map.firstKey();
		map.remove(key);
		return key;
	}

	@Override
	public synchronized String fetchLastKey() {
		checkNotEmpty();
		String key = map.lastKey();
		map.remove(key);
		return key;
	}

	@Override
	public synchronized FplValue fetchFirstValue() {
		checkNotEmpty();
		Entry<String, FplValue> entry = map.firstEntry();
		map.remove(entry.getKey());
		return entry.getValue();
	}

	@Override
	public synchronized FplValue fetchLastValue() {
		checkNotEmpty();
		Entry<String, FplValue> entry = map.lastEntry();
		map.remove(entry.getKey());
		return entry.getValue();
	}

	@Override
	public synchronized FplList fetchFirstEntry() {
		checkNotEmpty();
		Entry<String, FplValue> entry = map.firstEntry();
		map.remove(entry.getKey());
		return FplList.fromValues(new FplString(entry.getKey()), entry.getValue());
	}

	@Override
	public synchronized FplList fetchLastEntry() {
		checkNotEmpty();
		Entry<String, FplValue> entry = map.lastEntry();
		map.remove(entry.getKey());
		return FplList.fromValues(new FplString(entry.getKey()), entry.getValue());
	}

	private void checkNotEmpty() throws ScopeException {
		if (map.isEmpty()) {
			throw new ScopeException("dictionary is empty");
		}
	}
	
	@Override
	public String typeName() {
		return "sorted-dictionary";
	}

	@Override
	public Iterator<Entry<String, FplValue>> iterator() {
		return map.entrySet().iterator();
	}

	@Override
	public String toString() {
		return FplDictionary.toString(map);
	}
}
