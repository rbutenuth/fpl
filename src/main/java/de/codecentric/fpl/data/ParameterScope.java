package de.codecentric.fpl.data;

import de.codecentric.fpl.datatypes.FplValue;

public class ParameterScope extends Scope {
	private FplValue[] parameters;
	private FplValue dollar;
	
	public ParameterScope(Scope next, FplValue[] parameters) {
		super(next);
		this.parameters = parameters;
	}

	public ParameterScope(Scope next, ParameterScope scope) {
		super(next);
		this.parameters = scope.parameters;
	}

	public FplValue getParameter(int index) {
		return parameters[index];
	}
	

	public void setDollar(FplValue dollar) {
		this.dollar = dollar;
	}

	@Override
	public FplValue get(String key) {
		return "$".equals(key) ? dollar : super.get(key);
	}

	@Override
	public FplValue getLocal(String key) {
		return "$".equals(key) ? dollar : super.getLocal(key);
	}

	@Override
	public void put(String key, FplValue value) throws ScopeException {
		checkNotDollar(key);
		super.put(key, value);
	}

	@Override
	public void putGlobal(String key, FplValue value) throws ScopeException {
		checkNotDollar(key);
		super.putGlobal(key, value);
	}

	@Override
	public FplValue change(String key, FplValue newValue) throws ScopeException {
		checkNotDollar(key);
		return super.change(key, newValue);
	}

	@Override
	public void define(String key, FplValue value) throws ScopeException {
		checkNotDollar(key);
		super.define(key, value);
	}

	private void checkNotDollar(String key) throws ScopeException {
		if ("$".equals(key)) {
			throw new ScopeException("key $ not allowed");
		}
	}
}
