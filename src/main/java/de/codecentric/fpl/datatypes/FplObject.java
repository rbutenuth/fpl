package de.codecentric.fpl.datatypes;

import java.util.Arrays;
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * The FPL version of an object: Most of the semantic comes from a combination
 * of {@link Scope} with {@link Named}. The rest are some built in functions for
 * linking and executing methods on objects.
 */
public class FplObject extends MapScope implements Function, PositionHolder {
	private Position position;

	public FplObject(String name) {
		super(name);
		position = Position.UNKNOWN;
	}

	/**
	 * Create an object which is not in the scope chain: A simple dictionary.
	 * 
	 * @param position Where it is defined in the source
	 */
	public FplObject(String name, Position position) throws IllegalArgumentException {
		super(name);
		if (position == null) {
			throw new IllegalArgumentException("position is null");
		}
		this.position = position;
	}

	/**
	 * Create a "real" object, with predecessor scope.
	 * 
	 * @param position Where it is defined in the source
	 * @param next     Next outer {@link Scope}
	 */
	public FplObject(String name, Position position, Scope next) throws IllegalArgumentException {
		this(name, position);
		setNext(next);
	}

	@Override
	public FplValue call(Scope scope, FplValue... parameters) throws EvaluationException {
		Function firstElement = AbstractFunction.evaluateToFunction(this, parameters[0]);
		FplValue[] shiftedParameters = Arrays.copyOfRange(parameters, 1, parameters.length);
		return firstElement.call(this, shiftedParameters);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public String typeName() {
		return "object";
	}

	@Override
	public String toString() {
		final String NL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<String, FplValue> entry : map.entrySet()) {
			sb.append(NL).append("    ");
			sb.append(entry.getKey()).append(": ");
			FplValue v = entry.getValue();
			// Be careful: Cycles in data structures can lead to endless recursion...
			if (v instanceof FplList) {
				sb.append("<list>");
			} else if (v instanceof FplDictionary) {
				sb.append("<dictionary>");
			} else if (v instanceof FplObject) {
				sb.append("<object>");
			} else {
				sb.append(v.toString());
			}
		}
		sb.append(NL).append("}").append(NL);

		return sb.toString();
	}
	
}
