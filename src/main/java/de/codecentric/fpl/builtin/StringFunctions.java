package de.codecentric.fpl.builtin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

public class StringFunctions implements ScopePopulator {
	private static final String nl = System.lineSeparator();

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {
		scope.define(new AbstractFunction("describe", "Create a description in markdown format for a function",
				"expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue value = evaluateToAny(scope, parameters[0]);
				if (value instanceof AbstractFunction) {
					StringBuilder sb = new StringBuilder();
					AbstractFunction f = (AbstractFunction) value;
					sb.append("Function ").append(f.getName()).append(nl);
					sb.append(f.getComment()).append(nl);
					for (Iterator<String> paramIter = f.getParameterNameToIndex().keySet().iterator(); paramIter
							.hasNext();) {
						String param = paramIter.next();
						sb.append("* ").append(param);
						if (!paramIter.hasNext() && f.isVararg()) {
							sb.append("...");
						}
						String c = f.getParameterComment(param);
						if (c != null && c.trim().length() > 0) {
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

		scope.define(new AbstractFunction("join", "join strings", "string...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				StringBuilder result = new StringBuilder();
				for (FplValue value : parameters) {
					result.append(evaluateToString(scope, value));
				}
				return new FplString(result.toString());
			}
		});

		scope.define(new AbstractFunction("join-list", "join strings within a list", "values") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList values = evaluateToList(scope, parameters[0]);
				StringBuilder result = new StringBuilder();
				for (FplValue value : values) {
					result.append(evaluateToString(scope, value));
				}
				return new FplString(result.toString());
			}
		});

		scope.define(new AbstractFunction("format-number",
				"Format a number to string format. The format is a Java DecimalFormat string. The locale a two letter locale.",
				"format", "locale", "number") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				DecimalFormatSymbols symbols = new DecimalFormatSymbols(
						new Locale(evaluateToString(scope, parameters[1])));
				NumberFormat format = new DecimalFormat(evaluateToString(scope, parameters[0]), symbols);
				FplValue number = evaluateToAny(scope, parameters[2]);
				if (number instanceof FplDouble) {
					return new FplString(format.format(((FplDouble) number).getValue()));
				} else if (number instanceof FplInteger) {
					return new FplString(format.format(((FplInteger) number).getValue()));
				} else {
					throw new EvaluationException("Not a number: " + number);
				}
			}
		});

		scope.define(new AbstractFunction("parse-number",
				"Parse a string to a number. The format is a Java NumberFormat string. The locale a two letter locale.",
				"format", "locale", "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
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

		scope.define(
				new AbstractFunction("length", "Determine the length (number of characters) of a string.", "string") {

					@Override
					public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						return FplInteger.valueOf(evaluateToString(scope, parameters[0]).length());
					}
				});

		scope.define(new AbstractFunction("char-at", "Return the code (integer) of the character at position index.",
				"string", "index") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return FplInteger.valueOf(
						evaluateToString(scope, parameters[0]).charAt((int) evaluateToLong(scope, parameters[1])));
			}
		});

