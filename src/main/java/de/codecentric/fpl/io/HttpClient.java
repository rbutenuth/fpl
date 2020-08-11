package de.codecentric.fpl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

/**
 * A simple, non-streaming HTTP client.
 */
public class HttpClient {

	public HttpResponse execute(HttpRequest req) throws IOException {
		HttpResponse res = new HttpResponse();

		URL url = new URL(req.getUri());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (req.getBasicAuth() != null) {
			con.setRequestProperty("Authorization", req.getBasicAuth());
		}
		for (String headerName : req.getHeaderNames()) {
			con.setRequestProperty(headerName, req.getCombinedHeaders(headerName));
		}
		con.setRequestMethod(req.getMethod());
		byte[] body = req.getBody();
		if (body != null) {
			con.setDoOutput(true);
			try (OutputStream os = con.getOutputStream()) {
				os.write(body);
			}
		}
		int status = con.getResponseCode();
		res.setStatusCode(status);
		res.setStatusMessage(con.getResponseMessage());
		for (Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			for (String value : entry.getValue()) {
				res.addHeader(key, value);
			}
		}
		if (status < 400) {
			try (InputStream is = con.getInputStream()) {
				res.setBody(StreamUtil.readStreamToBytes(is));
			}
		}
		return res;
	}
}
