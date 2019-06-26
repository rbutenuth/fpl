package de.codecentric.fpl.datatypes;

import java.util.Arrays;
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.parser.Position;

/**
 * The FPL version of an object: Most of the semantic comes from a combination
 * of {@link Scope} with {@link Named}. The rest are some built in functions for
 * linking and executing methods on objects.
 */
public class FplObject extends Scope implements PositionHolder, FplValue, Function {
	private static String NL = System.lineSeparator();
	private Position position;

	public FplObject() {
		position = Position.UNKNOWN;
	}

	/**
	 * @param position Where it is defined in the source
	 */
	public FplObject(Position position) throws IllegalArgumentException {
		if (position == null) {
			throw new IllegalArgumentException("position is null");
		}
		this.position = position;
	}

	/**
	 * @param position Where it is defined in the source
	 * @param next     Next outer {@link Scope}
	 */
	public FplObject(Position position, Scope next) throws IllegalArgumentException {
		this(position);
		setNext(next);
	}

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		return this;
	}

	@Override
	public FplValue call(Scope scope, FplValue[] parameters) throws EvaluationException {
		Scope callScope;
		if (scope instanceof ParameterScope) {
			callScope = new ParameterScope(this, (ParameterScope) scope);
		} else {
			callScope = this;
		}
		FplValue firstElement = parameters[0].evaluate(callScope);

		if (firstElement instanceof Function) {
			FplValue[] shiftedParameters = Arrays.copyOfRange(parameters, 1, parameters.length);
			return ((Function) firstElement).call(callScope, shiftedParameters);
		} else {
			throw new EvaluationException("Not a function: " + firstElement);
		}
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public String typeName() {
		return next == null ? "object" : "class";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<String, FplValue> entry : map.entrySet()) {
			sb.append(NL).append("    ");
			sb.append(entry.getKey()).append(": ").append(entry.getValue().toString());
		}
		sb.append(NL).append("}").append(NL);

		return sb.toString();
	}
}
