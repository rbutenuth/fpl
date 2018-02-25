package de.codecentric.fpl.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class SimpleHttpClient {

	public static void main(String[] args) throws Exception {
		boolean lastBlockOnly = args.length == 4 && "lastBlockOnly".equals(args[4]);

		try (InputStream is = new FileInputStream(args[3])) {
			System.out.println(post(args[0], args[1], args[2], is, lastBlockOnly));
		}
	}

	public static String post(String baseUrl, String user, String password, InputStream fis, boolean lastBlockOnly)
			throws IOException {
		if (lastBlockOnly) {
			baseUrl = baseUrl + "?lastBlockOnly";
		}

		URL url = new URL(baseUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		byte[] bytes = (user + ":" + password).getBytes();
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(bytes);
		con.setRequestProperty("Authorization", basicAuth);
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		try (OutputStream os = con.getOutputStream()) {
			int b = fis.read();
			while (b != -1) {
				os.write(b);
				b = fis.read();
			}
		} finally {
			fis.close();
		}

		int responseCode = con.getResponseCode();
		if (responseCode != 200) {
			return "Failure: " + responseCode;
		}
		StringBuilder response = new StringBuilder();
		try (Reader rd = new BomAwareReader(con.getInputStream())) {
			int ch = rd.read();
			while (ch != -1) {
				response.append((char) ch);
				ch = rd.read();
			}
		}
		return response.toString();
	}
}
