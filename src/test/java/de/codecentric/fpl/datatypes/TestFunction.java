package de.codecentric.fpl.datatypes;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.parser.Position;

public class TestFunction extends AbstractFunction {

	public TestFunction(Position position, String name, boolean varArg, String... parameterNames) throws EvaluationException {
		super(position, name, "", varArg, parameterNames);
	}

	public TestFunction(String name, boolean varArg, String... parameterNames) throws EvaluationException {
		super(Position.UNKNOWN, name, "", varArg, parameterNames);
	}
	
	@Override
	public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
		return null;
	}
	
}