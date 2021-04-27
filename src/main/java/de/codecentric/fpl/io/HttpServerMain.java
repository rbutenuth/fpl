package de.codecentric.fpl.io;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import com.sun.net.httpserver.BasicAuthenticator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.StringResultCallback;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * A really simple HTTP server implementation. This is not really production
 * ready, but enough for testing and does not need any additional libraries.
 */
public class HttpServerMain {
	private static SimpleHttpServer server;
	private static FplEngine engine;

	/**
	 * @param args
	 *             <ul>
	 *             <li>[0]: HTTP server port</li>
	 *             <li>[1]: user</li>
	 *             <li>[2]: password</li>
	 *             </ul>
	 * @throws IOException On socket failures etc.
	 */
	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(args[0]);
		String serverUser = args[1];
		String serverPassword = args[2];

		engine = new FplEngine();
		Scope scope = engine.getScope();
		scope.define(new AbstractFunction("stop-server", "Stop HTTP server.") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				terminate(2);
				return new FplString("Stopping HTTP server...");
			}
		});

		BasicAuthenticator authenticator = new BasicAuthenticator("FPL Server") {
			@Override
			public boolean checkCredentials(String user, String password) {
				return serverUser.equals(user) && serverPassword.equals(password);
			}
		};

		HttpRequestHandler handler = new HttpRequestHandler() {
			@Override
			public HttpResponse handleRequest(HttpRequest req) throws Exception {
				HttpResponse res = new HttpResponse();
				String method = req.getMethod();
				if ("GET".equals(method)) {
					handleGet(req, res);
				} else if ("POST".equals(method)) {
					handlePost(req, res);
				} else {
					res.setStatusCode(500);
					res.setStatusMessage("unsupported method");
				}
				return res;
			}
		};

		server = new SimpleHttpServer(engine.getPool(), port, handler, authenticator);
	}

	public static void terminate(int delayInSeconds) {
		server.terminate(delayInSeconds);
	}

	public static void waitForTermination() {
		server.waitForTermination();
	}
	
	private static void handleGet(HttpRequest req, HttpResponse res) throws IOException {
		if ("/fpl/terminate".equals(req.getBaseUri())) {
			res.setBody("Good bye...", "UTF-8");
			terminate(2);
		} else {
			res.setBody("Post your FPL expressions to this URL for evaluation.", "UTF-8");
		}
	}

	private static void handlePost(HttpRequest req, HttpResponse res) throws IOException {
		if ("/fpl".equals(req.getBaseUri())) {
			String content = req.getBodyAsString("UTF-8");
			if (req.getParamNames().contains("lastBlockOnly")) {
				content = lastBlock(content);
			}
			StringResultCallback callback = new StringResultCallback(true);
			synchronized (engine) {
				engine.setSystemOut(new PrintStream(callback.getOutputStream(), true, "UTF-8"));
				engine.evaluate("http-post", new StringReader(content), callback);
				engine.setSystemOut(System.out);
			}
			res.setBody(callback.toString(), "UTF-8");
		} else {
			res.setStatusCode(404);
		}
	}

	public static String lastBlock(String raw) {
		// Split at line breaks
		String[] text = raw.trim().split("\\R");
		// Search for the first empty line from the end
		int i = text.length - 1;
		while (i > 0 && text[i].trim().length() > 0) {
			i--;
		}
		StringBuilder sb = new StringBuilder(raw.length());
		while (i < text.length) {
			sb.append(text[i++]);
			if (i < text.length) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}
}
