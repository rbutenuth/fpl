package de.codecentric.fpl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
			for (String value: entry.getValue()) {
				res.addHeader(key, value);
			}
		}
		if (status < 300) {
			int totalSize = 0;
			List<Chunk> chunks = new ArrayList<>();
			try (InputStream is = con.getInputStream()) {
				boolean eof = false;
				do {
					Chunk chunk = new Chunk();
					chunk.size = is.read(chunk.buffer);
					if (chunk.size > 0) {
						chunks.add(chunk);
						totalSize += chunk.size;
					} else {
						eof = true;
					}
				} while (!eof);
			}
			byte[] buffer = new byte[totalSize];
			int pos = 0;
			for (Chunk ch : chunks) {
				System.arraycopy(ch.buffer, 0, buffer, pos, ch.size);
				pos += ch.size;
			}
			res.setBody(buffer);
		}
		return res;
	}

	private static class Chunk {
		private byte[] buffer = new byte[8 * 1024];
		private int size;
	}
}
