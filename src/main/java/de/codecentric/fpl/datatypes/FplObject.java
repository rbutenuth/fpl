package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ListableScope;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.parser.Position;

/**
 * The FPL version of an object: Most of the semantic comes from a combination
 * of {@link Scope} with {@link Named}. The rest are some built in functions for
 * linking and executing methods on objects.
 */
public class FplObject extends EvaluatesToThisValue implements PositionHolder, ListableScope {
	private Position position;
	private Scope next;
	private boolean sealed;
	private ConcurrentMap<String, FplValue> map;
	private List<FplValue> initCode;

	/**
	 * @param next Next outer {@link Scope}
	 */
	public FplObject(Position position) throws IllegalArgumentException {
		if (position == null) {
			throw new IllegalArgumentException("position is null");
		}
		this.position = position;
		map = new ConcurrentHashMap<>();
		initCode = Collections.emptyList();
	}

	@Override
	public Scope getNext() {
		return next;
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
		if (next == null) {
			next = scope;
			for (FplValue v : initCode) {
				v.equals(this);
			}
		}
		return this;
	}

	@Override
	public FplValue get(String key) {
		FplValue value = map.get(key);
		if (value != null) {
			return value;
		}
		if (next != null) {
			return next.get(key);
		}
		return null;
	}

	@Override
	public void put(String key, FplValue value) throws EvaluationException {
		if (sealed) {
			throw new EvaluationException("Scope is sealed");
		}
		if (key == null || key.length() == 0) {
			throw new EvaluationException("key null or empty");
		}
		// ConcurrentHashMap does not support null values, so we can't put null.
		if (value == null) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}

	public void putUnsafe(String key, FplValue value) {
		map.put(key, value);
	}

	@Override
	public SortedSet<String> allKeys() {
		SortedSet<String> keySet = new TreeSet<>(map.keySet());
		keySet.addAll(map.keySet());
		return keySet;
	}

	public void setSealed(boolean sealed) {
		this.sealed = sealed;
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public Position getPosition() {
		return position;
	}
}
