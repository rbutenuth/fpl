package de.codecentric.fpl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExecuteViaHttpTest {
	private final static String user = "fred";
	private final static String nl = System.lineSeparator();

	private static int nextPort = 9099;
	private int port;
	private String baseUrl;
	private String password;

	@Before
	public void startServer() throws Exception {
		port = nextPort++;
		baseUrl = "http://localhost:" + port + "/fpl";
		SecureRandom sr = new SecureRandom();
		byte[] bytes = new byte[32];
		sr.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ('0' + ((0xff & bytes[i]) % 10));
		}
		password = new String(bytes);
		HttpServerMain.main(new String[] { Integer.toString(port), user, password });
		// Wait until server in background thread has started and we can really connect.
		boolean success = false;
		while (!success) {
			try (Socket s = new Socket(InetAddress.getLocalHost(), port)) {
				success = true;
			} catch (IOException e) {
				Thread.sleep(100);
			}
		}
	}

	@After
	public void stopServer() throws Exception {
		HttpServerMain.terminate(0);
		HttpServerMain.waitForTermination();
	}

	@Test
	public void coverConstructors() {
		new HttpServerMain();
		new StreamUtil();
	}

	@Test
	public void stopServerWithGetTerminate() throws IOException {
		HttpRequest req = new HttpRequest();
		req.setBaseUri(baseUrl + "/terminate");
		req.setBasicAuth(user, password);
		new HttpClient().execute(req);
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(200, res.getStatusCode());
	}

	@Test
	public void stopServerWithFplFunction() throws IOException {
		InputStream is = getClass().getResourceAsStream("stop-server.fpl");
		assertNotNull(is);
		String response = ExecuteViaHttp.post(baseUrl, user, password, is, false);
		assertNotNull(response);
		assertEquals("\"Stopping HTTP server...\"", response.trim());
	}

	@Test
	public void getWithoutAuthGives401() throws IOException {
		HttpRequest req = new HttpRequest();
		req.setBaseUri(baseUrl + "/terminate");
		new HttpClient().execute(req);
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(401, res.getStatusCode());
	}

	@Test
	public void postToBadUrlAuthGives404() throws IOException {
		HttpRequest req = new HttpRequest();
		req.setBasicAuth(user, password);
		req.setBaseUri(baseUrl.substring(0, baseUrl.indexOf("fpl")));
		req.setMethod("POST");
		new HttpClient().execute(req);
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(404, res.getStatusCode());
	}

	@Test
	public void emptyGetReturnsError() throws Exception {
		HttpRequest req = new HttpRequest();
		req.setBaseUri(baseUrl);
		req.setBasicAuth(user, password);
		req.addHeader("key", "value"); // some useless header
		new HttpClient().execute(req);
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(200, res.getStatusCode());
		assertEquals("Post your FPL expressions to this URL for evaluation.", res.getBodyAsString("UTF-8"));
	}

	@Test
	public void evaluateOneExpression() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream("(+ 3 4)"), false);
		assertEquals("7", response.trim());
	}

	@Test
	public void evaluatePrintExpression() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream("(print 42)"), false);
		assertEquals("42" + System.lineSeparator() + "nil", response.trim());
	}

	@Test
	public void evaluateNil() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream("nil"), false);
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
			ExecuteViaHttp.main(args);
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
			ExecuteViaHttp.main(args);
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
			ExecuteViaHttp.main(args);
			// nil -> null -> terminates the parsing loop, therefore "nothing" returned as
			// result.
			file.delete();
		} finally {
			System.setOut(originalOut);
		}
	}

	@Test
	public void nilResultViaMainLastBlockOnlyAndOnlyOneBlockInFile() throws Exception {
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
			ExecuteViaHttp.main(args);
			file.delete();
		} finally {
			System.setOut(originalOut);
		}
	}

	@Test
	public void twoExpressionsReturnTwoResults() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("7" + System.lineSeparator() + System.lineSeparator() + "42", response.trim());
	}

	@Test
	public void oneExpressionFollowedByFailureGivesResultAndFailure() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream("(+ 3 4)\n(/ 3 0)"), false);
		assertEquals("7" + nl + nl + "java.lang.ArithmeticException: / by zero" + nl + "    at /(<unknown>:1)",
				response.trim());
	}

	@Test
	public void checkStacktrace() throws IOException {
		String input = "(def-function function-a (a) (function-b a))" + nl + //
				"(def-function function-b (a) (function-c a))" + nl + //
				"(def-function function-c (a) (/ 1 a))" + nl + //
				"(function-a 0)";
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream(input), false);
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
		String response = ExecuteViaHttp.post(baseUrl, user, password, stream("(+ 3 4"), false);
		assertEquals("Unexpected end of source in list", response.trim());
	}

	@Test
	public void oneExpressionExecutedLastBlockOnly() throws IOException {
		String str = "(* 6 7)\r\n\r\n(+ 3 4)";
		String response = ExecuteViaHttp.post(baseUrl + "?lastBlockOnly", user, password, stream(str), false);
		assertEquals("7", response.trim());
	}

	@Test
	public void badUserShouldReturn401() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user + "foo", password, stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("Failure: 401, reason: Unauthorized", response.trim());
	}

	@Test
	public void wrongPasswordShouldReturn401() throws IOException {
		String response = ExecuteViaHttp.post(baseUrl, user, password + "foo", stream("(+ 3 4) (* 6 7)"), false);
		assertEquals("Failure: 401, reason: Unauthorized", response.trim());
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
			for (byte b : "foo".getBytes()) {
				os.write(b);
			}
		}

		int responseCode = con.getResponseCode();
		assertEquals(500, responseCode);
	}

	@Test
	public void lastBlockOfTwoLineList() {
		String input = "(put a 3\n" + ")\n" + "";
		String lastBlock = HttpServerMain.lastBlock(input);
		assertEquals(input.trim(), lastBlock);
	}

	@Test
	public void justInstantiateClientToCoverConstructor() {
		new ExecuteViaHttp();
	}

	private InputStream stream(String str) {
		return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
	}
}
