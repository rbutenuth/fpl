package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.parser.Position;

/**
 * The FPL version of an object: Most of the semantic comes from a combination
 * of {@link Scope} with {@link Named}. The rest are some built in functions for
 * linking and executing methods on objects.
 */
public class FplObject extends Scope implements PositionHolder, FplValue {
	private Position position;
	private List<FplValue> initCode;

	/**
	 * @param next Next outer {@link Scope}
	 */
	public FplObject(Position position) throws IllegalArgumentException {
		if (position == null) {
			throw new IllegalArgumentException("position is null");
		}
		this.position = position;
		initCode = Collections.emptyList();
	}

	public synchronized void addInitCodeValue(FplValue value) {
		// Replace unmodifiable emptyList with empty array list
		if (initCode.isEmpty()) {
			initCode = new ArrayList<FplValue>();
		}
		initCode.add(value);
	}

	public synchronized List<FplValue> getInitCode() {
		return Collections.unmodifiableList(initCode);
	}

	@Override
	public synchronized FplValue evaluate(Scope scope) throws EvaluationException {
		if (scope == null) {
			throw new IllegalArgumentException("Scope of object can't be null");
		}
		if (getNext() == null) {
			initNext(scope);
			for (FplValue v : initCode) {
				v.evaluate(this);
			}
		}
		return this;
	}

	@Override
	public Position getPosition() {
		return position;
	}
}
