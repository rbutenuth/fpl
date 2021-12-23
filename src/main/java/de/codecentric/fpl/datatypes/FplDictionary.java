package de.codecentric.fpl.datatypes;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import de.codecentric.fpl.data.ScopeException;

public interface FplDictionary {

	public FplValue get(String key);
	
	public FplValue put(String key, FplValue value) throws ScopeException;
	
	public FplValue define(String key, FplValue value) throws ScopeException;
	
	public FplValue replace(String key, FplValue newValue) throws ScopeException;
	
	public Set<String> keySet();
	
	public Collection<FplValue> values();
	
	public Set<Entry<String, FplValue>> entrieSet();
}
