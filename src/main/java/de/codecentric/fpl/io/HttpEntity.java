package de.codecentric.fpl.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class HttpEntity {
	private String basicAuthString;
	private Map<String, List<String>> headers;
	private byte[] body;
	
	public HttpEntity() {
		headers = new LinkedHashMap<>();
	}

	public void setBasicAuth(String user, String password) {
		byte[] bytes = (user + ":" + password).getBytes(StandardCharsets.UTF_8);
		basicAuthString = Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * @return Encoded user and password, including "Basic" prefix or
	 *         <code>null</code> when user/password are not set.
	 */
	public String getBasicAuth() {
		return basicAuthString == null ? null : "Basic " + basicAuthString;
	}
	
	public String getUser() {
		if (basicAuthString == null) {
			return null;
		}
		byte[] bytes = Base64.getDecoder().decode(basicAuthString);
		String userColonPassword = new String(bytes, StandardCharsets.UTF_8);
		return userColonPassword.substring(0, userColonPassword.indexOf(':'));
	}
	
	public String getPassword() {
		if (basicAuthString == null) {
			return null;
		}
		byte[] bytes = Base64.getDecoder().decode(basicAuthString);
		String userColonPassword = new String(bytes, StandardCharsets.UTF_8);
		return userColonPassword.substring(userColonPassword.indexOf(':') + 1);
	}
	
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}
	
	public void addHeader(String key, String value) {
		add(headers, key, value);
	}
	
	public List<String> getHeaders(String key) {
		return getValues(headers, key);
	}
	
	public String getCombinedHeaders(String key) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String value : getValues(headers, key)) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			sb.append(value);
		}
		return sb.toString();
	}
	
	public String getHeader(String key) {
		return getValue(headers, key);
	}
	
	/**
	 * @param body The content, will not be copied.
	 */
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	/**
	 * @return The content, will not be copied.
	 */
	public byte[] getBody() {
		return body;
	}
	
	public String getBodyAsString(String encoding) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (encoding.equals("UTF-8")) {
			try (Reader rd = new BomAwareReader(new ByteArrayInputStream(body))) {
				int ch = rd.read();
				while (ch != -1) {
					sb.append((char)ch);
					ch = rd.read();
				}
			}
		} else {
			try (Reader rd = new InputStreamReader(new ByteArrayInputStream(body), encoding)) {
				int ch = rd.read();
				while (ch != -1) {
					sb.append((char)ch);
					ch = rd.read();
				}
			}
		}
		return sb.toString();
	}
	
	protected void add(Map<String, List<String>> map, String key, String value) {
		if (key == null) {
			key = "";
		}
		if (value == null) {
			value = "";
		}
		List<String> values = map.get(key);
		if (values == null) {
			values = new ArrayList<>();
			map.put(key, values);
		}
		values.add(value);
	}
	
	protected List<String> getValues(Map<String, List<String>> map, String key) {
		return map.get(key);
	}
	
	protected String getValue(Map<String, List<String>> map, String key) {
		List<String> values = map.get(key);
		if (values == null) {
			return null;
		} else {
			return values.get(0);
		}
	}
}
