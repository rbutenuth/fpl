package de.codecentric.fpl.datatypes;

import java.util.Iterator;
import java.util.Map.Entry;

import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * The FPL version of an object: Most of the semantic comes from a combination
 * of {@link Scope} with {@link Named}. The rest are some built in functions for
 * linking and executing methods on objects.
 */
public class FplMapDictionary extends MapScope implements FplDictionary, FplValue, EvaluatesToThisValue {
	protected static final String NL = System.lineSeparator();

	public FplMapDictionary(String name) {
		super(name);
	}

	@Override
	public String typeName() {
		return "dictionary";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<String, FplValue> entry : map.entrySet()) {
			sb.append(NL).append("    ");
			sb.append(entry.getKey()).append(": ");
			FplValue v = entry.getValue();
			// Be careful: Cycles in data structures can lead to endless recursion...
			if (v instanceof FplList) {
				sb.append("<list>");
			} else if (v instanceof FplDictionary) {
				sb.append("<dictionary>");
			} else {
				sb.append(v.toString());
			}
		}
		sb.append(NL).append("}").append(NL);

		return sb.toString();
	}

	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public synchronized String peekFirstKey() throws ScopeException {
		Iterator<String> iterator = map.keySet().iterator();
		checkHasNext(iterator);
		return iterator.next();
	}

	@Override
	public String peekLastKey() throws ScopeException {
		return peekFirstKey();
	}

	@Override
	public synchronized String fetchFirstKey() throws ScopeException {
		Iterator<String> iterator = map.keySet().iterator();
		checkHasNext(iterator);
		String result = iterator.next();
		iterator.remove();
		return result;
	}

	@Override
	public String fetchLastKey() throws ScopeException {
		return fetchFirstKey();
	}

	@Override
	public synchronized FplValue fetchFirstValue() throws ScopeException {
		Iterator<FplValue> iterator = map.values().iterator();
		checkHasNext(iterator);
		FplValue result = iterator.next();
		iterator.remove();
		return result;
	}

	@Override
	public FplValue fetchLastValue() throws ScopeException {
		return fetchFirstValue();
	}

	@Override
	public synchronized FplList fetchFirstEntry() throws ScopeException {
		Iterator<Entry<String, FplValue>> iterator = map.entrySet().iterator();
		checkHasNext(iterator);
		Entry<String, FplValue> result = iterator.next();
		iterator.remove();
		return FplList.fromValues(new FplString(result.getKey()), result.getValue());
	}

	@Override
	public FplList fetchLastEntry() throws ScopeException {
		return fetchFirstEntry();
	}
	
	private void checkHasNext(Iterator<?> iterator) throws ScopeException {
		if (!iterator.hasNext()) {
			throw new ScopeException("dictionary is empty");
		}
	}
}
