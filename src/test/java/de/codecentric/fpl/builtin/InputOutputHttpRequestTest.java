package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.BasicAuthenticator;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.io.HttpRequest;
import de.codecentric.fpl.io.HttpRequestHandler;
import de.codecentric.fpl.io.HttpResponse;
import de.codecentric.fpl.io.SimpleHttpServer;

public class InputOutputHttpRequestTest extends AbstractFplTest {
	private final static String testUser = "flintstone";
	private String testPassword;
	private static int nextPort = 9099;
	private int port;
	private String baseUrl;
	private SimpleHttpServer server;
	private List<HttpRequest> requests;

	@Before
	public void startServer() throws Exception {
		port = nextPort++;
		baseUrl = "http://localhost:" + port + "/test";
		SecureRandom sr = new SecureRandom();
		byte[] bytes = new byte[32];
		sr.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ('0' + ((0xff & bytes[i]) % 10));
		}
		testPassword = new String(bytes);
		server = new SimpleHttpServer(port, new Handler(), new BasicAuthenticator("unit-test") {

			@Override
			public boolean checkCredentials(String username, String password) {
				return testUser.equals(username) && testPassword.equals(password);
			}
		});
		// Wait until server in background thread has started and we can really connect.
		boolean success = false;
		while (!success) {
			try (Socket s = new Socket(InetAddress.getLocalHost(), port)) {
				success = true;
			} catch (IOException e) {
				Thread.sleep(100);
			}
		}
		requests = new ArrayList<HttpRequest>();
	}

	@After
	public void stopServer() throws Exception {
		server.terminate(0);
		server.waitForTermination();
	}

	@Test
	public void getRequest() throws Exception {
		FplList list = execute("/hello", "GET", "nil", "nil", "nil");
		assertEquals(3, list.size());
		assertEquals(200, ((FplInteger) list.get(0)).getValue());
		assertEquals("Hello world!", ((FplString) list.get(2)).getContent());
	}

	@Test
	public void getRequestWithHeadersFromVariable() throws Exception {
		FplList list = execute("/hello", "GET", "not-defined-symbol-with-no-headers", "nil", "nil");
		assertEquals(3, list.size());
		assertEquals(200, ((FplInteger) list.get(0)).getValue());
		assertEquals("Hello world!", ((FplString) list.get(2)).getContent());
	}

	@Test
	public void getRequestWithHeadersAreNotDictionary() throws Exception {
		try {
			execute("/hello", "GET", "'(1 2)", "nil", "nil");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not a dictionary: (1 2)", e.getMessage());
		}
	}

	@Test
	public void getRequestWithHeadersAndQueryParams() throws Exception {
		String headers = "{ h1: \"value1\" h2: (1 2) h3: (nil)}";
		String params = "{ p1: \"p-value1\" p2: (3 4)}";
		FplList list = execute("/hello", "GET", headers, params, "nil");
		assertEquals(3, list.size());
		assertEquals(200, ((FplInteger) list.get(0)).getValue());
		assertEquals("Hello world!", ((FplString) list.get(2)).getContent());
		assertEquals(1, requests.size());
		HttpRequest req = requests.get(0);

		Set<String> headerNames = req.getHeaderNames();
		assertTrue(headerNames.contains("h1"));
		assertTrue(headerNames.contains("h2"));
		assertTrue(headerNames.contains("h3"));
		List<String> h1List = req.getHeaders("h1");
		assertEquals(1, h1List.size());
		assertTrue(h1List.contains("value1"));
		List<String> h2List = req.getHeaders("h2");
		assertEquals(2, h2List.size());
		assertTrue(h2List.contains("1"));
		assertTrue(h2List.contains("2"));
		List<String> h3List = req.getHeaders("h3");
		assertEquals(1, h3List.size());
		assertTrue(h3List.contains(""));

		Set<String> paramNames = req.getParamNames();
		assertTrue(paramNames.contains("p1"));
		assertTrue(paramNames.contains("p2"));
		List<String> p1List = req.getParams("p1");
		assertEquals(1, p1List.size());
		assertTrue(p1List.contains("p-value1"));
		List<String> p2List = req.getParams("p2");
		assertEquals(2, p2List.size());
		assertTrue(p2List.contains("3"));
		assertTrue(p2List.contains("4"));
	}

	@Test
	public void postRequest() throws Exception {
		String body = "A total meaningless body.";
		FplList list = execute("/hello", "POST", "nil", "nil", "\"" + body + "\"");
		assertEquals(3, list.size());
		assertEquals(1, requests.size());
		HttpRequest req = requests.get(0);
		assertEquals(body, req.getBodyAsString("UTF-8"));
	}

	@Test
	public void postRequestWithListBody() throws Exception {
		FplList list = execute("/hello", "POST", "nil", "nil", "'(1 2 3)");
		assertEquals(3, list.size());
		assertEquals(1, requests.size());
		HttpRequest req = requests.get(0);
		assertEquals("(1 2 3)", req.getBodyAsString("UTF-8"));
	}

	@Test
	public void postRequestCreaeNoResponseBody() throws Exception {
		String body = "A total meaningless body.";
		FplList list = execute("/create", "POST", "nil", "nil", "\"" + body + "\"");
		assertEquals(3, list.size());
		assertEquals(201, ((FplInteger) list.get(0)).getValue());
		assertNull(list.get(2));
		assertEquals(1, requests.size());
		HttpRequest req = requests.get(0);
		assertEquals(body, req.getBodyAsString("UTF-8"));
	}

	private FplList execute(String path, String method, String headers, String query, String body) throws Exception {
		// (http-request url method headers query-params body user password)
		String expression = "(http-request \"http://localhost:" + port //
				+ "" + path + "\"" //
				+ " \"" + method + "\" " //
				+ headers + " " + query + " " //
				+ body + " " + "\"" + testUser + "\" \"" + testPassword + "\")";
		FplList list = (FplList) evaluate("http-request", expression);
		return list;
	}

	@Test
	public void getRequestWithBadProtocol() throws Exception {
		String expression = "(http-request \"httpxxx://localhost:1/hello\"" //
				+ " \"GET\" " //
				+ " nil nil nil " // headers, query, body
				+ "\"" + testUser + "\" \"" + testPassword + "\")";
		try {
			evaluate("http-request", expression);
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("unknown protocol: httpxxx", e.getMessage());
		}
	}

	private class Handler implements HttpRequestHandler {

		@Override
		public HttpResponse handleRequest(HttpRequest req) throws Exception {
			synchronized (InputOutputHttpRequestTest.this) {
				requests.add(req);
			}
			HttpResponse res = new HttpResponse();
			res.addHeader("Key", "value-1");
			res.addHeader("Key", "value-2");
			if (req.getBaseUri().equals("/hello")) {
				res.setBody("Hello world!", "UTF-8");
			} else if (req.getBaseUri().equals("/create")) {
				res.setStatusCode(201);
			}
			return res;
		}
	}
}
