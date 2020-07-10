package de.codecentric.fpl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
	public void stopServerWithFplFunction() throws IOException {
		InputStream is = getClass().getResourceAsStream("stop-server.fpl");
		assertNotNull(is);
		String response = SimpleHttpClient.post(baseUrl, user, password, is, false);
		assertNotNull(response);
		assertEquals("\"Stopping HTTP server...\"", response.trim());
		stopped = true;
	}

	@Test
	public void emptyGetReturnsError() throws Exception {
		String response = get("");
		assertEquals("Post your FPL expressions to this URL for evaluation.", response);
	}

	@Test
	public void evaluateOneExpression() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4)"), false);
		assertEquals("7", response.trim());
	}

	@Test
	public void evaluatePrintExpression() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(print 42)"), false);
		assertEquals("42" + System.lineSeparator() + "nil", response.trim());
	}

	@Test
	public void evaluateNil() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("nil"), false);
		// nil -> null -> terminates the parsing loop, therefore "nothing" returned as
		// result.
		assertEquals("nil", response.trim());
	}

	@Test
	public void nilResultViaMainMethod() throws Exception {
		PrintStream originalOut = System.out;
		try (PrintStream dummyOut = new PrintStream(new ByteArrayOutputStream())) {
			System.setOut(dummyOut);
			File file = File.createTempFile("test", ".lisp");
			FileWriter writer = new FileWriter(file);
			writer.write("nil");
			writer.close();
			String[] args = new String[4];
			args[0] = baseUrl;
			args[1] = user;
			args[2] = password;
			args[3] = file.getAbsolutePath();
			SimpleHttpClient.main(args);
			// nil -> null -> terminates the parsing loop, therefore "nothing" returned as
			// result.
			file.delete();
		} finally {
			System.setOut(originalOut);
		}
	}

	@Test
	public void nilResultViaMainWithNonsensArg() throws Exception {
		PrintStream originalOut = System.out;
		try (PrintStream dummyOut = new PrintStream(new ByteArrayOutputStream())) {
			System.setOut(dummyOut);
			File file = File.createTempFile("test", ".lisp");
			FileWriter writer = new FileWriter(file);
			writer.write("nil");
			writer.close();
			String[] args = new String[5];
			args[0] = baseUrl;
			args[1] = user;
			args[2] = password;
			args[3] = file.getAbsolutePath();
			args[4] = "foobar";
			SimpleHttpClient.main(args);
			// nil -> null -> terminates the parsing loop, therefore "nothing" returned as
			// result.
			file.delete();
		} finally {
			System.setOut(originalOut);
		}
	}

	@Test
	public void nilResultViaMainLastBlockOnly() throws Exception {
		PrintStream originalOut = System.out;
		try (PrintStream dummyOut = new PrintStream(new ByteArrayOutputStream())) {
			System.setOut(dummyOut);
			File file = File.createTempFile("test", ".lisp");
			FileWriter writer = new FileWriter(file);
			writer.write("nil\r\nnil");
			writer.close();
			String[] args = new String[5];
			args[0] = baseUrl;
			args[1] = user;
			args[2] = password;
			args[3] = file.getAbsolutePath();
			args[4] = "lastBlockOnly";
			SimpleHttpClient.main(args);
			// nil -> null -> terminates the parsing loop, therefore "nothing" returned as
			// result.
			file.delete();
		} finally {
			System.setOut(originalOut);
		}
	}

	@Test
	public void emptyFileBlockOnly() throws Exception {
		PrintStream originalOut = System.out;
		try (PrintStream dummyOut = new PrintStream(new ByteArrayOutputStream())) {
			System.setOut(dummyOut);
			File file = File.createTempFile("test", ".lisp");
			FileWriter writer = new FileWriter(file);
			writer.write("\nnil");
			writer.close();
			String[] args = new String[5];
			args[0] = baseUrl;
			args[1] = user;
			args[2] = password;
			args[3] = file.getAbsolutePath();
			args[4] = "lastBlockOnly";
			SimpleHttpClient.main(args);
			file.delete();
		} finally {
			System.setOut(originalOut);
		}
	}

	@Test
	public void twoExpressionsReturnTwoResults() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("7" + System.lineSeparator() + System.lineSeparator() + "42", response.trim());
	}

	@Test
	public void oneExpressionFollowedByFailureGivesResultAndFailure() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4)\n(/ 3 0)"), false);
		assertEquals("7" + nl + nl + "java.lang.ArithmeticException: / by zero" + nl + "    at /(<unknown>:1)", response.trim());
	}

	@Test
	public void checkStacktrace() throws IOException {
		String input = "(def-function function-a (a) (function-b a))" + nl + //
				"(def-function function-b (a) (function-c a))" + nl + //
				"(def-function function-c (a) (/ 1 a))" + nl + //
				"(function-a 0)";
		String response = SimpleHttpClient.post(baseUrl, user, password, stream(input), false);
		assertEquals("(lambda (a) (function-b a))" + nl + nl + //
				"(lambda (a) (function-c a))" + nl + nl + //
				"(lambda (a) (/ 1 a))" + nl + nl + //
				"java.lang.ArithmeticException: / by zero" + nl + //
				"    at /(<unknown>:1)" + nl + //
				"    at function-c(http-post:3)" + nl + //
				"    at function-b(http-post:2)" + nl + //
				"    at function-a(http-post:1)", //
				response.trim());
	}

	@Test
	public void checkParseException() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password, stream("(+ 3 4"), false);
		assertEquals("Unexpected end of source in list", response.trim());
	}

	@Test
	public void oneExpressionExecutedLastBlockOnly() throws IOException {
		String str = "(* 6 7)\r\n\r\n(+ 3 4)";
		String response = SimpleHttpClient.post(baseUrl + "?lastBlockOnly", user, password, stream(str), false);
		assertEquals("7", response.trim());
	}

	@Test
	public void badUserShouldReturn401() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user + "foo", password, stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("Failure: 401", response.trim());
	}

	@Test
	public void wrongPasswordShouldReturn401() throws IOException {
		String response = SimpleHttpClient.post(baseUrl, user, password + "foo", stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("Failure: 401", response.trim());
	}
	
	@Test
	public void wrongHttpMethodShouldReturn500() throws IOException {
		URL url = new URL(baseUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		byte[] bytes = (user + ":" + password).getBytes();
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(bytes);
		con.setRequestProperty("Authorization", basicAuth);
		con.setRequestMethod("PUT");
		con.setDoOutput(true);
		try (OutputStream os = con.getOutputStream()) {
			for (byte b: "foo".getBytes()) {
				os.write(b);
			}
		}

		int responseCode = con.getResponseCode();
		assertEquals(500, responseCode);
	}
	
	@Test
	public void lastBlockOfTwoLineList() {
		String input = "(put a 3\n" + 
				")\n" + 
				"";
		String lastBlock =SimpleHttpServer.lastBlock(input);
		assertEquals(input.trim(), lastBlock);
	}
	
	@Test
	public void splitEmptyQueryShouldReturnEmptyMap() throws UnsupportedEncodingException {
		Map<String, List<String>> query = SimpleHttpServer.splitQuery("");
		assertEquals(0, query.size());
		query = SimpleHttpServer.splitQuery(null);
		assertEquals(0, query.size());
	}
	
	@Test
	public void splitQueryStringIntoMap() throws UnsupportedEncodingException {
		Map<String, List<String>> query = SimpleHttpServer.splitQuery("foo=bar&foo=baz&key1=&key2");
		assertEquals(3, query.size());
		List<String> foo = query.get("foo");
		assertEquals(2, foo.size());
		assertEquals("bar", foo.get(0));
		assertEquals("baz", foo.get(1));
		List<String> values1 = query.get("key1");
		assertEquals(1, values1.size());
		assertNull(values1.get(0));
		List<String> values2 = query.get("key2");
		assertEquals(1, values2.size());
		assertNull(values2.get(0));
	}
	
	@Test
	public void justInstantiateClientToCoverConstructor() {
		new SimpleHttpClient();
	}

	@Test
	public void interruptServer() throws Exception {
		assertTrue(SimpleHttpServer.isRunning());
		SimpleHttpServer.getInstance().interrupt();
		assertTrue(SimpleHttpServer.isRunning());
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
