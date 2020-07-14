package de.codecentric.fpl.io;

public interface HttpRequestHandler {

	/**
	 * @param request Request
	 * @return Response
	 * @throws Exception If thronw, server willt answer with status 500.
	 */
	public HttpResponse handleRequest(HttpRequest request) throws Exception;
}
