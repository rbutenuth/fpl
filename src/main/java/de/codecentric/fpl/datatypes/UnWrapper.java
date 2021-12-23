package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.list.FplList;

public class UnWrapper {

	public static FplValue wrap(Object obj) throws EvaluationException {
		FplValue value;
		if (obj == null) {
			value = null;
		} else if (obj instanceof FplValue) {
			value = (FplValue) obj;
		} else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long) {
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
			value = wrap((List<?>) obj);
		} else if (obj instanceof Object[]) {
			value = wrap((Object[]) obj);
		} else if (obj instanceof Map && (areAllKeysStrings((Map<?, ?>) obj))) {
			value = wrap((Map<?, ?>) obj);
		} else {
			value = new FplWrapper(obj);
		}
		return value;
	}

	private static FplList wrap(List<?> list) {
		Iterator<?> iter = list.iterator();
		return FplList.fromIterator(new Iterator<FplValue>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public FplValue next() {
				return wrap(iter.next());
			}
		}, list.size());
	}

	private static FplList wrap(Object[] a) {
		return FplList.fromIterator(new Iterator<FplValue>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < a.length;
			}

			@Override
			public FplValue next() {
				return wrap(a[i++]);
			}
		}, a.length);
	}

	private static FplObject wrap(Map<?, ?> map) {
		FplObject object = new FplObject("dict");
		for (Entry<?, ?> entry : map.entrySet()) {
			object.put((String) entry.getKey(), wrap(entry.getValue()));
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
			u = unwrap((FplList) p);
		} else if (p instanceof FplObject) {
			u = unwrap((FplObject) p);
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
