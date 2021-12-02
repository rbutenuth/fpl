package de.codecentric.fpl.builtin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.BasicAuthenticator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.io.BomAwareReader;
import de.codecentric.fpl.io.HttpClient;
import de.codecentric.fpl.io.HttpEntity;
import de.codecentric.fpl.io.HttpRequest;
import de.codecentric.fpl.io.HttpRequestHandler;
import de.codecentric.fpl.io.HttpResponse;
import de.codecentric.fpl.io.SimpleHttpServer;
import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;

public class InputOutput implements ScopePopulator {
	private FplEngine engine;

	public InputOutput(FplEngine engine) {
		this.engine = engine;
	}

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("parse-resource", //
				"Read or evaluate all expressions within the resource given by the URI. Return a list which contains the results. "
						+ "The resource must be UTF-8 encoded.",
				"uri", "evaluate") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				boolean evaluate = evaluateToBoolean(scope, parameters[1]);
				String uriAsString = evaluateToString(scope, parameters[0]);
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
						return FplList.fromValues(values);  // TODO: Change to fromIterator to avoid copying
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
				"Parse or evaluate all expressions within the string. Return a list which contains the results.",
				"string", "evaluate") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
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
					return FplList.fromValues(values);  // TODO: Change to fromIterator to avoid copying
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
				"Converts an expression into its string representation.", "expression") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters[0] == null) {
					return new FplString("nil");
				} else {
					FplValue value = parameters[0].evaluate(scope);
					return new FplString(value == null ? "nil" : value.toString());
				}
			}
		});

		scope.define(new AbstractFunction("write-string-to-file", //
				"Write the content of a string to a file. Use UTF-8 as encoding.", "filename", "content") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
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

		scope.define(new AbstractFunction("read-string-from-resource", //
				"Read the content of a a file or other resource as String. Use UTF-8 as encoding.", "url") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String uriAsString = evaluateToString(scope, parameters[0]);
				try {
					URI uri = new URI(uriAsString);
					try (InputStream is =uri.toURL().openStream()) { 
						return readFromInputStream(is);
					}
				} catch (IOException | URISyntaxException e) {
					throw new EvaluationException(e);
				}
			}
		});

		scope.define(new AbstractFunction("read-string-from-file", //
				"Read the content of a a file. Use UTF-8 as encoding.", "filename") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				File file = new File(evaluateToString(scope, parameters[0]));
				try {
					try (InputStream is = new FileInputStream(file)) {
						return readFromInputStream(is);
					}
				} catch (IOException e) {
					throw new EvaluationException(e);
				}
			}
		});
		
		scope.define(new AbstractFunction("http-request", //
				"Execute an HTTP-request. Two additional parameters for basic auth (user, password) are possible", //
				"url", "method", "headers", "query-params", "body...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				try {
					HttpRequest req = new HttpRequest();
					if (parameters.length == 7) {
						req.setBasicAuth(evaluateToString(scope, parameters[5]), evaluateToString(scope, parameters[6]));
					} else if (parameters.length == 6) {
						throw new EvaluationException("user set, password missing");
					} else if (parameters.length > 7) {
						throw new EvaluationException("too many parameters");
					}
					req.setBaseUri(evaluateToString(scope, parameters[0]));
					req.setMethod(evaluateToString(scope, parameters[1]));
					setHeaders(req, evaluateToDictionaryNullDefaultsToEmpty(scope, parameters[2]));
					setParams(req, evaluateToDictionaryNullDefaultsToEmpty(scope, parameters[3]));
					FplValue body = parameters[4] == null ? null : parameters[4].evaluate(scope);
					if (body != null) {
						if (body instanceof FplString) {
							req.setBody(((FplString) body).getContent(), "UTF-8");
						} else {
							req.setBody(body.toString(), "UTF-8");
						}
					}
					HttpResponse res = new HttpClient().execute(req);
					return FplList.fromValues(FplInteger.valueOf(res.getStatusCode()),
							fplHeaders(res),
							res.hasBody() ? new FplString(res.getBodyAsString("UTF-8")) : null);
				} catch (IOException | ScopeException e) {
					throw new EvaluationException(e.getMessage(), e);
				}
			}

			private void setParams(HttpRequest req, FplObject dict) {
				for (Entry<String, FplValue> entry : dict) {
					FplValue value = entry.getValue();
					if (value instanceof FplList) {
						for (FplValue v : (FplList) value) {
							req.addParam(entry.getKey(), valueToString(v));
						}
					} else {
						req.addParam(entry.getKey(), valueToString(value));
					}
				}
			}
		});

		scope.define(new AbstractFunction("http-server", //
				"Start an HTTP server. Returns a function to terminate the server, parameter is the delay in seconds.",
				"port", "authenticator", "handlers...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				int port = (int) evaluateToLong(scope, parameters[0]);
				BasicAuthenticator authenticator = createAuthenticator(scope,
						evaluateToFunctionOrNull(scope, parameters[1]));
				HttpRequestHandler handler = new Handler(createHandlers(scope, parameters));

				SimpleHttpServer server;
				try {
					server = new SimpleHttpServer(engine.getPool(), port, handler, authenticator);
				} catch (IOException | IllegalArgumentException e) {
					throw new EvaluationException(e.getMessage(), e);
				}

				return new AbstractFunction("terminate-server", "Terminate the HTTP server", "delay") {

					@Override
					protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						int delay = (int) evaluateToLong(scope, parameters[0]);
						server.terminate(delay);
						server.waitForTermination();
						return null;
					}
				};
			}

			private BasicAuthenticator createAuthenticator(Scope scope, Function authLambda) {
				if (authLambda == null) {
					return null;
				} else {
					return new BasicAuthenticator("fpl-server") {
						@Override
						public boolean checkCredentials(String username, String password) {
							FplValue[] parameters = new FplValue[2];
							parameters[0] = FplString.make(username);
							parameters[1] = FplString.make(password);
							try {
								return evaluateToBoolean(scope, authLambda.call(scope, parameters));
							} catch (EvaluationException e) {
								return false;
							}
						}
					};
				}
			}

			// start at parameter 3 (0 is port, 1 authenticator)
			private PathHandler[] createHandlers(Scope scope, FplValue... parameters) throws EvaluationException {
				PathHandler[] handlers = new PathHandler[parameters.length - 2];
				for (int i = 0; i < parameters.length - 2; i++) {
					handlers[i] = createHandler(scope, evaluateToList(scope, parameters[i + 2]));
				}
				return handlers;
			}

			// Example for entry: ("GET" "/some-path/*" some-function)
			private PathHandler createHandler(Scope scope, FplList entry) throws EvaluationException {
				String method = ((FplString) entry.get(0)).getContent();
				String path = ((FplString) entry.get(1)).getContent();
				Function function = (Function) entry.get(2);
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
				boolean wildcard = isWildcard(path);
				path = withoutWildcard(path);

				return new PathHandler(method.toUpperCase(), path, wildcard, scope, function);
			}

			private boolean isWildcard(String path) {
				return path.endsWith("*");
			}

			private String withoutWildcard(String path) {
				return isWildcard(path) ? path.substring(0, path.length() - 1) : path;
			}

			class Handler implements HttpRequestHandler {
				private PathHandler[] handlers;

				public Handler(PathHandler[] handlers) {
					this.handlers = handlers;
				}

				@Override
				public HttpResponse handleRequest(HttpRequest req) throws Exception {
					for (PathHandler h : handlers) {
						if (h.isMatch(req.getMethod(), req.getBaseUri())) {
							return h.handleRequest(req);
						}
					}
					HttpResponse res = new HttpResponse();
					res.setStatusCode(404);
					return res;
				}
			}

			class PathHandler implements HttpRequestHandler {
				private Function function;
				private Scope scope;
				private boolean wildcard;
				private String path;
				private String method;

				PathHandler(String method, String path, boolean wildcard, Scope scope, Function function) {
					this.method = method;
					this.path = path;
					this.wildcard = wildcard;
					this.scope = scope;
					this.function = function;
				}

				public boolean isMatch(String method, String path) {
					if (method.equals(this.method)) {
						if (wildcard) {
							return path.startsWith(this.path);
						} else {
							return path.equals(this.path);
						}
					} else {
						return false;
					}
				}

				@Override
				public HttpResponse handleRequest(HttpRequest req) throws EvaluationException, ScopeException, IOException {
					HttpResponse res = new HttpResponse();
					FplValue[] parameters = new FplValue[4];
					// path Complete path, including the prefix defined in `handlers`.
					parameters[0] = new FplString(req.getBaseUri());
					// headers Request headers as map (header names converted to lower case)
					parameters[1] = fplHeaders(req);
					// params Request parameters as map
					parameters[2] = fplParams(req);
					// body Request body as string. May be `nil`
					parameters[3] = req.hasBody() ? new FplString(req.getBodyAsString("UTF-8")) : null;
					// FplString(req.getBodyAsString("UTF-8")) : null;
					try {
						FplList result = (FplList) function.call(scope, parameters);
						// HTTP status code
						res.setStatusCode((int) ((FplInteger) result.get(0)).getValue());
						// map with response headers
						setHeaders(res, (FplObject) result.get(1));
						// Body as string, may be `nil`
						res.setBody(valueToString(result.get(2)), "UTF-8");
					} catch (EvaluationException e) {
						res.setBody(e.getMessage(), "UTF-8");
						int id = e.getId();
						res.setStatusCode(id == 0 ? 500 : id);
						return res;
					} catch (Exception e) {
						res.setBody(e.getMessage(), "UTF-8");
						res.setStatusCode(500);
						return res;
					}
					return res;
				}
			}
		});
	}

	private void setHeaders(HttpEntity entity, FplObject dict) {
		for (Entry<String, FplValue> entry : dict) {
			FplValue value = entry.getValue();
			if (value instanceof FplList) {
				for (FplValue v : (FplList) value) {
					entity.addHeader(entry.getKey(), valueToString(v));
				}
			} else {
				entity.addHeader(entry.getKey(), valueToString(value));
			}
		}
	}

	private String valueToString(FplValue value) {
		if (value == null) {
			return "";
		}
		if (value instanceof FplString) {
			return ((FplString) value).getContent();
		} else {
			return value.toString();
		}
	}

	private FplObject fplParams(HttpRequest res) throws ScopeException {
		FplObject params = new FplObject("dict");
		for (String name : res.getParamNames()) {
			List<String> values = res.getParams(name);
			int count = values.size();
			if (count == 1) {
				params.put(name, new FplString(values.get(0)));
			} else {
				params.put(name, FplList.fromIterator(new Iterator<FplValue>() {
					int i = 0;
					
					@Override
					public boolean hasNext() {
						return i < count;
					}

					@Override
					public FplValue next() {
						return new FplString(values.get(i++));
					}
				} , count));
			}
		}
		return params;
	}

	private FplObject fplHeaders(HttpEntity entity) throws ScopeException {
		FplObject headers = new FplObject("dict");
		for (String name : entity.getHeaderNames()) {
			if (!name.isEmpty()) {
				List<String> values = entity.getHeaders(name);
				int count = values.size();
				if (count == 1) {
					headers.put(name.toLowerCase(), new FplString(values.get(0)));
				} else {
					headers.put(name.toLowerCase(), FplList.fromIterator(new Iterator<FplValue>() {
						int i = 0;
						
						@Override
						public boolean hasNext() {
							return i < count;
						}

						@Override
						public FplValue next() {
							return new FplString(values.get(i++));
						}
					} , count));
				}
			}
		}
		return headers;
	}
	
	private FplString readFromInputStream(InputStream is) throws IOException {
		try (Reader rd = new BomAwareReader(is)) {
			StringBuilder sb = new StringBuilder();
			char[] buffer = new char[8192];
			int count = rd.read(buffer);
			while (count >= 0) {
				sb.append(buffer, 0, count);
				count = rd.read(buffer);
			}
			return new FplString(sb.toString());
		}
	}
}
