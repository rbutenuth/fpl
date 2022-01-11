package de.codecentric.fpl.datatypes;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.list.FplList;
import static de.codecentric.fpl.data.Scope.checkKeyNotNullOrEmpty;
import static de.codecentric.fpl.data.Scope.checkValueNotNull;

/**
 * The FPL version of an object: Most of the semantic comes from a combination
 * of {@link Scope} with {@link Named}. The rest are some built in functions for
 * linking and executing methods on objects.
 */
public class FplMapDictionary implements FplDictionary, FplValue, EvaluatesToThisValue {
	private ConcurrentMap<String, FplValue> map;

	public FplMapDictionary() {
		map = new ConcurrentHashMap<>();
	}

	@Override
	public String typeName() {
		return "dictionary";
	}

	@Override
	public String toString() {
		return FplDictionary.toString(map);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public synchronized String peekFirstKey() throws ScopeException {
		Iterator<String> iterator = map.keySet().iterator();
		checkHasNext(iterator);
		return iterator.next();
	}

	@Override
	public String peekLastKey() throws ScopeException {
		return peekFirstKey();
	}

	@Override
	public synchronized String fetchFirstKey() throws ScopeException {
		Iterator<String> iterator = map.keySet().iterator();
		checkHasNext(iterator);
		String result = iterator.next();
		iterator.remove();
		return result;
	}

	@Override
	public String fetchLastKey() throws ScopeException {
		return fetchFirstKey();
	}

	@Override
	public synchronized FplValue fetchFirstValue() throws ScopeException {
		Iterator<FplValue> iterator = map.values().iterator();
		checkHasNext(iterator);
		FplValue result = iterator.next();
		iterator.remove();
		return result;
	}

	@Override
	public FplValue fetchLastValue() throws ScopeException {
		return fetchFirstValue();
	}

	@Override
	public synchronized FplList fetchFirstEntry() throws ScopeException {
		Iterator<Entry<String, FplValue>> iterator = map.entrySet().iterator();
		checkHasNext(iterator);
		Entry<String, FplValue> result = iterator.next();
		iterator.remove();
		return FplList.fromValues(new FplString(result.getKey()), result.getValue());
	}

	@Override
	public FplList fetchLastEntry() throws ScopeException {
		return fetchFirstEntry();
	}

	private void checkHasNext(Iterator<?> iterator) throws ScopeException {
		if (!iterator.hasNext()) {
			throw new ScopeException("dictionary is empty");
		}
	}

	@Override
	public Iterator<Entry<String, FplValue>> iterator() {
		return map.entrySet().iterator();
	}

	/**
	 * Lookup a symbol, if not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found value, may be null.
	 */
	@Override
	public FplValue get(String key) {
		return map.get(key);
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
	public FplValue replace(String key, FplValue newValue) throws ScopeException {
		checkKeyNotNullOrEmpty(key);
		checkValueNotNull(newValue);
		FplValue oldValue = map.replace(key, newValue);
		if (oldValue == null) {
			throw new ScopeException("No value with key " + key + " found");
		}
		return oldValue;
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
}
