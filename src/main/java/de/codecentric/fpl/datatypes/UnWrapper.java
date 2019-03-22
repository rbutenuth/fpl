package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.list.FplList;

public class UnWrapper {

	public static FplValue wrap(Object obj) throws EvaluationException {
		FplValue value;
		if (obj == null) {
			value = null;
		} else if (obj instanceof FplValue) {
			value = (FplValue) obj;
		} else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer
				|| obj instanceof Long) {
			value = FplInteger.valueOf(((Number) obj).longValue());
		} else if (obj instanceof Float || obj instanceof Double) {
			value = new FplDouble(((Number) obj).doubleValue());
		} else if (obj instanceof Character) {
			value = new FplString("" + obj);
		} else if (obj instanceof String) {
			value = new FplString((String) obj);
		} else if (obj instanceof Boolean) {
			boolean b = ((Boolean) obj).booleanValue();
			value = b ? FplInteger.valueOf(1) : null;
		} else if (obj instanceof List) {
			value = wrap((List<?>)obj); 
		} else if (obj instanceof Object[]) {
			value = wrap((Object[])obj); 
		} else if (obj instanceof Map && (areAllKeysStrings((Map<?, ?>)obj))) {
			value = wrap((Map<?, ?>)obj); 
		} else {
			value = new FplWrapper(obj);
		}
		return value;
	}

	private static FplList wrap(List<?> list) throws EvaluationException {
		FplValue[] values = new FplValue[list.size()];
		int i = 0;
		for (Object o : list) {
			values[i++] = wrap(o);
		}
		return new FplList(values);
	}
	
	private static FplList wrap(Object[] a) throws EvaluationException {
		FplValue[] values = new FplValue[a.length];
		for (int i = 0; i < a.length; i++) {
			values[i] = wrap(a[i]);
		}
		return new FplList(values);
	}
	
	private static FplObject wrap(Map<?, ?> map) throws EvaluationException {
		FplObject object = new FplObject();
		try {
			for (Entry<?, ?> entry : map.entrySet()) {
				object.put((String)entry.getKey(), wrap(entry.getValue()));
			}
		} catch (ScopeException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
		return object;
	}
	
	private static boolean areAllKeysStrings(Map<?, ?> map) {
		for (Object key : map.keySet()) {
			if (!(key instanceof String)) {
				return false;
			}
		}
		return true;
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
		} else if (p instanceof FplList) {
			u = unwrap((FplList)p);
		} else if (p instanceof FplObject) {
			u = unwrap((FplObject)p);
		} else {
			u = p;
		}
		return u;
	}

	private static Object unwrap(FplList p) {
		List<Object> result = new ArrayList<>(p.size());
		for (FplValue f : p) {
			result.add(unwrap(f));
		}
		return result;
	}

	private static Object unwrap(FplObject p) {
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, FplValue> entry : p) {
			result.put(entry.getKey(), unwrap(entry.getValue()));
		}
		return result;
	}
}
