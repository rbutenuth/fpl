package de.codecentric.fpl.io;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpRequest extends HttpEntity {
	private static Set<String> allowedMethods;
	static {
		allowedMethods = new HashSet<>();
		allowedMethods.add("GET");
		allowedMethods.add("PUT");
		allowedMethods.add("POST");
		allowedMethods.add("DELETE");
		allowedMethods.add("PATCH");
	}
	private String baseUri;
	private String method;
	private Map<String, List<String>> params;

	public HttpRequest() {
		method = "GET";
		params = new LinkedHashMap<>();
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		if (!allowedMethods.contains(method)) {
			throw new IllegalArgumentException("Unsupported method: " + method);
		}
		this.method = method;
	}

	/**
	 * @return URI without query string
	 */
	public String getBaseUri() {
		return baseUri;
	}

	/**
	 * @param baseUri URI without query string
	 */
	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getUri() {
		StringBuilder sb = new StringBuilder(baseUri);
		if (!params.isEmpty()) {
			sb.append('?');
			boolean first = true;
			for (Entry<String, List<String>> entry : params.entrySet()) {
				String key = entry.getKey();
				for (String value : entry.getValue()) {
					if (!first) {
						sb.append('&');
					}
					first = false;
					sb.append(urlEncode(key));
					if (!value.isEmpty()) {
						sb.append('=').append(urlEncode(value));
					}
				}
			}
		}
		return sb.toString();
	}

	public Set<String> getParamNames() {
		return params.keySet();
	}

	public void addParam(String key, String value) {
		add(params, key, value);
	}

	public List<String> getParams(String key) {
		return getValues(params, key);
	}

	public String getParam(String key) {
		return getValue(params, key);
	}

	protected String urlEncode(String value) {
		return urlEncode(value, "UTF-8");
	}

	protected String urlEncode(String value, String encoding) {
		try {
			return URLEncoder.encode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("unknown encoding: " + encoding);
		}
	}
}
