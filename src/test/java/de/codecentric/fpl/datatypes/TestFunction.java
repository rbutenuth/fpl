package de.codecentric.fpl.datatypes;

import java.util.ArrayList;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.parser.Position;

public class TestFunction extends AbstractFunction {

	public TestFunction(Position position, String name, boolean varArg, String... parameterNames) {
		super(position, new ArrayList<>(), name, varArg, parameterNames);
	}

	public TestFunction(String name, boolean varArg, String... parameterNames) {
		super(Position.UNKNOWN, new ArrayList<>(), name, varArg, parameterNames);
	}
	
	@Override
	public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
		return null;
	}
	
}