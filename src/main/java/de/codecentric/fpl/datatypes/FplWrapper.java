package de.codecentric.fpl.datatypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

public class FplWrapper extends AbstractFunction {
	private Class<?> clazz;
	private Object instance;

	// String name, List<String> comment, boolean varArg, String... parameterNames) {
	public FplWrapper(Object value) {
		super(value.getClass().getName(), Collections.emptyList(), true, "args...");
		clazz = value.getClass();
		instance = value;
	}

	public FplWrapper(String className, Object[] methodParams) throws EvaluationException {
		super(className, Collections.emptyList(), true, "args...");
		unwrap(methodParams);
		try {
			clazz = Class.forName(className);
			if (methodParams.length == 0) {
				instance = clazz.newInstance();
			} else {
				Constructor<?>[] constructors = clazz.getConstructors();
				Constructor<?> constructor = findBestMatch(constructors, methodParams);
				instance = constructor.newInstance(methodParams);
			}
		} catch (ClassNotFoundException e) {
			throw new EvaluationException("unknown class: " + className);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}

	@Override
	public String typeName() {
		return "wrapper(" + clazz.getName() + ")";
	}

	@Override
	protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
		FplValue first = parameters[0];
		String methodName = null;
		if (first instanceof Symbol) {
			methodName = ((Symbol) first).getName();
		} else {
			methodName = ((FplString) first.evaluate(scope)).getContent();
		}
		try {
			Method method = clazz.getMethod(methodName, new Class<?>[0]);
			Object result = method.invoke(instance, null);
			return wrapResult(result);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new EvaluationException(e.getMessage());
		}
	}

	public Object getInstance() {
		return instance;
	}
	
