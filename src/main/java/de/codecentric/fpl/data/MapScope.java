package de.codecentric.fpl.data;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Just a little bit more than a {@link Map}, can be nested.
 */
public class MapScope implements ListableScope {
    private Map<String, FplValue> map;
    private Scope next;
    private boolean sealed;

    /**
     * Create a top level scope.
     */
    public MapScope() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Create an inner scope.
     * @param next Next outer scope.
     */
    public MapScope(Scope next) {
        this();
        this.next = next;
    }

    @Override
    public Scope getNext() {
        return next;
    }

    @Override
    public FplValue get(String key) {
    	if (map.containsKey(key)) {
    		return map.get(key);
    	}
    	if (next != null) {
    		return next.get(key);
    	}
    	return null;
    }

    @Override
    public void put(String key, FplValue value) throws EvaluationException {
        if (sealed) {
            throw new EvaluationException("Scope is sealed");
        }
        if (key == null || key.length() == 0) {
            throw new EvaluationException("key null or empty");
        }
        // ConcurrentHashMap does not support null values, so we can't put null.
        if (value == null) {
        	map.remove(key);
        } else {
        	map.put(key, value);
        }
    }

	@Override
    public boolean isSealed() {
        return sealed;
    }

    /**
     * @param sealed Is this scope read only?
     */
    public void setSealed(boolean sealed) {
        this.sealed = sealed;
    }

    @Override
    public SortedSet<String> allKeys() {
        SortedSet<String> keySet = new TreeSet<>(map.keySet());
        keySet.addAll(map.keySet());
        return keySet;
    }
}
