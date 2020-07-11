package de.codecentric.fpl.builtin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.io.BomAwareReader;
import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;

public class InputOutput implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("parse-resource", //
				comment("Read or evaluate all expressions within the resource given by the URI. Return a list which contains the results. "
						+ "The resource must be UTF-8 encoded."),
				false, "uri", "evaluate") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String uriAsString = evaluateToString(scope, parameters[0]);
				boolean evaluate = evaluateToBoolean(scope, parameters[1]);
				try {
					URI uri = new URI(uriAsString);
					try (InputStream is = uri.toURL().openStream();
							Parser p = new Parser(
									new Scanner(uriAsString, new BomAwareReader(is)))) {
						List<FplValue> values = new ArrayList<>();
						while (p.hasNext()) {
							FplValue value = p.next();
							if (evaluate) {
								value = value.evaluate(scope);
							}
							values.add(value);
						}
						return FplList.fromValues(values);
					}
				} catch (IOException | URISyntaxException e) {
					EvaluationException ee = new EvaluationException(e.getMessage(), e);
					ee.add(new StackTraceElement("InputOutput", "parse-resource", uriAsString, 0));
					throw ee;
				} catch (ParseException e) {
					EvaluationException ee = new EvaluationException(e.getMessage(), e);
					Position pos = e.getPosition();
					ee.add(new StackTraceElement("InputOutput", "parse-resource", uriAsString, pos.getLine()));
					throw ee;
				}
			}
		});

		scope.define(new AbstractFunction("parse-string", //
				comment("Parse or evaluate all expressions within the string. Return a list which contains the results."),
				false, "string", "evaluate") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String str = evaluateToString(scope, parameters[0]);
				boolean evaluate = evaluateToBoolean(scope, parameters[1]);
				try (Parser p = new Parser(new Scanner(str, new StringReader(str)))) {
						List<FplValue> values = new ArrayList<>();
						while (p.hasNext()) {
							FplValue value = p.next();
							if (evaluate) {
								value = value.evaluate(scope);
							}
							values.add(value);
						}
						return FplList.fromValues(values);
				} catch (ParseException | IOException e) {
					EvaluationException ee = new EvaluationException(e.getMessage(), e);
					// the cast does not have to be protected, the IOException will never be thrown when reading from a String
					Position pos = ((ParseException)e).getPosition();
					ee.add(new StackTraceElement("InputOutput", "parse-string", str, pos.getLine()));
					throw ee;
				}
			}
		});

		scope.define(new AbstractFunction("to-string", //
				comment("Write the content of a string to a file. Use UTF-8 as encoding."), false, "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (parameters[0] == null) {
					return new FplString("nil");
				} else {
					FplValue value = parameters[0].evaluate(scope);
					return new FplString(value == null ? "nil" : value.toString());
				}
			}
		});
		
		scope.define(new AbstractFunction("write-string-to-file", //
				comment("Write the content of a string to a file. Use UTF-8 as encoding."), false, "filename", "content") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				String filename = evaluateToString(scope, parameters[0]);
				String content = evaluateToString(scope, parameters[1]);
				try (FileOutputStream fos = new FileOutputStream(filename);
						OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
					writer.write(content);
					return new FplString(content);
				} catch (IOException e) {
					throw new EvaluationException(e);
				}
			}
		});
	}

}
