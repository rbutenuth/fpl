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
		} else if (result instanceof Number) {
			value = FplInteger.valueOf(((Number)result).longValue());
		} else {
			value = new FplWrapper(result);
		}
		return value;
	}
}
