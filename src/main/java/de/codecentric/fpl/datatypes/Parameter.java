package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;

public class Parameter implements Named {
	private Symbol symbol;
	private int index;

	public Parameter(Symbol symbol, int index) {
		this.symbol = symbol;
		this.index = index;
	}

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		ParameterScope paramScope = findNextParameterScope(scope);
		FplValue value = paramScope.getParameter(index);
		if (value instanceof FplLazy) {
			value = value.evaluate(scope);
		}
		return value;
	}

	public FplValue quote(Scope scope) throws EvaluationException {
		ParameterScope paramScope = findNextParameterScope(scope);
		FplValue parameter = paramScope.getParameter(index);
		if (parameter instanceof FplLazy) {
			return ((FplLazy) parameter).getOriginalExpression();
		} else {
			return parameter;
		}
	}

	public Symbol getSymbol() {
		return symbol;
	}

	@Override
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
	
	private ParameterScope findNextParameterScope(Scope scope) throws EvaluationException {
		while (scope != null && !(scope instanceof ParameterScope)) {
			scope = scope.getNext();
		}
		if (scope == null) {
			throw new EvaluationException("not nested in ParameterScope");
		}
		return (ParameterScope) scope;
	}
}
