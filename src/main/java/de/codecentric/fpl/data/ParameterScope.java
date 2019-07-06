package de.codecentric.fpl.data;

import de.codecentric.fpl.datatypes.FplValue;

public class ParameterScope extends Scope {
	private FplValue[] parameters;
	
	public ParameterScope(String name, Scope next, FplValue[] parameters) {
		super(name, next);
		this.parameters = parameters;
	}

	public ParameterScope(String name, Scope next, ParameterScope scope) {
		super(name, next);
		this.parameters = scope.parameters;
	}

	public FplValue getParameter(int index) {
		return parameters[index];
	}
}
