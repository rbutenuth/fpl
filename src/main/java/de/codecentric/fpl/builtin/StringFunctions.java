package de.codecentric.fpl.builtin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

public class StringFunctions implements ScopePopulator {
	private static final String nl = System.lineSeparator();

	@Override
	public void populate(Scope scope) throws ScopeException {
		scope.define(new AbstractFunction("describe", comment("Create a description in markdown format for a function"),
				false, "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue value = parameters[0].evaluate(scope);
				if (value instanceof AbstractFunction) {
					StringBuilder sb = new StringBuilder();
					AbstractFunction f = (AbstractFunction) value;
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

		scope.define(new AbstractFunction("join", comment("join strings"), true, "string...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				StringBuilder result = new StringBuilder();
				for (FplValue value : parameters) {
					result.append(evaluateToString(scope, value));
				}
				return new FplString(result.toString());
			}
		});

		scope.define(new AbstractFunction("format-number", comment(
				"Format a number to string format. The format is a Java DecimalFormat string. The locale a two letter locale."),
				false, "format", "locale", "number") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				DecimalFormatSymbols symbols = new DecimalFormatSymbols(
						new Locale(evaluateToString(scope, parameters[1])));
				NumberFormat format = new DecimalFormat(evaluateToString(scope, parameters[0]), symbols);
				FplValue number = parameters[2].evaluate(scope);
				if (number instanceof FplDouble) {
					return new FplString(format.format(((FplDouble) number).getValue()));
				} else if (number instanceof FplInteger) {
					return new FplString(format.format(((FplInteger) number).getValue()));
				} else {
					throw new EvaluationException("Not a number: " + number);
				}
			}
		});

		scope.define(new AbstractFunction("parse-number", comment(
				"Parse a string to a number. The format is a Java NumberFormat string. The locale a two letter locale."),
				false, "format", "locale", "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				DecimalFormatSymbols symbols = new DecimalFormatSymbols(
						new Locale(evaluateToString(scope, parameters[1])));
				NumberFormat format = new DecimalFormat(evaluateToString(scope, parameters[0]), symbols);
				String string = evaluateToString(scope, parameters[2]);
				try {
					Number number = format.parse(string);
					if (number instanceof Double) {
						return new FplDouble(number.doubleValue());
					} else {
						return FplInteger.valueOf(number.longValue());
					}
				} catch (ParseException e) {
					throw new EvaluationException(e.getMessage());
				}
			}
		});

		scope.define(new AbstractFunction("length", comment("Determine the length (number of characters) of a string."),
				false, "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToString(scope, parameters[0]).length());
			}
		});

	}
}
