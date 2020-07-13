package de.codecentric.fpl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class HttpResponseTest {
	private HttpResponse res = new HttpResponse();
	
	@Test
	public void defaultIs200() {
		assertEquals(200, res.getStatusCode());
		assertNull(res.getStatusMessage());
	}

	@Test
	public void status() {
		res.setStatusCode(201);
		res.setStatusMessage("created");
		assertEquals(201, res.getStatusCode());
		assertEquals("created", res.getStatusMessage());
	}
}
