package de.codecentric.fpl.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

/**
 * A map scope with one additional entry. Used to implement pipelines.
 * The inherited methods are not allowed to change the value for this special symbol.
 */
public class PipelineScope extends MapScope {
	private final String pipeSymbol;
	private FplValue pipeValue;
	
	public PipelineScope(String name, Scope next) {
		super("pipeline-" + name, next);
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("pipeline symbol not allowed");
		}
		pipeSymbol = name;
	}

	@Override
	public FplValue get(String key) {
		if (pipeSymbol.equals(key)) {
			return pipeValue;
		} else {
			return super.get(key);
		}
	}

	public FplValue get() {
		return pipeValue;
	}
	
	public void set(FplValue value) {
		pipeValue = value;
	}
	
	@Override
	public FplValue put(String key, FplValue value) throws ScopeException {
		checkNotPipeSymbol(key);
		return super.put(key, value);
	}

	@Override
	public FplValue replace(String key, FplValue value) throws ScopeException {
		checkNotPipeSymbol(key);
		return super.replace(key, value);
	}

	@Override
	public FplValue replaceLocal(String key, FplValue value) throws ScopeException {
		checkNotPipeSymbol(key);
		return super.replaceLocal(key, value);
	}

	@Override
	public FplValue define(String key, FplValue value) throws ScopeException {
		checkNotPipeSymbol(key);
		return super.define(key, value);
	}

	@Override
	public void define(Named value) throws ScopeException {
		checkNotPipeSymbol(value.getName());
		super.define(value);
	}

	@Override
	public Iterator<Entry<String, FplValue>> iterator() {
		ConcurrentMap<String, FplValue> mapWithPipe = new ConcurrentHashMap<String, FplValue>(map);
		mapWithPipe.put(pipeSymbol, pipeValue);
		return mapWithPipe.entrySet().iterator();
	}

	@Override
	public Set<String> keySet() {
		Set<String> setWithPipe = new HashSet<>(map.keySet());
		setWithPipe.add(pipeSymbol);
		return setWithPipe;
	}

	@Override
	public Collection<FplValue> values() {
		Collection<FplValue> valuesWithPipe = new ArrayList<>(map.size() + 1);
		valuesWithPipe.addAll(super.values());
		valuesWithPipe.add(pipeValue);
		return valuesWithPipe;
	}

	@Override
	public Set<Entry<String, FplValue>> entrieSet() {
		ConcurrentMap<String, FplValue> mapWithPipe = new ConcurrentHashMap<String, FplValue>(map);
		mapWithPipe.put(pipeSymbol, pipeValue);
		return mapWithPipe.entrySet();
	}

	@Override
	public String toString() {
		return "PipelineScope<" + name + ">";
	}

	private void checkNotPipeSymbol(String key) throws ScopeException {
		if (pipeSymbol.equals(key)) {
			throw new ScopeException("replacement of pipeline symbol not allowed");
		}
	}
}
