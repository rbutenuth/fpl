package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;

public class Parameter implements FplValue {
	private String name;
	private int index;

	public Parameter(String name, int index) {
		this.name = name;
		this.index = index;
	}

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		ParameterScope paramScope = (ParameterScope) scope;
        FplValue value = paramScope.getParameter(index);
        if (value instanceof LazyExpression) {
        	value = value.evaluate(scope);
        }
        return value;
	}
	
	public FplValue quote(Scope scope) throws EvaluationException {
		ParameterScope paramScope = (ParameterScope) scope;
        return paramScope.getParameter(index);
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
