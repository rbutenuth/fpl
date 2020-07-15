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
import java.util.Map.Entry;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.io.BomAwareReader;
import de.codecentric.fpl.io.HttpClient;
import de.codecentric.fpl.io.HttpRequest;
import de.codecentric.fpl.io.HttpResponse;
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
							Parser p = new Parser(new Scanner(uriAsString, new BomAwareReader(is)))) {
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
					// the cast does not have to be protected, the IOException will never be thrown
					// when reading from a String
					Position pos = ((ParseException) e).getPosition();
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
				comment("Write the content of a string to a file. Use UTF-8 as encoding."), false, "filename",
				"content") {
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
		scope.define(new AbstractFunction("http-request", //
				comment("Do an HTTP-request."), false, "url", "method", "headers", "query-params", "body", "user",
				"password") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				try {
					HttpRequest req = new HttpRequest();
					req.setBaseUri(evaluateToString(scope, parameters[0]));
					req.setMethod(evaluateToString(scope, parameters[1]));
					setHeaders(req, evaluateToDictionaryNullDefaultsToEmpty(scope, parameters[2]));
					setParams(req, evaluateToDictionaryNullDefaultsToEmpty(scope, parameters[3]));
					FplValue body = parameters[4] == null ? null : parameters[4].evaluate(scope);
					if (body != null) {
						if (body instanceof FplString) {
							req.setBody(((FplString)body).getContent(), "UTF-8");
						} else {
							req.setBody(body.toString(), "UTF-8");
						}
					}
					req.setBasicAuth(evaluateToString(scope, parameters[5]), evaluateToString(scope, parameters[6]));
					HttpResponse res = new HttpClient().execute(req);
					FplValue[] values = new FplValue[3];
					values[0] = FplInteger.valueOf(res.getStatusCode());
					values[1] = responseHeaders(res);
					values[2] = res.hasBody() ? new FplString(res.getBodyAsString("UTF-8")) : null;
					return FplList.fromValues(values);
				} catch (IOException | ScopeException e) {
					throw new EvaluationException(e.getMessage(), e);
				}
			}


			private void setHeaders(HttpRequest req, FplObject dict) {
				for (Entry<String, FplValue> entry : dict) {
					FplValue value = entry.getValue();
					if (value instanceof FplList) {
						for (FplValue v : (FplList)value) {
							req.addHeader(entry.getKey(), valueToString(v));
						}
					} else {
						req.addHeader(entry.getKey(), valueToString(value));
					}
				}
			}

			private void setParams(HttpRequest req, FplObject dict) {
				for (Entry<String, FplValue> entry : dict) {
					FplValue value = entry.getValue();
					if (value instanceof FplList) {
						for (FplValue v : (FplList)value) {
							req.addParam(entry.getKey(), valueToString(v));
						}
					} else {
						req.addParam(entry.getKey(), valueToString(value));
					}
				}
			}
			
			private String valueToString(FplValue value) {
				if (value == null) {
					return "";
				} if (value instanceof FplString) {
					return ((FplString)value).getContent();
				} else {
					return value.toString();
				}
			}
			
			private FplObject responseHeaders(HttpResponse res) throws ScopeException {
				FplObject headers = new FplObject("dict");
				for (String name : res.getHeaderNames()) {
					if (!name.isEmpty()) {
						List<String> values = res.getHeaders(name);
						int count = values.size();
						if (count == 1) {
							headers.put(name.toLowerCase(), new FplString(values.get(0)));
						} else {
							FplValue[] array = new FplString[count];
							for (int i = 0; i < count; i++) {
								array[i] = new FplString(values.get(i));
							}
							headers.put(name.toLowerCase(), FplList.fromValues(array));
						}
					}
				}
				return headers;
			}
		});
	}

}
