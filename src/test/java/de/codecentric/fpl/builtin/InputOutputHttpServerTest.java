package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.io.HttpClient;
import de.codecentric.fpl.io.HttpRequest;
import de.codecentric.fpl.io.HttpResponse;

public class InputOutputHttpServerTest extends AbstractFplTest {
	private final static String testUser = "flintstone";
	private String testPassword;
	private static int nextPort = 9099;
	private int port;
	private String baseUrl;
	private HttpRequest req;

	@Before
	public void prepareForServerStart() throws Exception {
		port = nextPort++;
		baseUrl = "http://localhost:" + port + "/";
		SecureRandom sr = new SecureRandom();
		byte[] bytes = new byte[32];
		sr.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ('0' + ((0xff & bytes[i]) % 10));
		}
		testPassword = new String(bytes);

		req = new HttpRequest();
		req.setBaseUri(baseUrl);
		req.setMethod("GET");

		evaluate("auth", "(def-function auth (user password) " + "(and (eq \"" + testUser + "\" user) (eq \""
				+ testPassword + "\" password)))");
		evaluate("auth-fail", "(def-function auth-fail (user password) " + "(/ 1 0))");
	}

	@After
	public void stopServer() throws Exception {
		// Don't try to terminate when the start failed. :-)
		evaluate("terminate", "(if (ne terminate nil) (terminate 0))");
	}

	@Test
	public void startStopServerWithoutAuthentication() throws Exception {
		evaluate("start", "(def terminate (http-server " + port + " nil nil))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);

		HttpResponse res = new HttpClient().execute(req);
		assertEquals(404, res.getStatusCode());
	}

	@Test
	public void authenticatorNotALambda() throws Exception {
		try {
			evaluate("start", "(def terminate (http-server " + port + " 42 nil))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not a lambda: 42", e.getMessage());
		}
	}

	@Test
	public void badPort() throws Exception {
		try {
			evaluate("start", "(def terminate (http-server -1 auth nil))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("port out of range:-1", e.getMessage());
		}
	}

	@Test
	public void startStopServerWithoutAuthenticationByNullSymbol() throws Exception {
		evaluate("start", "(def terminate (http-server " + port + " not-an-authentication-function nil))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);

		HttpResponse res = new HttpClient().execute(req);
		assertEquals(404, res.getStatusCode());
	}

	@Test
	public void startStopServerWithWrongAuthentication() throws Exception {
		evaluate("start", "(def terminate (http-server " + port + " auth nil))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);

		setWrongUser();
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(401, res.getStatusCode());
	}

	@Test
	public void startStopServerWithCorrectAuthentication() throws Exception {
		evaluate("start", "(def terminate (http-server " + port + " auth nil))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(404, res.getStatusCode());
	}

	@Test
	public void getRequestWithHeadersAndParamameters() throws Exception {
		// Return the query parameters as response headers 
		evaluate("callback-1", "(def-function callback-1 (path headers params body) (list 201 {} nil))");
		evaluate("callback-2", "(def-function callback-2 (path headers params body) (list 200 params \"Hello world\"))");
		evaluate("start", "(def terminate (http-server " + port + " auth nil "
				+ "(list \"GET\" \"/foo\" callback-1)"
				+ "(list \"POST\" \"/get-path\" callback-1)"
				+ "(list \"GET\" \"/get-path\" callback-2)))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		req.setBaseUri(baseUrl + "get-path");
		req.addParam("key", "value");
		req.addParam("multikey", "value-1");
		req.addParam("multikey", "value-2");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(200, res.getStatusCode());
		Set<String> headerNames = res.getHeaderNames();
		assertTrue(headerNames.contains("Key"));
		assertTrue(headerNames.contains("Multikey"));
		assertEquals("value", res.getHeader("Key"));
		List<String> values = res.getHeaders("Multikey");
		assertEquals(2, values.size());
		assertTrue(values.contains("value-1"));
		assertTrue(values.contains("value-2"));
		assertEquals("Hello world", res.getBodyAsString("UTF-8"));
	}

	@Test
	public void getRequestWithWildcard() throws Exception {
		evaluate("callback-1", "(def-function callback-1 (path headers params body) (list 201 {} nil))");
		evaluate("callback-2", "(def-function callback-2 (path headers params body) (list 200 params \"Hello world\"))");
		evaluate("start", "(def terminate (http-server " + port + " auth nil "
				+ "(list \"GET\" \"/foo\" callback-1)"
				+ "(list \"POST\" \"/get-path\" callback-1)"
				+ "(list \"GET\" \"/get*\" callback-2)))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		req.setBaseUri(baseUrl + "get-path");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(200, res.getStatusCode());
		assertEquals("Hello world", res.getBodyAsString("UTF-8"));
	}

	@Test
	public void getRequestLowerCaseMethodAndMissingSlashInPath() throws Exception {
		evaluate("callback-1", "(def-function callback-1 (path headers params body) (list 201 {} nil))");
		evaluate("callback-2", "(def-function callback-2 (path headers params body) (list 200 params \"Hello world\"))");
		evaluate("start", "(def terminate (http-server " + port + " auth nil "
				+ "(list \"GET\" \"/foo\" callback-1)"
				+ "(list \"POST\" \"/get-path\" callback-1)"
				+ "(list \"get\" \"get-path\" callback-2)))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		req.setBaseUri(baseUrl + "get-path");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(200, res.getStatusCode());
		assertEquals("Hello world", res.getBodyAsString("UTF-8"));
	}

	@Test
	public void getRequestWithExceptionInHandlerWithLogger() throws Exception {
		evaluate("callback", "(def-function callback (path headers params body) (/ 1 0))");
		evaluate("logger", "(def-function logger (message) (def-global log-message message))");
		evaluate("start", "(def terminate (http-server " + port + " auth logger "
				+ "(list \"GET\" \"/get-path\" callback)))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		req.setBaseUri(baseUrl + "get-path");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(500, res.getStatusCode());
		FplString msg = (FplString) evaluate("extract", "log-message");
		assertEquals("de.codecentric.fpl.EvaluationException: java.lang.ArithmeticException: / by zero", msg.getContent());
	}

	@Test
	public void getRequestWithExceptionInHandler() throws Exception {
		evaluate("callback", "(def-function callback (path headers params body) (/ 1 0))");
		evaluate("start", "(def terminate (http-server " + port + " auth nil "
				+ "(list \"GET\" \"/get-path\" callback)))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		req.setBaseUri(baseUrl + "get-path");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(500, res.getStatusCode());
	}

	@Test
	public void postRequestWith() throws Exception {
		evaluate("callback", "(def-function callback (path headers params body) (list 201 {} (join \"Body: \" body)))");
		evaluate("start", "(def terminate (http-server " + port + " auth nil "
				+ "(list \"POST\" \"/get-path\" callback)))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		req.setBaseUri(baseUrl + "get-path");
		req.setMethod("POST");
		req.setBody("abcdef", "UTF-8");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(201, res.getStatusCode());
		assertEquals("Body: abcdef", res.getBodyAsString("UTF-8"));
	}

	@Test
	public void startStopServerWithExceptionInAuthentication() throws Exception {
		evaluate("start", "(def terminate (http-server " + port + " auth-fail nil))");
		Function terminate = (Function) evaluate("lambda", "terminate");
		assertNotNull(terminate);
		setCorrectUser();
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(401, res.getStatusCode());
	}

	private void setWrongUser() {
		req.setBasicAuth(testUser, "not-good");
	}

	private void setCorrectUser() {
		req.setBasicAuth(testUser, testPassword);
	}
}
