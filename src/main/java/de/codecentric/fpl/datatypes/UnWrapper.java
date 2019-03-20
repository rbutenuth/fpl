package de.codecentric.fpl.datatypes;

public class UnWrapper {

	// TODO: FplObject <-> HashMap, FplList <-> List
	
	public static FplValue wrapResult(Object result) {
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

	public static void unwrap(Object[] params) {
		for (int i = 0; i < params.length; i++) {
			params[i] = unwrap((FplValue) params[i]);
		}
	}

	public static Object unwrap(FplValue p) {
		Object u;
		if (p instanceof FplWrapper) {
			u = ((FplWrapper) p).getInstance();
		} else if (p instanceof FplString) {
			u = ((FplString) p).getContent();
		} else if (p instanceof FplDouble) {
			u = Double.valueOf(((FplDouble) p).getValue());
		} else if (p instanceof FplInteger) {
			u = Long.valueOf(((FplInteger) p).getValue());
		} else {
			u = p;
		}
		return u;
	}
}
