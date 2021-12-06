package de.codecentric.fpl.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.BasicAuthenticator;

public class SimpleHttpServerTest {
	private final static String testUser = "fred";

	private int port;
	private String baseUrl;
	private String testPassword;
	private SimpleHttpServer server;
	private List<HttpRequest> requests;
	private HttpRequest req;

	@BeforeEach
	public void startServer() throws Exception {
		SecureRandom sr = new SecureRandom();
		byte[] bytes = new byte[32];
		sr.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ('0' + ((0xff & bytes[i]) % 10));
		}
		testPassword = new String(bytes);
		server = new SimpleHttpServer(ForkJoinPool.commonPool(), port, new Handler(), new BasicAuthenticator("unit-test") {
			
			@Override
			public boolean checkCredentials(String username, String password) {
				return testUser.equals(username) && testPassword.equals(password);
			}
		});
		port = server.getPort();

		requests = new ArrayList<HttpRequest>();

		baseUrl = "http://localhost:" + port + "/test";
		
		req = new HttpRequest();
		req.setBaseUri(baseUrl);
		req.setBasicAuth(testUser, testPassword);
	}

	@AfterEach
	public void stopServer() throws Exception {
		server.terminate(0);
		server.waitForTermination();
	}

	@Test
	public void waitTwoTimesForTermination() {
		server.terminate(0);
		server.waitForTermination();
	}
	
	private AtomicBoolean waiting = new AtomicBoolean(false);
	private Thread waiter;
	
	@Test
	public void InterruptWaitTermination() throws Exception {
		Future<?> future = ForkJoinPool.commonPool().submit(new Runnable() {
			@Override
			public void run() {
				waiter = Thread.currentThread();
				waiting.set(true);
				server.waitForTermination();
			}
		});
		while (!waiting.get()) {
			Thread.sleep(1);;
		}
		waiter.interrupt();
		server.terminate(0);
		future.get();
	}
	
	@Test
	public void simpelGet() throws Exception {
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(1, requests.size());
		HttpRequest serverRequest = requests.get(0);
		assertEquals(testUser, serverRequest.getUser());

		assertEquals("Hello world!", res.getBodyAsString("UTF-8"));
		List<String> values = res.getHeaders("Key");
		assertEquals(2, values.size());
		assertTrue(values.contains("value-1"));
		assertTrue(values.contains("value-2"));
	}
	
	@Test
	public void getWithQueryParams() throws Exception {
		req.addParam("key", "value-1");
		req.addParam("key", "value-2");
		req.addParam("key2", "");
		new HttpClient().execute(req);
		assertEquals(1, requests.size());
		HttpRequest serverRequest = requests.get(0);
		assertTrue(serverRequest.getParamNames().contains("key"));
		List<String> values = serverRequest.getParams("key");
		assertEquals(2, values.size());
		assertTrue(values.contains("value-1"));
		assertTrue(values.contains("value-2"));
		List<String> values2 = serverRequest.getParams("key2");
		assertEquals(1, values2.size());
		assertTrue(values2.contains(""));
	}
	
	@Test
	public void serverError() throws Exception {
		req.addParam("bäm!", "");
		HttpResponse res = new HttpClient().execute(req);
		assertEquals(500, res.getStatusCode());
	}
	
	private class Handler implements HttpRequestHandler {

		@Override
		public HttpResponse handleRequest(HttpRequest req) throws Exception {
			synchronized (SimpleHttpServerTest.this) {
				requests.add(req);
			}
			if (req.getParamNames().contains("bäm!")) {
				throw new Exception("bäm!");
			}
			HttpResponse res = new HttpResponse();
			res.addHeader("Key", "value-1");
			res.addHeader("Key", "value-2");
			res.setBody("Hello world!", "UTF-8");
			return res;
		}
	}
}
