package de.codecentric.fpl.io;

public class HttpResponse extends HttpEntity {
	private int statusCode = 200;
	private String statusMessage;
	
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
}
