package de.codecentric.fpl.datatypes;

import static de.codecentric.fpl.data.MapScope.checkKeyNotNullOrEmpty;
import static de.codecentric.fpl.data.MapScope.checkValueNotNull;

import java.util.Collection;
import java.util.Comparator;
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
		String result = map.firstKey();
		map.remove(result);
		return result;
	}

	@Override
	public synchronized String fetchLastKey() {
		checkNotEmpty();
		String result = map.lastKey();
		map.remove(result);
		return result;
	}

	@Override
	public synchronized FplValue fetchFirstValue() {
		checkNotEmpty();
		Entry<String, FplValue> result = map.firstEntry();
		map.remove(result.getKey());
		return result.getValue();
	}

	@Override
	public synchronized FplValue fetchLastValue() {
		checkNotEmpty();
		Entry<String, FplValue> result = map.lastEntry();
		map.remove(result.getKey());
		return result.getValue();
	}

	@Override
	public synchronized FplList fetchFirstEntry() {
		checkNotEmpty();
		Entry<String, FplValue> result = map.firstEntry();
		map.remove(result.getKey());
		return FplList.fromValues(new FplString(result.getKey()), result.getValue());
	}

	@Override
	public synchronized FplList fetchLastEntry() {
		checkNotEmpty();
		Entry<String, FplValue> result = map.lastEntry();
		map.remove(result.getKey());
		return FplList.fromValues(new FplString(result.getKey()), result.getValue());
	}

	private void checkNotEmpty() throws ScopeException {
		if (map.isEmpty()) {
			throw new ScopeException("dictionary is empty");
		}
	}
	
	@Override
	public String typeName() {
		return "sorted-dictionary";
	}}
