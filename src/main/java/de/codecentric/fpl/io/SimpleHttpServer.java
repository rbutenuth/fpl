package de.codecentric.fpl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.builtin.Parallel;
import de.codecentric.fpl.data.ScopeException;

public class SimpleHttpServer {
	private HttpServer server;
	private boolean running;

	public SimpleHttpServer(int port, HttpRequestHandler handler, BasicAuthenticator authenticator)
			throws IOException, EvaluationException, ScopeException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.setExecutor(Parallel.fplPool());
		HttpContext context = server.createContext("/");
		context.setAuthenticator(authenticator);
		context.setHandler((he) -> {
			HttpRequest req = new HttpRequest();
			req.setMethod(he.getRequestMethod());
			URI uri = he.getRequestURI();
			req.setBaseUri(uri.getPath());
			handleQueryPart(req, uri.getQuery());
			handleHeaders(req, he);
			handleBody(req, he.getRequestBody());

			try {
				HttpResponse res = handler.handleRequest(req);
				Headers resHeaders = he.getResponseHeaders();
				for (String key : res.getHeaderNames()) {
					for (String value : res.getHeaders(key)) {
						resHeaders.add(key, value);
					}
				}
				if (res.hasBody()) {
					byte[] bytes = res.getBody();
					he.sendResponseHeaders(res.getStatusCode(), bytes.length);
					try (OutputStream output = he.getResponseBody()) {
						output.write(bytes);
					}
				} else {
					he.sendResponseHeaders(res.getStatusCode(), 0);
				}
			} catch (Exception e) {
				he.sendResponseHeaders(500, 0);
			}
			he.close();
		});
		server.start();
		running = true;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void waitForTermination() {
		while (running) {
			try {
				wait();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
	
	/**
	 * Terminate the server in the background, don't wait for termination.
	 * 
	 * @param delayInSeconds Delay for termination.
	 */
	public synchronized void terminate(int delayInSeconds) {
		if (isRunning()) {
			Parallel.fplPool().execute(new Runnable() {
				@Override
				public void run() {
					server.stop(delayInSeconds);
					synchronized (SimpleHttpServer.this) {
						running = false;
						SimpleHttpServer.this.notifyAll();
					}
				}
			});
		}
	}

	private void handleHeaders(HttpRequest req, HttpExchange he) {
		Headers reqHeaders = he.getRequestHeaders();
		for (Entry<String, List<String>> entry : reqHeaders.entrySet()) {
			String key = entry.getKey().toLowerCase();
			for (String value : entry.getValue()) {
				if (value.indexOf(',') < 0) {
					req.addHeader(key, value);
				} else {
					for (String v : value.split(",")) {
						req.addHeader(key, v.trim());
					}
				}
			}
			if ("authorization".equals(key)) {
				req.setBasicAuth(entry.getValue().get(0));
			}
		}
	}

	public void handleQueryPart(HttpRequest req, String query) throws UnsupportedEncodingException {
		if (query == null) {
			return;
		}
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			String key, value;
			int idx = pair.indexOf("=");
			if (idx > 0) {
				key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
				value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
			} else {
				key = pair;
				value = "";
			}
			req.addParam(key, value);
		}
	}

	private void handleBody(HttpRequest req, InputStream is) throws IOException {
		try {
			req.setBody(StreamUtil.readStreamToBytes(is));
		} finally {
			is.close();
		}
	}
}