		scope.define(new AbstractFunction("from-chars", "Build a string from a list of characters (UTF integers).",
				"list-of-chars") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[0]);
				char[] chars = new char[list.size()];
				int i = 0;
				for (FplValue value : list) {
					if (value instanceof FplInteger) {
						chars[i++] = (char) ((FplInteger) value).getValue();
					} else {
						throw new EvaluationException("Not an integer at list pos " + i + ": " + value);
					}
				}
				return new FplString(new String(chars));
			}
		});

		scope.define(new AbstractFunction("to-chars", "Build a list of UTF codes from a string.",
				"string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String str = evaluateToString(scope, parameters[0]);
				int length = str.length();
				return FplList.fromIterator(new Iterator<FplValue>() {
					int i = 0;
					
					@Override
					public boolean hasNext() {
						return i < length;
					}

					@Override
					public FplValue next() {
						return FplInteger.valueOf(str.charAt(i++));
					}
				}, length);
			}
		});

		scope.define(new AbstractFunction("index-of",
				"Determine the first index of pattern in a string. Return -1 for not found.", "string", "pattern") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return FplInteger.valueOf(
						evaluateToString(scope, parameters[0]).indexOf(evaluateToString(scope, parameters[1])));
			}
		});

		scope.define(new AbstractFunction("last-index-of",
				"Determine the last index of pattern in a string. Return -1 for not found.", "string", "pattern") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return FplInteger.valueOf(
						evaluateToString(scope, parameters[0]).lastIndexOf(evaluateToString(scope, parameters[1])));
			}
		});

		scope.define(new AbstractFunction("substring",
				"Returns a substring starting at begin-index (including) and ending at end-index (excluding).",
				"string", "begin-index", "end-index") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				int beginIndex = (int) evaluateToLong(scope, parameters[1]);
				int endIndex = (int) evaluateToLong(scope, parameters[2]);
				return new FplString(evaluateToString(scope, parameters[0]).substring(beginIndex, endIndex));
			}
		});

		scope.define(new AbstractFunction("match",
				"Matches a string against a regular expression. Returns a list where the first element "
						+ "contains the position of the match, followed by the matches. The second entry in the list"
						+ "is the complete match, followed by the partial matches (marked by parentheses in the pattern). Empty list"
						+ "if no match found.",
				"string", "regex") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Pattern pattern = Pattern.compile(evaluateToString(scope, parameters[1]));
				Matcher m = pattern.matcher(evaluateToString(scope, parameters[0]));
				if (m.find()) {
					// 2+: index of match, group 0 (match), which is not counted by groupCount()
					int count = 2 + m.groupCount();
					return FplList.fromIterator(new Iterator<FplValue>() {
						int i = 0;

						@Override
						public boolean hasNext() {
							return i < count;
						}

						@Override
						public FplValue next() {
							if (i == 0) {
								i++;
								return FplInteger.valueOf(m.start());
							} else {
								String group = m.group(i++ - 1);
								return new FplString(group == null ? "" : group);
							}
						}
					}, count);
				} else {
					return FplList.EMPTY_LIST;
				}
			}
		});

		scope.define(new AbstractFunction("replace-all",
				"Replaces each substring of this string that matches the given regex with the given replacement. "
						+ "(See String.replaceAll for more details.)",
				"string", "regex", "replacement") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String str = evaluateToString(scope, parameters[0]);
				String regex = evaluateToString(scope, parameters[1]);
				String replacement = evaluateToString(scope, parameters[2]);
				return new FplString(str.replaceAll(regex, replacement));
			}
		});

		scope.define(new AbstractFunction("to-lower-case", "Convert the string to lower case.", "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return new FplString(evaluateToString(scope, parameters[0]).toLowerCase());
			}
		});

		scope.define(new AbstractFunction("to-upper-case", "Convert the string to upper case.", "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return new FplString(evaluateToString(scope, parameters[0]).toUpperCase());
			}
		});

		scope.define(new AbstractFunction("trim", "Remove white space at begin and end.", "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return new FplString(evaluateToString(scope, parameters[0]).trim());
			}
		});

		scope.define(new AbstractFunction("split", "Split string by regular expression, limit number of results if limit is positive. " 
				+ "0 will return all, but omit trailing empty string. -1 will return all.", "input-string", "regex", "limit") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String input = evaluateToString(scope, parameters[0]);
				String regex = evaluateToString(scope, parameters[1]);
				int limit = (int)evaluateToLong(scope, parameters[2]);
				String[] splitted = input.split(regex, limit);
				return FplList. fromIterator(new Iterator<FplValue>() {
					int i = 0;
					
					@Override
					public boolean hasNext() {
						return i < splitted.length;
					}

					@Override
					public FplValue next() {
						return new FplString(splitted[i++]);
					}
				}, splitted.length);
			}
		});

		scope.define(new AbstractFunction("symbol", "Create a symbol.", "string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return new Symbol(evaluateToString(scope, parameters[0]));
			}
		});

		scope.define(new AbstractFunction("name-of-symbol", "Determine the name of a symbol.", "symbol") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return new FplString(((Symbol) evaluateToAny(scope, parameters[0])).getName());
			}
		});

		scope.define(new AbstractFunction("serialize-to-json", "Convert value to JSON string.", "value") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				FplValue value = evaluateToAny(scope, parameters[0]);
				StringBuilder sb = new StringBuilder();
				serialize(sb, value);
				return new FplString(sb.toString());
			}

			private void serialize(StringBuilder sb, FplValue value) throws EvaluationException {
				if (value == null) {
					sb.append("null");
				} else if (value instanceof FplList) {
					serialiazeList(sb, (FplList) value);
				} else if (value instanceof FplObject) {
					serializeObject(sb, (FplObject) value);
				} else if (value instanceof FplDouble) {
					serializeDouble(sb, (FplDouble) value);
				} else if (value instanceof FplInteger) {
					serializeInteger(sb, (FplInteger) value);
				} else if (value instanceof FplString) {
					serializeString(sb, (FplString) value);
				} else if (value instanceof Symbol) {
					serializeSymbol(sb, (Symbol) value);
				} else {
					throw new EvaluationException("Can't serialize " + value.typeName() + " to json");
				}
			}

			private void serialiazeList(StringBuilder sb, FplList list) throws EvaluationException {
				sb.append("[");
				boolean first = true;
				for (FplValue value : list) {
					if (!first) {
						sb.append(",");
					}
					first = false;
					serialize(sb, value);
				}
				sb.append("]");
			}

			private void serializeObject(StringBuilder sb, FplObject object) throws EvaluationException {
				boolean first = true;
				sb.append("{");
				for (Entry<String, FplValue> entry : object) {
					if (!first) {
						sb.append(",");
					}
					sb.append(JsonStream.serialize(entry.getKey()));
					sb.append(":");
					serialize(sb, entry.getValue());
					first = false;
				}
				sb.append("}");
			}

			private void serializeDouble(StringBuilder sb, FplDouble d) throws EvaluationException {
				sb.append(JsonStream.serialize(d.getValue()));
			}

			private void serializeInteger(StringBuilder sb, FplInteger i) throws EvaluationException {
				sb.append(JsonStream.serialize(i.getValue()));
			}

			private void serializeString(StringBuilder sb, FplString str) throws EvaluationException {
				sb.append(JsonStream.serialize(str.getContent()));
			}

			private void serializeSymbol(StringBuilder sb, Symbol symbol) throws EvaluationException {
				String name = symbol.getName();
				if (name.equalsIgnoreCase("true")) {
					sb.append("true");
				} else if (name.equalsIgnoreCase("false")) {
					sb.append("false");
				} else {
					sb.append(JsonStream.serialize(name));
				}
			}
		});

		scope.define(new AbstractFunction("parse-json",
				"Convert a JSON string to list/object. In case the JSON contain a key \"nil\","
						+ " it is converted to \"<nil>\", as \"nil\" is not a valid symbol in FPL.",
				"string") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String str = evaluateToString(scope, parameters[0]);
				Any parsed = JsonIterator.deserialize(str);
				try {
					return deserialize(parsed);
				} catch (TunnelException e) {
					throw new EvaluationException(e.getMessage(), e);
				}
			}

			private FplValue deserialize(Any any) {
				ValueType t = any.valueType();
				if (t == ValueType.ARRAY) {
					return deserializeList(any.asList());
				} else if (t == ValueType.OBJECT) {
					return deserializeMap(any.asMap());
				} else if (t == ValueType.STRING) {
					return new FplString(any.toString());
				} else if (t == ValueType.BOOLEAN) {
					return any.toBoolean() ? FplInteger.valueOf(1) : FplInteger.valueOf(0);
				} else if (t == ValueType.NUMBER) {
					return deserializeNumber(any);
				} else { // ValueType.NULL or INVALID
					return null;
				}
			}

			private FplValue deserializeList(List<Any> list) {
				int size = list.size();
				return FplList.fromIterator(new Iterator<FplValue>() {
					int i = 0;

					@Override
					public boolean hasNext() {
						return i < size;
					}

					@Override
					public FplValue next() {
						return deserialize(list.get(i++));
					}
				}, size);
			}

			private FplValue deserializeMap(Map<String, Any> map) {
				FplObject obj = new FplObject("dict");
				for (Map.Entry<String, Any> entry : map.entrySet()) {
					try {
						obj.put(entry.getKey(), deserialize(entry.getValue()));
					} catch (ScopeException e) {
						throw new TunnelException(new EvaluationException(e.getMessage(), e));
					}
				}
				return obj;
			}

			private FplValue deserializeNumber(Any any) {
				String s = any.toString();
				if (s.indexOf('.') >= 0) {
					return new FplDouble(any.toDouble());
				} else {
					return FplInteger.valueOf(any.toLong());
				}
			}
		});
	}
}
