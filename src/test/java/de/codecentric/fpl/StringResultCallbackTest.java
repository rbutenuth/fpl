package de.codecentric.fpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import de.codecentric.fpl.datatypes.FplString;

public class StringResultCallbackTest {

	@Test
	public void successWithString() {
		StringResultCallback src = new StringResultCallback(true);
		assertEquals("", src.toString());
		assertTrue(src.handleSuccess(new FplString("huhu")));
		assertEquals("\"huhu\"", src.toString().trim());
	}
	
	@Test
	public void successWithNull() {
		StringResultCallback src = new StringResultCallback(true);
		assertTrue(src.handleSuccess(null));
		assertEquals("nil", src.toString().trim());
	}
	
	@Test
	public void continueOnException() {
		StringResultCallback src = new StringResultCallback(true);
		assertTrue(src.handleException(new NullPointerException()));
	}
	
	@Test
	public void stopOnException() {
		StringResultCallback src = new StringResultCallback(false);
		assertFalse(src.handleException(new NullPointerException()));
	}
	
	@Test
	public void successAndOutputStream() throws IOException {
		StringResultCallback src = new StringResultCallback(true);
		assertTrue(src.handleSuccess(new FplString("huhu")));
		OutputStream os = src.getOutputStream();
		os.write("complete".getBytes(StandardCharsets.UTF_8));
		os.write("incomplete".getBytes(StandardCharsets.UTF_8), 0, 2);
		os.write((byte)'x');
		os.flush();
		os.close();
		assertEquals("\"huhu\"" + System.lineSeparator() + "completeinx", src.toString());
	}
}
