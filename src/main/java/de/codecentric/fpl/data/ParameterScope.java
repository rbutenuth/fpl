package de.codecentric.fpl.data;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

public class ParameterScope implements Scope {
	private FplValue[] parameters;
	private Scope next;
	
	public ParameterScope(Scope next, int count) {
		if (next == null) {
			throw new NullPointerException("next");
		}
		this.next = next;
		parameters = new FplValue[count];
	}

	public void setParameter(int index, FplValue value) {
		parameters[index] = value;
	}

	public FplValue getParameter(int index) {
		return parameters[index];
	}

	@Override
	public Scope getNext() {
		return next;
	}

	@Override
	public FplValue get(String key) {
		return next.get(key);
	}

	@Override
	public void put(String key, FplValue value) throws EvaluationException {
		next.put(key, value);
	}
	
	@Override
	public void put(Named value) throws EvaluationException {
		next.put(value);
	}

	@Override
	public void putGlobal(String key, FplValue value) throws EvaluationException {
		next.putGlobal(key, value);
	}

	@Override
	public boolean isSealed() {
		return next.isSealed();
	}
}
