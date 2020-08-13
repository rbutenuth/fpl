package de.codecentric.fpl.data;

import de.codecentric.fpl.datatypes.FplValue;

public class ParameterScope extends Scope {
	private String[] parameterNames;
	private FplValue[] parameters;
	
	public ParameterScope(String name, Scope next, String[] parameterNames, FplValue[] parameters) {
		super(name, next);
		this.parameterNames = parameterNames;
		this.parameters = parameters;
	}

	public ParameterScope(String name, Scope next, ParameterScope scope) {
		super(name, next);
		parameterNames = scope.parameterNames;
		this.parameters = scope.parameters;
	}

	public FplValue getParameter(int index) {
		return parameters[index];
	}
	
	/**
	 * Lookup a symbol, first in <code>map</code>, then in list of parameters.
	 * If not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found expression, may be null.
	 */
	public FplValue get(String key) {
		FplValue value = map.get(key);
		if (value != null) {
			return value;
		}
		for (int i = 0; i < parameters.length; i++) {
			if (key.equals(parameterNames[i])) {
				return parameters[i];
			}
		}
		if (next != null) {
			return next.get(key);
		}
		return null;
	}
}
