package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.ExceptionWrapper.wrapException;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDictionary;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplMapDictionary;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

public class StringFunctions implements ScopePopulator {
	private static final String nl = System.lineSeparator();

	@Override
	public void populate(FplEngine engine) throws ScopeException, EvaluationException {
		Scope scope = engine.getScope();

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
						Locale.of(evaluateToString(scope, parameters[1])));
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
						Locale.of(evaluateToString(scope, parameters[1])));
				NumberFormat format = new DecimalFormat(evaluateToString(scope, parameters[0]), symbols);
				String string = evaluateToString(scope, parameters[2]);
				return wrapException(() -> {
					Number number = format.parse(string);
					if (number instanceof Double) {
						return new FplDouble(number.doubleValue());
					} else {
						return FplInteger.valueOf(number.longValue());
					}
				});
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

		scope.define(
				new AbstractFunction("from-char", "Build a string from one characters (UTF integer).", "char-as-int") {

					@Override
					public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						long character = evaluateToLong(scope, parameters[0]);
						return new FplString(String.valueOf((char) character));
					}
				});

		scope.define(new AbstractFunction("to-chars", "Build a list of UTF codes from a string.", "string") {

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

		scope.define(new AbstractFunction("split",
				"Split string by regular expression, limit number of results if limit is positive. "
						+ "0 will return all, but omit trailing empty string. -1 will return all.",
				"input-string", "regex", "limit") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String input = evaluateToString(scope, parameters[0]);
				String regex = evaluateToString(scope, parameters[1]);
				int limit = (int) evaluateToLong(scope, parameters[2]);
				String[] splitted = input.split(regex, limit);
				return FplList.fromIterator(new Iterator<FplValue>() {
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

				return wrapException(() -> {
					StringWriter strWriter = new StringWriter();
					JsonGenerator generator = new JsonFactory().createGenerator(strWriter);
					serialize(generator, value);
					generator.flush();
					return new FplString(strWriter.toString());
				});
			}

			private void serialize(JsonGenerator generator, FplValue value) throws EvaluationException, IOException {
				if (value == null) {
					generator.writeNull();
				} else if (value instanceof FplList) {
					serialiazeList(generator, (FplList) value);
				} else if (value instanceof FplDictionary) {
					serializeDictionary(generator, (FplDictionary) value);
				} else if (value instanceof FplDouble) {
					serializeDouble(generator, (FplDouble) value);
				} else if (value instanceof FplInteger) {
					serializeInteger(generator, (FplInteger) value);
				} else if (value instanceof FplString) {
					serializeString(generator, (FplString) value);
				} else if (value instanceof Symbol) {
					serializeSymbol(generator, (Symbol) value);
				} else {
					throw new EvaluationException("Can't serialize " + value.typeName() + " to json");
				}
			}

			private void serialiazeList(JsonGenerator generator, FplList list) throws EvaluationException, IOException {
				generator.writeStartArray();
				for (FplValue value : list) {
					serialize(generator, value);
				}
				generator.writeEndArray();

			}

			private void serializeDictionary(JsonGenerator generator, FplDictionary dict)
					throws EvaluationException, IOException {
				generator.writeStartObject();
				for (Entry<FplValue, FplValue> entry : dict) {
					FplValue key = entry.getKey();
					String keyAsString = (key instanceof FplString) ? ((FplString) key).getContent() : key.toString();
					generator.writeFieldName(keyAsString);
					serialize(generator, entry.getValue());
				}
				generator.writeEndObject();
			}

			private void serializeDouble(JsonGenerator generator, FplDouble d) throws EvaluationException, IOException {
				generator.writeNumber(d.getValue());
			}

			private void serializeInteger(JsonGenerator generator, FplInteger i)
					throws EvaluationException, IOException {
				generator.writeNumber(i.getValue());
			}

			private void serializeString(JsonGenerator generator, FplString str)
					throws EvaluationException, IOException {
				generator.writeString(str.getContent());
			}

			private void serializeSymbol(JsonGenerator generator, Symbol symbol)
					throws EvaluationException, IOException {
				String name = symbol.getName();
				if (name.equalsIgnoreCase("true")) {
					generator.writeBoolean(true);
				} else if (name.equalsIgnoreCase("false")) {
					generator.writeBoolean(false);
				} else {
					generator.writeString(name);
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
				return wrapException(() -> {
					try (JsonParser parser = new JsonFactory().createParser(str)) {
						parser.nextToken();
						return parse(parser);
					}
				});
			}

			private FplValue parse(JsonParser parser) throws IOException {
				FplValue result = null;
				JsonToken token = parser.currentToken();

				if (token == JsonToken.START_ARRAY) {
					result = parseList(parser);
				} else if (token == JsonToken.START_OBJECT) {
					result = parseObject(parser);
				} else if (token == JsonToken.VALUE_NUMBER_INT) {
					result = FplInteger.valueOf(parser.getValueAsLong());
				} else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
					result = new FplDouble(parser.getValueAsDouble());
				} else if (token == JsonToken.VALUE_STRING) {
					result = FplString.make(parser.getText());
				} else if (token == JsonToken.VALUE_TRUE) {
					result = FplInteger.valueOf(1);
				} else if (token == JsonToken.VALUE_FALSE) {
					result = FplInteger.valueOf(0);
				} else { // JsonToken.VALUE_NULL
					result = null;
				}
				parser.nextToken();
				return result;
			}

			private FplList parseList(JsonParser parser) throws IOException {
				parser.nextToken(); // skip START_ARRAY
				FplList list = FplList.fromIterator(new Iterator<FplValue>() {

					@Override
					public boolean hasNext() {
						JsonToken token = parser.currentToken();
						return token != JsonToken.END_ARRAY;
					}

					@Override
					public FplValue next() {
						return wrapException(() -> { return parse(parser); });
					}
				});
				return list;
			}
			
			private FplMapDictionary parseObject(JsonParser parser) throws IOException {
				parser.nextToken(); // skip START_OBJECT
				FplMapDictionary obj = new FplMapDictionary();
				JsonToken token = parser.currentToken();
				while (token != JsonToken.END_OBJECT) {
					String key = parser.getText();
					parser.nextToken();
					FplValue value = parse(parser);
					obj.define(FplString.make(key), value);
					token = parser.currentToken();
				}
				return obj;
			}
		});
	}
}
