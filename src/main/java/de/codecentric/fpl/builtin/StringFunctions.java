package de.codecentric.fpl.builtin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

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

		scope.define(new AbstractFunction("char-at", comment("Return the code (integer) of the character at position index."),
				false, "string", "index") {
			
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToString(scope, parameters[0]).charAt((int)evaluateToLong(scope, parameters[1])));
			}
		});
		
		scope.define(new AbstractFunction("from-chars", comment("Build a string from a list of characters (integers)."),
				false, "list-of-chars") {
			
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[0]);
				char[] chars = new char[list.size()];
				int i = 0;
				for (FplValue value : list) {
					if (value instanceof FplInteger) {
						chars[i++] = (char)((FplInteger)value).getValue();
					} else {
						throw new EvaluationException("Not an integer at list pos " + i + ": " + value);
					}
				}
				return new FplString(new String(chars));
			}
		});
		
		scope.define(new AbstractFunction("index-of", comment("Determine the first index of pattern in a string. Return -1 for not found."),
				false, "string", "pattern") {
			
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToString(scope, parameters[0]).indexOf(evaluateToString(scope, parameters[1])));
			}
		});
		
		scope.define(new AbstractFunction("last-index-of", comment("Determine the last index of pattern in a string. Return -1 for not found."),
				false, "string", "pattern") {
			
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return FplInteger.valueOf(evaluateToString(scope, parameters[0]).lastIndexOf(evaluateToString(scope, parameters[1])));
			}
		});
		
		scope.define(new AbstractFunction("substring", comment("Returns a substring starting at begin-index (including) and ending at end-index (excluding)."),
				false, "string", "begin-index",  "end-index") {
			
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				int beginIndex = (int)evaluateToLong(scope, parameters[1]);
				int endIndex = (int)evaluateToLong(scope, parameters[2]);
				return new FplString(evaluateToString(scope, parameters[0]).substring(beginIndex, endIndex));
			}
		});
		
		scope.define(new AbstractFunction("match", comment("Matches a string against a regular expression. Returns a list where the first element "
				+"contains the position of the match, followed by the matches. The second entry in the list" + 
				"is the complete match, followed by the partial matches (marked by parentheses in the pattern). Empty list" + 
				"if no match found."),
				false, "string", "regex") {
			
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Pattern pattern = Pattern.compile(evaluateToString(scope, parameters[1]));
				Matcher m = pattern.matcher(evaluateToString(scope, parameters[0]));
				if (m.find()) {
					// 2+: index of match, group 0 (match), which is not counted by groupCount()
					FplValue[] values = new FplValue[2 + m.groupCount()];
					values[0] = FplInteger.valueOf(m.start());
					for (int i = 1; i < values.length; i++) {
						values[i] = new FplString(m.group(i - 1));
					}
					return FplList.fromValues(values);
				} else {
					return FplList.EMPTY_LIST;
				}
			}
		});
		
		scope.define(new AbstractFunction("replace-all", comment(
				"Replaces each substring of this string that matches the given regex with the given replacement. "
						+ "(See String.replaceAll for more details.)"),
				false, "string", "regex", "replacement") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String str = evaluateToString(scope, parameters[0]);
				String regex = evaluateToString(scope, parameters[1]);
				String replacement = evaluateToString(scope, parameters[2]);
				return new FplString(str.replaceAll(regex, replacement));
			}
		});
		
		scope.define(
				new AbstractFunction("to-lower-case", comment("Convert the string to lower case."), false, "string") {

					@Override
					public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
						return new FplString(evaluateToString(scope, parameters[0]).toLowerCase());
					}
				});

		scope.define(
				new AbstractFunction("to-upper-case", comment("Convert the string to upper case."), false, "string") {

					@Override
					public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
						return new FplString(evaluateToString(scope, parameters[0]).toUpperCase());
					}
				});
	}
}
