package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class EvaluationExceptionTest {

	@Test
	public void testWithFunction() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void testWithCause() {
		EvaluationException ee = new EvaluationException(new NullPointerException("baz"));
		assertEquals("java.lang.NullPointerException: baz", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void testWithMessageAndCause() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void testWithEmptyMessageAndCause() {
		EvaluationException ee = new EvaluationException("", new NullPointerException("baz"));
		assertEquals("java.lang.NullPointerException: baz", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void testAdd() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals(0, ee.getAdded());
		ee.add(new StackTraceElement("class", "method", "file", 42));
		assertTrue(ee.getAdded() > 0);
		StackTraceElement[] stackTrace = ee.getStackTrace();
		assertEquals("class", stackTrace[0].getClassName());
	}
}
