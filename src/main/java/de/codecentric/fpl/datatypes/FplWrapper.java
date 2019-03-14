package de.codecentric.fpl.datatypes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

public class FplWrapper extends EvaluatesToThisValue {
	private Object instance;
	
	public FplWrapper(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("can't wrap null");
		}
		instance = value;
	}
	
	public FplWrapper(String className) throws EvaluationException {
		try {
			Class<?> clazz = Class.forName(className);
			instance = clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new EvaluationException("unknown class: " + className);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new EvaluationException(e.getMessage());
		}
	}

	@Override
	public String typeName() {
		return "wrapper(" + instance.getClass().getName() + ")";
	}
	
	public Object getInstance() {
		return instance;
	}

	public FplValue evaluate(Scope scope, FplValue[] parameters) throws EvaluationException {
		FplValue first = parameters[0];
		String methodName = null;
		if (first instanceof Symbol) {
			methodName = ((Symbol)first).getName();
		} else {
			methodName = ((FplString)first.evaluate(scope)).getContent();
		}
		try {
			Method method = instance.getClass().getMethod(methodName, new Class<?>[0]);
			Object result = method.invoke(instance, null);
			return wrapResult(result);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new EvaluationException(e.getMessage());
		}
	}

	private FplValue wrapResult(Object result) {
		FplValue value;
		if (result == null) {
			value = null;
		} else if (result instanceof FplValue) {
			value = (FplValue)result;
		} else if (result instanceof Byte || result instanceof Short || result instanceof Integer || result instanceof Long) {
			value = FplInteger.valueOf(((Number)result).longValue());
		} else if (result instanceof Float || result instanceof Double) {
			value = new FplDouble(((Number)result).doubleValue());
		} else if (result instanceof Character) {
			value = new FplString("" + result);
		} else if (result instanceof String) {
			value = new FplString((String)result);
		} else if (result instanceof Boolean) {
			boolean b = ((Boolean)result).booleanValue();
			value = b ? FplInteger.valueOf(1) : null;
		} else {
			value = new FplWrapper(result);
		}
		return value;
	}
}
