package de.codecentric.fpl.data;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

/**
 * Just a little bit more than a {@link Map}, can be nested.
 */
public class MapScope implements Scope {
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
	public void put(Named value) throws EvaluationException {
		put(value.getName(), value);
	}
	
    @Override
    public void putGlobal(String key, FplValue value) throws EvaluationException {
        Scope chain = this;
        while (chain.getNext() != null && !chain.getNext().isSealed()) {
            chain = chain.getNext();
        }
        chain.put(key, value);
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

    /**
     * @return All keys from this scope and all parent scopes, ordered by natural {@link String} order.
     */
    public SortedSet<String> allKeys() {
        SortedSet<String> keySet = new TreeSet<>(map.keySet());
        Scope chain = next;
        while (chain != null) {
        	if (chain instanceof MapScope) {
        		keySet.addAll(((MapScope)chain).map.keySet());
        	}
            chain = chain.getNext();
        }
        return keySet;
    }
}
