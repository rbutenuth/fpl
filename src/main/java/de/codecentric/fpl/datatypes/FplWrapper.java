package de.codecentric.fpl.datatypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;

public class FplWrapper extends AbstractFunction {
	private static final Map<Class<?>, Class<?>> primitive2Wrapper = new HashMap<>();
	static {
		primitive2Wrapper.put(Boolean.TYPE, Boolean.class);
		primitive2Wrapper.put(Byte.TYPE, Byte.class);
		primitive2Wrapper.put(Character.TYPE, Character.class);
		primitive2Wrapper.put(Short.TYPE, Short.class);
		primitive2Wrapper.put(Integer.TYPE, Integer.class);
		primitive2Wrapper.put(Long.TYPE, Long.class);
		primitive2Wrapper.put(Double.TYPE, Double.class);
		primitive2Wrapper.put(Float.TYPE, Float.class);
		primitive2Wrapper.put(Void.TYPE, Void.TYPE);
	}

	private final Class<?> clazz;
	private final Object instance;

	public FplWrapper(Object value) throws EvaluationException {
		super(value.getClass().getName(), "", true, "args...");
		clazz = value.getClass();
		instance = value;
	}

	public FplWrapper(String className) throws EvaluationException {
		super(className, "", true, "args...");
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new EvaluationException("unknown class: " + className);
		}
		instance = null;
	}

	public FplWrapper(String className, Object[] methodParams) throws EvaluationException {
		super(className, "", true, "args...");
		unwrap(methodParams);
		try {
			clazz = Class.forName(className);
			Constructor<?>[] constructors = clazz.getConstructors();
			Constructor<?> constructor = findBestMatch(className, constructors, methodParams);
			instance = constructor.newInstance(methodParams);
		} catch (ClassNotFoundException e) {
			throw new EvaluationException("unknown class: " + className);
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}

	@Override
	public String typeName() {
		return "wrapper(" + clazz.getName() + ")";
	}

	@Override
	protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
		FplValue first = parameters[0];
		String name = null;
		if (first instanceof Symbol) {
			name = ((Symbol) first).getName();
		} else {
			name = ((FplString) first.evaluate(scope)).getContent();
		}
		try {
			Object[] javaParams = evaluatedParams(parameters);
			Method[] executables = clazz.getMethods();
			Method method = findBestMatch(name, executables, javaParams);
			Object result = method.invoke(instance, javaParams);
			return UnWrapper.wrap(result);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}

	public Object getInstance() {
		return instance;
	}

	@Override
	public String toString() {
		return "FplWrapper(\"" + clazz.getName() + "\")";
	}
	
	/**
	 * Find the best matching method (or constructor) for the given arguments.
	 *
	 * @param name        Name of method (or class name when searching constructors
	 * @param executables methods / constructors
	 * @param params      parameters for the method / constructor
	 * @return Best match, <code>null</code> when no match is found.
	 * @throws EvaluationException If no matching method is found.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Executable> T findBestMatch(String name, Executable[] executables, Object[] params) throws EvaluationException {
		int targetIndex = 0;
		for (int sourceIndex = 0; sourceIndex < executables.length; sourceIndex++) {
			if (isMatch(name, executables[sourceIndex], params)) {
				executables[targetIndex++] = executables[sourceIndex];
			}
		}
		if (targetIndex < executables.length) {
			executables[targetIndex] = null;
		}
		if (targetIndex == 0) {
			throw new EvaluationException("No matching method with name " + name + " found");
		} else if (targetIndex == 1) {
			coerceParameters(executables[0], params);
			return (T) executables[0]; // line needing the "unchecked"
		} else {
			int e = bestMatch(executables, targetIndex, params);
			coerceParameters(executables[e], params);
			return (T) executables[e]; // line needing the "unchecked"
		}
	}

	private void coerceParameters(Executable executable, Object[] values) {
		Parameter[] parameters = executable.getParameters();
		for (int i = 0; i < values.length; i++) {
			Object value = values[0];
			if (values[i] != null) {
				Class<?> paramClass = parameters[i].getType();
				if (paramClass.equals(Boolean.class) || paramClass.equals(boolean.class)) {
					if (value instanceof Boolean) {
						values[i] = value;
					} else {
						values[i] = ((Number) value).intValue() != 0;
					}
				} else if (paramClass.equals(Integer.class) || paramClass.equals(int.class)) {
					values[i] = ((Number) value).intValue();
				} else if (paramClass.equals(Short.class) || paramClass.equals(short.class)) {
					values[i] = ((Number) value).shortValue();
				} else if (paramClass.equals(Byte.class) || paramClass.equals(byte.class)) {
					values[i] = ((Number) value).byteValue();
				} else if (paramClass.equals(Long.class) || paramClass.equals(long.class)) {
					values[i] = ((Number) value).longValue();
				} else if (paramClass.equals(Float.class) || paramClass.equals(float.class)) {
					values[i] = ((Number) value).floatValue();
				} else if (paramClass.equals(Double.class) || paramClass.equals(double.class)) {
					values[i] = ((Number) value).doubleValue();
				}
			}
		}
	}

	private boolean isMatch(String name, Executable executable, Object[] values) {
		if (executable.getParameterCount() != values.length) {
			return false;
		}
		if (!name.equals(executable.getName())) {
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
		// now clazz is primitive (due to "if" line above),
		// void and char are the only primitives which are not a number.
		// void can't be a parameter or given value, so we just have to exclude char:
		return !clazz.equals(char.class);
	}

	private int bestMatch(Executable[] executables, int to, Object[] params) {
		int bestValue = 0;
		int bestIndex = 0;
		for (int i = 0; i < to; i++) {
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
		pType = primitive2wrapperClass(pType);
		vType = primitive2wrapperClass(vType);
		if (pType.equals(vType)) {
			return 80;
		}
		if (isIntegralNumber(pType) && isIntegralNumber(vType)) {
			return 60;
		}
		if (isFractionalNumber(pType) && isFractionalNumber(vType)) {
			return 60;
		}

		return 0;
	}

	private Class<?> primitive2wrapperClass(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		Class<?> match = primitive2Wrapper.get(clazz);
		if (match == null) {
			return clazz;
		} else {
			return match;
		}
	}

	private boolean isIntegralNumber(Class<?> clazz) {
		return Byte.class.equals(clazz) || Short.class.equals(clazz) //
				|| Integer.class.equals(clazz) || Long.class.equals(clazz);
	}

	private boolean isFractionalNumber(Class<?> clazz) {
		return Double.class.equals(clazz) || Float.class.equals(clazz);
	}

	private Object[] evaluatedParams(FplValue[] params) throws EvaluationException {
		Object[] javaParams = new Object[params.length - 1];
		for (int i = 0; i < javaParams.length; i++) {
			javaParams[i] = params[i + 1];
		}
		unwrap(javaParams);
		return javaParams;
	}

	private void unwrap(Object[] params) {
		for (int i = 0; i < params.length; i++) {
			params[i] = UnWrapper.unwrap((FplValue) params[i]);
		}
	}

}
