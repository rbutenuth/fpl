package de.codecentric.fpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.fpl.datatypes.FplString;

public class StringResultCallbackTest {

	@Test
	public void testReset() {
		StringResultCallback src = new StringResultCallback(true);
		assertEquals("", src.toString());
		assertTrue(src.handleSuccess(new FplString("huhu")));
		assertEquals("\"huhu\"", src.toString().trim());
		src.reset();
		assertEquals("", src.toString());
	}
	
	@Test
	public void testNullValue() {
		StringResultCallback src = new StringResultCallback(true);
		assertTrue(src.handleSuccess(null));
		assertEquals("nil", src.toString().trim());
	}
	
	@Test
	public void testContinueOnException() {
		StringResultCallback src = new StringResultCallback(true);
		assertTrue(src.handleException(new NullPointerException()));
	}
	
	@Test
	public void testStopOnException() {
		StringResultCallback src = new StringResultCallback(false);
		assertFalse(src.handleException(new NullPointerException()));
	}
}
