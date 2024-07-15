package de.codecentric.fpl.io;

import static de.codecentric.fpl.ExceptionWrapper.wrapException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
	private static final String BASIC = "Basic ";

	private String basicAuthString;
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

	public void setBasicAuth(String user, String password) {
		byte[] bytes = (user + ":" + password).getBytes(StandardCharsets.UTF_8);
		basicAuthString = Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * @param authHeader Header value, including "Basic " as prefix.
	 */
	public void setBasicAuth(String authHeader) {
		if (authHeader.startsWith(BASIC)) {
			basicAuthString = authHeader.substring(BASIC.length());
		}
	}
	
	/**
	 * @return Encoded user and password, including "Basic" prefix or
	 *         <code>null</code> when user/password are not set.
	 */
	public String getBasicAuth() {
		return basicAuthString == null ? null : BASIC + basicAuthString;
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

	protected String urlEncode(String value) {
		return urlEncode(value, "UTF-8");
	}

	protected String urlEncode(String value, String encoding) {
		return wrapException(() -> {
			return URLEncoder.encode(value, encoding);
		});
	}
}
