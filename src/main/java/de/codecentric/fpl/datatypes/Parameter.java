package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;

public class Parameter implements FplValue {
	private Symbol symbol;
	private int index;

	public Parameter(Symbol symbol, int index) {
		this.symbol = symbol;
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

	public Symbol getSymbol() {
		return symbol;
	}
	
	public String getName() {
		return symbol.getName();
	}
	
	@Override
	public String toString() {
		return symbol.getName();
	}
	
	@Override
	public String typeName() {
		return "parameter";
	}
}
