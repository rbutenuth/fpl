package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.Function.comment;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

public class StringFunctions {
	private static final String nl = System.lineSeparator();

	public static void put(Scope scope) throws ScopeException {
		scope.put(new Function("describe", comment("Create a comment in markdown format for a function"), false,
				"expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue value = parameters[0].evaluate(scope);
				if (value instanceof Function) {
					StringBuilder sb = new StringBuilder();
					Function f = (Function) value;
					sb.append("Function ").append(f.getName()).append(nl);
					for (String line : f.getComment()) {
						sb.append(line).append(nl);
					}
					String[] params = f.getParameterNames();
					for (int i = 0; i < params.length; i++) {
						sb.append("* ").append(params[i]);
						String c = f.getParameterComment(i);
						if (c.trim().length() > 0) {
							sb.append(" ").append(c);
						}
						sb.append(nl);
					}
					return new FplString(sb.toString());
				} else {
					return new FplString("There is no documentation for " + value);
				}
			}
		});
	}
}
