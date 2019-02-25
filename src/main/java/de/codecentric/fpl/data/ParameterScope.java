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
	public FplValue put(String key, FplValue value) throws ScopeException {
		checkNotDollar(key);
		return super.put(key, value);
	}

	@Override
	public FplValue replace(String key, FplValue newValue) throws ScopeException {
		checkNotDollar(key);
		return super.replace(key, newValue);
	}

	@Override
	public FplValue define(String key, FplValue value) throws ScopeException {
		checkNotDollar(key);
		return super.define(key, value);
	}

	private void checkNotDollar(String key) throws ScopeException {
		if ("$".equals(key)) {
			throw new ScopeException("key $ not allowed");
		}
	}
}