	/**
	 * Find the best matching method (or constructor) for the given arguments. The
	 * array may be partially filled, so stop after the first <code>null</code>
	 * entry.
	 * 
	 * @param executables methods / constructors
	 * @param params      parameters for the method / constructor
	 * @return Best match, <code>null</code> when no match is found.
	 * @throws EvaluationException If no matching method is found.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Executable> T findBestMatch(Executable[] executables, Object[] params)
			throws EvaluationException {
		int targetIndex = 0;
		for (int sourceIndex = 0; sourceIndex < executables.length && executables[sourceIndex] != null; sourceIndex++) {
			if (isMatch(executables[sourceIndex], params)) {
				executables[targetIndex++] = executables[sourceIndex];
			}
		}
		if (targetIndex < executables.length) {
			executables[targetIndex] = null;
		}
		if (targetIndex == 0) {
			throw new EvaluationException("No matching method/constructor found");
		} else if (targetIndex == 1) {
			coerceParameters(executables[0], params);
			return (T) executables[0]; // line needing the "unchecked"
		} else {
			int e = bestMatch(executables, params);
			coerceParameters(executables[e], params);
			return (T) executables[e]; // line needing the "unchecked"
		}
	}

	private void coerceParameters(Executable executable, Object[] values) {
		Parameter[] parameters = executable.getParameters();
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				Class<?> paramClass = parameters[i].getType();
				if (paramClass.equals(Integer.class) || paramClass.equals(int.class)) {
					values[i] = ((Number) values[i]).intValue();
				} else if (paramClass.equals(Short.class) || paramClass.equals(short.class)) {
					values[i] = ((Number) values[i]).shortValue();
				} else if (paramClass.equals(Byte.class) || paramClass.equals(byte.class)) {
					values[i] = ((Number) values[i]).byteValue();
				} else if (paramClass.equals(Long.class) || paramClass.equals(long.class)) {
					values[i] = ((Number) values[i]).longValue();
				} else if (paramClass.equals(Float.class) || paramClass.equals(float.class)) {
					values[i] = ((Number) values[i]).floatValue();
				} else if (paramClass.equals(Double.class) || paramClass.equals(double.class)) {
					values[i] = ((Number) values[i]).doubleValue();
				} else if (paramClass.equals(Boolean.class) || paramClass.equals(boolean.class)) {
					values[i] = ((Number) values[i]).intValue() != 0;
				}
			}
		}
	}

	private boolean isMatch(Executable executable, Object[] values) {
		if (executable.getParameterCount() != values.length) {
			return false;
		}
		Parameter[] parameters = executable.getParameters();
		for (int i = 0; i < values.length; i++) {
			if (!isMatch(parameters[i], values[i])) {
				return false;
			}
		}
		return true;
	}

	private boolean isMatch(Parameter parameter, Object value) {
		Class<?> parameterType = parameter.getType();
		if (parameterType.isPrimitive() && value == null) {
			return false;
		}
		if (value == null) {
			return true;
		}
		Class<?> valueType = value.getClass();
		if (isNumberLike(parameterType) && isNumberLike(valueType)) {
			return true;
		}
		return parameterType.isAssignableFrom(value.getClass());
	}

	private boolean isNumberLike(Class<?> clazz) {
		if (Number.class.isAssignableFrom(clazz)) {
			return true;
		}
		// number like primitives are: Byte, Short, Integer, Long, Float, Double,
		// Boolean (1, 0)
		// non number like primitives are: Void, Character
		if (Boolean.class.equals(clazz)) {
			return true;
		}
		if (!clazz.isPrimitive()) {
			return false;
		}
		// now clazz is primitive
		return !(clazz.equals(void.class) || clazz.equals(char.class));
	}

	private int bestMatch(Executable[] executables, Object[] params) {
		int bestValue = 0;
		int bestIndex = 0;
		for (int i = 0; i < executables.length && executables[i] != null; i++) {
			int value = computeMatchValue(executables[i], params);
			if (value > bestValue) {
				bestValue = value;
				bestIndex = i;
			}
		}
		return bestIndex;
	}

	private int computeMatchValue(Executable executable, Object[] values) {
		Parameter[] parameters = executable.getParameters();
		int match = 0;
		for (int i = 0; i < parameters.length; i++) {
			match += computeMatchValue(parameters[i], values[i]);
		}
		return match;
	}

	private int computeMatchValue(Parameter parameter, Object value) {
		Class<?> pType = parameter.getType();
		Class<?> vType = value == null ? null : value.getClass();
		if (pType.equals(vType)) {
			return 100;
		}
		if (isIntegralNumber(pType) == isIntegralNumber(vType)) {
			return 60;
		}
		if (isFractionalNumber(pType) == isFractionalNumber(vType)) {
			return 60;
		}
		if (pType.isAssignableFrom(vType)) {
			return 50;
		}

		return 0;
	}

	private boolean isIntegralNumber(Class<?> clazz) {
		return byte.class.equals(clazz) || Byte.class.equals(clazz) //
				|| short.class.equals(clazz) || Short.class.equals(clazz) //
				|| int.class.equals(clazz) || Integer.class.equals(clazz) //
				|| long.class.equals(clazz) || Long.class.equals(clazz);
	}

	private boolean isFractionalNumber(Class<?> clazz) {
		return float.class.equals(clazz) || Float.class.equals(clazz) || double.class.equals(clazz) || Double.class.equals(clazz);
	}

	private FplValue wrapResult(Object result) {
		FplValue value;
		if (result == null) {
			value = null;
		} else if (result instanceof FplValue) {
			value = (FplValue) result;
		} else if (result instanceof Byte || result instanceof Short || result instanceof Integer
				|| result instanceof Long) {
			value = FplInteger.valueOf(((Number) result).longValue());
		} else if (result instanceof Float || result instanceof Double) {
			value = new FplDouble(((Number) result).doubleValue());
		} else if (result instanceof Character) {
			value = new FplString("" + result);
		} else if (result instanceof String) {
			value = new FplString((String) result);
		} else if (result instanceof Boolean) {
			boolean b = ((Boolean) result).booleanValue();
			value = b ? FplInteger.valueOf(1) : null;
		} else {
			value = new FplWrapper(result);
		}
		return value;
	}

	private void unwrap(Object[] params) {
		for (int i = 0; i < params.length; i++) {
			Object p = params[i];
			if (p instanceof FplWrapper) {
				params[i] = ((FplWrapper) p).getInstance();
			} else if (p instanceof FplString) {
				params[i] = ((FplString) p).getContent();
			} else if (p instanceof FplDouble) {
				params[i] = Double.valueOf(((FplDouble) p).getValue());
			} else if (p instanceof FplInteger) {
				params[i] = Long.valueOf(((FplInteger) p).getValue());
			}
		}
	}
}
