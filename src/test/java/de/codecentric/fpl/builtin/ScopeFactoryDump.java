package de.codecentric.fpl.builtin;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;

public class ScopeFactoryDump {

	/**
	 * @param args Unused
	 * @throws EvaluationException Should not happen
	 * @throws ScopeException      Should not happen
	 */
	public static void main(String[] args) throws EvaluationException, ScopeException {
		Scope scope = new FplEngine().getScope();
		SortedMap<String, FplValue> map = new TreeMap<>();
		for (Entry<String, FplValue> scopeEntry : scope) {
			map.put(scopeEntry.getKey(), scopeEntry.getValue());
		}
		for (Entry<String, FplValue> entry : map.entrySet()) {
			FplValue value = entry.getValue();
			if (value instanceof AbstractFunction) {
				AbstractFunction f = (AbstractFunction) value;
				System.out.println("### " + f.getName());
				List<String> comments = f.getComment();
				if (!comments.isEmpty()) {
					for (String c : comments) {
						System.out.println(c);
					}
				}
				System.out.print("```");
				System.out.print("(" + f.getName());
				String[] pns = f.getParameterNames();
				for (String pn : pns) {
					System.out.print(" " + pn);
				}
				System.out.println(")");
				System.out.println("```");
				System.out.println();
			}
		}
	}

}
