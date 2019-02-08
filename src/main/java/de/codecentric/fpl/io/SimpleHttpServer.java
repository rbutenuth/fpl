package de.codecentric.fpl.io;

import static de.codecentric.fpl.datatypes.Function.comment;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.StringResultCallback;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

/**
 * A really simple HTTP server implementation. This is not really production
 * ready, but enough for testing and does not need any additional libraries.
 */
public class SimpleHttpServer extends Thread {
	private static boolean running;
	private boolean terminate;
	private HttpServer server;
	private FplEngine engine;

	/**
	 * @param args
	 *            <ul>
	 *            <li>[0]: HTTP server port</li>
	 *            <li>[1]: user</li>
	 *            <li>[2]: password</li>
	 *            </ul>
	 * @throws IOException
	 *             On socket failures etc.
	 */
	public static void main(String[] args) throws Exception {
		SimpleHttpServer s = new SimpleHttpServer(Integer.parseInt(args[0]), args[1], args[2]);
		s.start();
		synchronized (SimpleHttpServer.class) {
			running = true;
		}
	}

	public SimpleHttpServer(int port, String user, String password) throws IOException, EvaluationException, ScopeException {
		super("http-server-starter");
		server = HttpServer.create(new InetSocketAddress(port), 0);
		HttpContext context = server.createContext("/fpl");
		context.setAuthenticator(new BasicAuthenticator("FPL Server") {
			@Override
			public boolean checkCredentials(String credDser, String credPassword) {
				return user.equals(credDser) && password.equals(credPassword);
			}
		});
		context.setHandler((he) -> {
			String method = he.getRequestMethod();
			if ("GET".equals(method)) {
				handleGet(he);
			} else if ("POST".equals(method)) {
				handlePost(he);
			} else {
				he.sendResponseHeaders(500, 0);
			}
		});
		engine = new FplEngine();
		Scope scope = engine.getScope();
		scope.put(new Function("stop-server", comment("Stop HTTP server."), false) {

			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				terminateServer();
				return new FplString("Stopping HTTP server...");
			}
		});
	}

	@Override
	public void run() {
		server.start();
		synchronized (this) {
			while (!terminate) {
				try {
					wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		server.stop(2); // delay in seconds
		synchronized (SimpleHttpServer.class) {
			running = false;
			SimpleHttpServer.class.notifyAll();
		}
	}
	
	public static synchronized boolean isRunning() {
		return running;
	}

	private synchronized void terminateServer() {
		terminate = true;
		notifyAll();
	}

	private void handleGet(HttpExchange he) throws IOException {
		String response;
		URI uri = he.getRequestURI();
		if ("/fpl/terminate".equals(uri.getPath())) {
			terminateServer();
			response = "Good bye...";
		} else {
			response = "Post your FPL expressions to this URL for evaluation.";
		}
		sendResponse(he, response);
	}

	private void handlePost(HttpExchange he) throws IOException {
		URI uri = he.getRequestURI();
		Map<String, List<String>> queryMap = splitQuery(uri.getQuery());

		String content;

		try (Reader rd = new BomAwareReader(he.getRequestBody())) {
			content = readContent(rd);
		}

		if (queryMap.containsKey("lastBlockOnly")) {
			content = lastBlock(content);
		}
		StringResultCallback callback = new StringResultCallback(true);
		engine.evaluate("http-post", new StringReader(content), callback);

		sendResponse(he, callback.toString());
	}

	private void sendResponse(HttpExchange he, String response) throws IOException {
		byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
		he.sendResponseHeaders(200, bytes.length);
		try (OutputStream output = he.getResponseBody()) {
			output.write(bytes);
		}
		he.close();
	}

	private String lastBlock(String raw) {
		String text = raw.trim();
		int newLinePos = 0;
		int i = text.length() - 1;
		while (i > 0) {
			while (i > 0 && text.charAt(i) != '\n') {
				i--;
			}
			newLinePos = i--;
			while (i > 0 && text.charAt(i) != '\n') {
				i--;
			}
			int prevNewLinePos = i--;
			if (text.substring(prevNewLinePos, newLinePos).trim().length() == 0) {
				break;
			}
		}
		return text.substring(newLinePos + 1);
	}

	public Map<String, List<String>> splitQuery(String query) throws UnsupportedEncodingException {
		if (query == null || query.length() == 0) {
			return new HashMap<>();
		}
		Map<String, List<String>> queryPairs = new LinkedHashMap<String, List<String>>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!queryPairs.containsKey(key)) {
				queryPairs.put(key, new ArrayList<String>());
			}
			String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
					: null;
			queryPairs.get(key).add(value);
		}
		return queryPairs;
	}

	private String readContent(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int ch = rd.read();
		while (ch != -1) {
			sb.append((char) ch);
			ch = rd.read();
		}
		return sb.toString();
	}
}
