package de.codecentric.fpl.io;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExecuteViaHttp {

	/**
	 * Simple command line client to execute one or several expressions.
	 * @param args
	 * <ol>
	 *   <li>baseUrl</li>
	 *   <li>user;
	 *   <li>password;
	 *   <li>file.getAbsolutePath();
	 *   <li>"lastBlockOnly";
	 * </ol>
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		boolean lastBlockOnly = args.length == 5 && "lastBlockOnly".equals(args[4]);

		try (InputStream is = new FileInputStream(args[3])) {
			System.out.println(post(args[0], args[1], args[2], is, lastBlockOnly));
		}
	}

	public static String post(String baseUrl, String user, String password, InputStream fis, boolean lastBlockOnly)
			throws IOException {
		HttpRequest req = new HttpRequest();
		if (lastBlockOnly) {
			req.addParam("lastBlockOnly", "");
		}

		req.setBaseUri(baseUrl);
		req.setBasicAuth(user, password);
		req.setMethod("POST");
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			int b = fis.read();
			while (b != -1) {
				os.write(b);
				b = fis.read();
			}
			req.setBody(os.toByteArray());
		} finally {
			fis.close();
		}
		HttpResponse res = new HttpClient().execute(req);
		if (res.getStatusCode() != 200) {
			return "Failure: " + res.getStatusCode() + ", reason: " + res.getStatusMessage();
		}
		return res.getBodyAsString("UTF-8");
	}
}
