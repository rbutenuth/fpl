package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;

public class Parameter implements FplValue {
	private Symbol symbol;
	private int index;
	private int depth;

	public Parameter(Symbol symbol, int index) {
		this.symbol = symbol;
		this.index = index;
	}

	public Parameter(Parameter parameter) {
		this.symbol = parameter.symbol;
		this.index = parameter.index;
		depth = parameter.depth + 1;
	}

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		int walkDistance = depth;
		Scope workScope = scope;
		while (walkDistance > 0) {
			workScope = workScope.getNext();
			if (workScope instanceof ParameterScope) {
				walkDistance--;
			}
		}
		ParameterScope paramScope = (ParameterScope) workScope;
		FplValue value = paramScope.getParameter(index);
		if (value instanceof LazyExpression) {
			value = value.evaluate(scope);
		}
		return value;
	}

	public FplValue quote(Scope scope) throws EvaluationException {
		ParameterScope paramScope = (ParameterScope) scope;
		FplValue parameter = paramScope.getParameter(index);
		if (parameter instanceof LazyExpression) {
			return ((LazyExpression) parameter).getOriginalExpression();
		} else {
			return parameter;
		}
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
