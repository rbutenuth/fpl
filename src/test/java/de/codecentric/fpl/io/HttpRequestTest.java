package de.codecentric.fpl.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

public class HttpRequestTest {
	private HttpRequest req = new HttpRequest();

	@Test
	public void emptyAuth() {
		assertNull(req.getBasicAuth());
		assertNull(req.getUser());
		assertNull(req.getPassword());
	}

	@Test
	public void setUserAndPassword() {
		req.setBasicAuth("max", "secret");
		assertEquals("Basic bWF4OnNlY3JldA==", req.getBasicAuth());
	}

	@Test
	public void setBasicAuth() {
		req.setBasicAuth("Basic bWF4OnNlY3JldA==");
		assertEquals("max", req.getUser());
		assertEquals("secret", req.getPassword());
	}

	@Test
	public void setBasicAuthMissingPrefix() {
		req.setBasicAuth("bWF4OnNlY3JldA==");
		assertNull(req.getUser());
		assertNull(req.getPassword());
	}

	@Test
	public void emptyHeaders() {
		assertTrue(req.getHeaderNames().isEmpty());
	}

	@Test
	public void addNullHeader() {
		req.addHeader(null, null);
		assertEquals("", req.getHeader(""));
	}

	@Test
	public void addOneHeader() {
		req.addHeader("key", "value");
		assertEquals(1, req.getHeaderNames().size());
		assertEquals("key", req.getHeaderNames().iterator().next());
		List<String> values = req.getHeaders("key");
		assertEquals(1, values.size());
		assertEquals("value", values.get(0));
		assertEquals("value", req.getHeader("key"));
		assertEquals("value", req.getCombinedHeaders("key"));
	}

	@Test
	public void addTwoHeaders() {
		req.addHeader("key", "value1");
		req.addHeader("key", "value2");
		assertEquals(1, req.getHeaderNames().size());
		assertEquals("key", req.getHeaderNames().iterator().next());
		List<String> values = req.getHeaders("key");
		assertEquals(2, values.size());
		assertEquals("value1", values.get(0));
		assertEquals("value2", values.get(1));
		assertEquals("value1", req.getHeader("key"));
		assertEquals("value1,value2", req.getCombinedHeaders("key"));
	}

	@Test
	public void getNonExistingHeader() {
		assertNull(req.getHeader("foo"));
		assertNull(req.getHeaders("foo"));
	}

	@Test
	public void emptyParams() {
		assertTrue(req.getParamNames().isEmpty());
	}

	@Test
	public void addParam() {
		req.addParam("key", "value");
		assertEquals(1, req.getParamNames().size());
		assertEquals("key", req.getParamNames().iterator().next());
		List<String> values = req.getParams("key");
		assertEquals(1, values.size());
		assertEquals("value", values.get(0));
		assertEquals("value", req.getParam("key"));
	}

	@Test
	public void emptyBody() {
		assertNull(req.getBody());
	}

	@Test
	public void body() {
		byte[] b = new byte[] { 1, 2, 3 };
		req.setBody(b);
		byte[] r = req.getBody();
		assertEquals(3, r.length);
		assertEquals(1, r[0]);
		assertEquals(2, r[1]);
		assertEquals(3, r[2]);
	}

	@Test
	public void bodyAsUTF8String() throws IOException {
		byte[] b = "Hello".getBytes(StandardCharsets.UTF_8);
		req.setBody(b);
		assertEquals("Hello", req.getBodyAsString("UTF-8"));
	}

	@Test
	public void bodyAsUTF16String() throws IOException {
		byte[] b = "Hello".getBytes(StandardCharsets.UTF_16);
		req.setBody(b);
		assertEquals("Hello", req.getBodyAsString("UTF-16"));
	}

	@Test
	public void defaultIsGet() {
		assertEquals("GET", req.getMethod());
	}

	@Test
	public void setIllegalMethod() {
		assertThrows(IllegalArgumentException.class, () -> {
			req.setMethod("FOO");
		});
	}

	@Test
	public void post() {
		req.setMethod("POST");
		assertEquals("POST", req.getMethod());
	}

	@Test
	public void encode() {
		assertEquals("foo", req.urlEncode("foo"));
		assertEquals("foo+bar", req.urlEncode("foo bar"));
	}

	@Test
	public void encodeWithException() {
		assertThrows(IllegalArgumentException.class, () -> {
			req.urlEncode("foo bar", "this charset does not exist");
		});
	}

	@Test
	public void baseUri() {
		assertNull(req.getBaseUri());
		String baseUri = "http://somehost.com";
		req.setBaseUri(baseUri);
		assertEquals(baseUri, req.getBaseUri());
	}

	@Test
	public void uriWithoutQueryString() {
		String baseUri = "http://somehost.com";
		req.setBaseUri(baseUri);
		assertEquals(baseUri, req.getUri());
	}

	@Test
	public void uriWithueryString() {
		String baseUri = "http://somehost.com";
		req.setBaseUri(baseUri);
		req.addParam("key", "value 1");
		req.addParam("key", "value 2");
		req.addParam("empty", "");
		assertEquals(baseUri + "?key=value+1&key=value+2&empty", req.getUri());
	}
}
