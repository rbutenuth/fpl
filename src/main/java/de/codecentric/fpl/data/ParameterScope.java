package de.codecentric.fpl.data;

import java.util.Set;

import de.codecentric.fpl.datatypes.FplValue;

public class ParameterScope extends Scope {
	private Set<String> parameterNames;
	private FplValue[] parameters;

	public ParameterScope(String name, Scope next, Set<String> parameterNames, FplValue[] parameters) {
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
	 * Lookup a symbol, first in <code>map</code>, then in list of parameters. If
	 * not found in this scope, walk chain of scopes.
	 * 
	 * @param key Name of value to lookup
	 * @return The found expression, may be null.
	 */
	public FplValue get(String key) {
		FplValue value = map.get(key);
		if (value != null) {
			return value;
		}
		if (parameterNames.contains(key)) {
			int i = 0;
			for (String name: parameterNames) {
				if (key.equals(name)) {
					return parameters[i];
				}
				i++;
			}
		}
		return next.get(key);
	}
}
