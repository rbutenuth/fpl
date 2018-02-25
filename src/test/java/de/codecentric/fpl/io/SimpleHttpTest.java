package de.codecentric.fpl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleHttpTest {
	private final static String baseUrl = "http://localhost:9099/fpl";
	private final static String user = "fred";
	private final static String nl = System.lineSeparator();


	private String password;
	private boolean stopped;

	@Before
	public void startServer() throws Exception {
		SecureRandom sr = new SecureRandom();
		byte[] bytes = new byte[32];
		sr.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ('0' + ((0xff & bytes[i]) % 10));
		}
		password = new String(bytes);
		SimpleHttpServer.main(new String[] { "9099", user, password });
	}

	@After
	public void stopServer() throws Exception {
		if (!stopped) {
			get("/terminate");
		}
		synchronized (SimpleHttpServer.class) {
			while (SimpleHttpServer.isRunning()) {
				SimpleHttpServer.class.wait(1000);
			}
		}
	}

	@Test
	public void testStopServerWithFplFunction() throws IOException {
		InputStream is = getClass().getResourceAsStream("stop-server.fpl");
		assertNotNull(is);
		String response = SimpleHttpClient.post(baseUrl, user, password, is, false);
		assertNotNull(response);
		assertEquals("\"Stopping HTTP server...\"", response.trim());
		stopped = true;
	}

	@Test
	public void testGet() throws Exception {
		String response = get("");
		assertEquals("Post your FPL expressions to this URL for evaluation.", response);
	}

	@Test
	public void testOneExpression() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4)"), false);
		assertEquals("7", response.trim());
	}

	@Test
	public void testNullResult() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("nil"), false);
		// nil -> null -> terminates the parsing loop, therefore "nothing" returned as result.
		assertEquals("nil", response.trim());
	}

	@Test
	public void testTwoExpressions() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("7" + System.lineSeparator() + System.lineSeparator() + "42", response.trim());
	}

	@Test
	public void testExpressionFollowedByFailure() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4)\n(/ 3 0)"), false);
		assertEquals("7" + nl + nl + "/ by zero" + nl + "    at /(<unknown>:1)",
				response.trim());
	}

	@Test
	public void testStacktrace() throws IOException {
		String input = "(defun function-a (a) (function-b a))" + nl + //
				"(defun function-b (a) (function-c a))" + nl + //
				"(defun function-c (a) (/ 1 a))" + nl + //
				"(function-a 0)";
		String response = SimpleHttpClient.post(baseUrl, user, password, stream(input), false);
		assertEquals("(lambda (a) (function-b a))" + nl + nl + //
				"(lambda (a) (function-c a))" + nl + nl + //
				"(lambda (a) (/ 1 a))" + nl + nl + //
				"/ by zero" + nl + //
				"    at /(<unknown>:1)" + nl + //
				"    at function-c(http-post:3)" + nl + //
				"    at function-b(http-post:2)" + nl + //
				"    at function-a(http-post:1)", //
				response.trim());
	}

	@Test
	public void testParseException() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4"), false);
		assertEquals("Unexpected end of source in list", response.trim());
	}

	@Test
	public void testOneExpressionLastBlockOnly() throws IOException {
		String str = "(* 6 7)\r\n\r\n(+ 3 4)";
		String response = SimpleHttpClient.post(baseUrl + "?lastBlockOnly", user, password, stream(str), false);
		assertEquals("7", response.trim());
	}

	private InputStream stream(String str) {
		return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
	}

	private String get(String relativeUrl) throws Exception {
		URL url = new URL(baseUrl + relativeUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		byte[] bytes = (user + ":" + password).getBytes();
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(bytes);
		con.setRequestProperty("Authorization", basicAuth);
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		assertEquals(200, responseCode);
		try (Reader rd = new BomAwareReader(con.getInputStream())) {
			return readContent(rd);
		}
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
