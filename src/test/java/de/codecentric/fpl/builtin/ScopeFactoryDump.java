package de.codecentric.fpl.builtin;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;

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
				String comment = f.getComment();
				if (!comment.isEmpty()) {
					System.out.println(comment);
				}
				System.out.println("```");
				System.out.print("(" + f.getName());
				for (String pn : f.getParameterNames()) {
					System.out.print(" " + pn);
				}
				System.out.println(")");
				System.out.println("```");
				System.out.println();
			}
		}
	}

}
