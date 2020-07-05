package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.TunnelException;

public class EvaluationExceptionTest {

	@Test
	public void withFunction() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void withCause() {
		EvaluationException ee = new EvaluationException(new NullPointerException("baz"));
		assertEquals("java.lang.NullPointerException: baz", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void withMessageAndCause() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void withEmptyMessageAndCause() {
		EvaluationException ee = new EvaluationException("", new NullPointerException("baz"));
		assertEquals("java.lang.NullPointerException: baz", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void add() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals(0, ee.getAdded());
		ee.add(new StackTraceElement("class", "method", "file", 42));
		assertTrue(ee.getAdded() > 0);
		StackTraceElement[] stackTrace = ee.getStackTrace();
		assertEquals("class", stackTrace[0].getClassName());
	}
	
	@Test
	public void withTunnelException() {
		EvaluationException e = new EvaluationException("foo", new TunnelException(new EvaluationException("tunnelled")));
		assertEquals("tunnelled", e.getMessage());
	}
}
