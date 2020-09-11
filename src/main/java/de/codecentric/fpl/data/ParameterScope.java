package de.codecentric.fpl.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

public class ParameterScope extends MapScope {
	private Map<String, Integer> nameToIndex;
	private FplValue[] parameters;

	public ParameterScope(String name, Scope next, Map<String, Integer> nameToIndex, FplValue[] parameters) {
		super(name, next);
		this.nameToIndex = nameToIndex;
		this.parameters = parameters;
	}

	public FplValue getParameter(int index) {
		return parameters[index];
	}

	/**
	 * Lookup a symbol, first in list of parameters. 
	 * If not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found expression, may be null.
	 */
	public FplValue get(String key) {
		Integer index = nameToIndex.get(key);
		if (index != null) {
			return parameters[index];
		}
		return super.get(key);
	}

	@Override
	public Scope createNested(String name) {
		return new ParameterScope(name, this, nameToIndex, parameters);
	}
	
	@Override
	public FplValue put(String key, FplValue value) throws ScopeException {
		checkNotParameterName(key);
		return super.put(key,  value);
	}

	@Override
	public FplValue replace(String key, FplValue newValue) throws ScopeException {
		checkNotParameterName(key);
		return super.replace(key, newValue);
	}

	@Override
	public FplValue replaceLocal(String key, FplValue newValue) throws ScopeException {
		checkNotParameterName(key);
		return super.replaceLocal(key, newValue);
	}

	@Override
	public FplValue define(String key, FplValue value) throws ScopeException {
		checkNotParameterName(key);
		return super.define(key, value);
	}

	@Override
	public void define(Named value) throws ScopeException {
		checkNotParameterName(value.getName());
		super.define(value);
	}

	@Override
	public Iterator<Entry<String, FplValue>> iterator() {
		return entrieSet().iterator();
	}

	@Override
	public Set<String> keySet() {
		Set<String> result = new HashSet<>();
		result.addAll(nameToIndex.keySet());
		result.addAll(map.keySet());
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Collection<FplValue> values() {
		Collection<FplValue> result = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			result.add(parameters[i]);
		}
		result.addAll(map.values());
		return Collections.unmodifiableCollection(result);
	}

	@Override
	public Set<Entry<String, FplValue>> entrieSet() {
		Map<String, FplValue> result = new HashMap<>();
		for (Entry<String, Integer> entry : nameToIndex.entrySet()) {
			result.put(entry.getKey(), parameters[entry.getValue()]);
		}
		result.putAll(map);;
		return Collections.unmodifiableSet(result.entrySet());
	}

	private void checkNotParameterName(String key) throws ScopeException {
		if (nameToIndex.containsKey(key)) {
			throw new ScopeException("Change of parameters not allowed");
		}
	}
}
