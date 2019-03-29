package de.codecentric.fpl.data;

import de.codecentric.fpl.datatypes.FplValue;

public class ParameterScope extends Scope {
	private FplValue[] parameters;
	
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
}
