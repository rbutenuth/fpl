package de.codecentric.fpl.datatypes;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ListableScope;
import de.codecentric.fpl.data.Scope;

/**
 * The FPL version of an object: Most of the semantic comes from a combination of
 * {@link Scope} with {@link Named}. The rest are some built in functions for 
 * linking and executing methods on objects.  
 */
public class FplObject extends EvaluatesToThisValue implements Named, ListableScope {
	private Scope next;
	private String name;
	private boolean sealed;
    private Map<String, FplValue> map;

	/**
	 * @param name The key in the {@link Scope} where this object lives.
	 * @param next Next outer {@link Scope}
	 * @throws IllegalArgumentException In case of <code>null</code> or empty name.
	 */
	public FplObject(String name, Scope next) throws IllegalArgumentException {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("empty or null name");
		}
		this.name = name;
		this.next = next;
        map = new ConcurrentHashMap<>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Scope getNext() {
		return next;
	}

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		if (next == null) {
			next = this;
		}
		return this;
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
    public SortedSet<String> allKeys() {
        SortedSet<String> keySet = new TreeSet<>(map.keySet());
        keySet.addAll(map.keySet());
        return keySet;
    }
    
	public void setSealed(boolean sealed) {
		this.sealed = sealed;
	}
	
	@Override
	public boolean isSealed() {
		return sealed;
	}
}
