package de.codecentric.fpl.datatypes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

/**
 * 
 */
public class FplObject extends EvaluatesToThisValue implements Named, Scope {
	private Scope next;
	private String name;
	private boolean sealed;
    private Map<String, FplValue> map;

	/**
	 * @param name
	 * @param next
	 * @throws EvaluationException
	 */
	public FplObject(String name, Scope next) throws EvaluationException {
		if (name == null || name.length() == 0) {
			throw new EvaluationException("empty or null name");
		}
		if (next == null) {
			throw new EvaluationException("next is null");
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

	public void setSealed(boolean sealed) {
		this.sealed = sealed;
	}
	
	@Override
	public boolean isSealed() {
		return sealed;
	}
}
