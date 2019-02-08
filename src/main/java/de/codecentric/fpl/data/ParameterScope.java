package de.codecentric.fpl.data;

import de.codecentric.fpl.datatypes.FplValue;

public class ParameterScope extends Scope {
	private FplValue[] parameters;
	
	public ParameterScope(Scope next, int count) {
		super(next);
		parameters = new FplValue[count];
	}

	public void setParameter(int index, FplValue value) {
		parameters[index] = value;
	}

	public FplValue getParameter(int index) {
		return parameters[index];
	}
}
