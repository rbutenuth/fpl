package de.codecentric.fpl.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class HttpEntity {
	private Map<String, List<String>> headers;
	private byte[] body;
	
	public HttpEntity() {
		headers = new LinkedHashMap<>();
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
	 * @param body Body as String
	 * @param encoding Used to convert String into bytes
	 * @throws UnsupportedEncodingException 
	 */
	public void setBody(String body, String encoding) throws UnsupportedEncodingException {
		this.body = body.getBytes(encoding);
	}

	public boolean hasBody() {
		return body != null && body.length > 0;
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
