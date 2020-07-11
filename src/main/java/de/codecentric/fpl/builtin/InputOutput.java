package de.codecentric.fpl.builtin;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

		scope.define(new AbstractFunction("read-resource", //
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
					ee.add(new StackTraceElement("InputOutput", "read-resource", uriAsString, 0));
					throw ee;
				} catch (ParseException e) {
					EvaluationException ee = new EvaluationException(e.getMessage(), e);
					Position pos = e.getPosition();
					ee.add(new StackTraceElement("InputOutput", "read-resource", uriAsString, pos.getLine()));
					throw ee;
				}
			}
		});

		scope.define(new AbstractFunction("write-to-file", //
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
