package de.codecentric.fpl.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;

public class EvaluationExceptionTest {

	@Test
	public void withMessageAndCause() {
		EvaluationException ee = new EvaluationException("foo", new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
		assertEquals(0, ee.getId());
	}

	@Test
	public void withId() {
		EvaluationException ee = new EvaluationException("foo", 42, new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
		assertEquals(42, ee.getId());
	}

	@Test
	public void withCause() {
		EvaluationException ee = new EvaluationException(new NullPointerException("baz"));
		assertEquals("baz", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
	}

	@Test
	public void withMessageAndCauseAndId() {
		EvaluationException ee = new EvaluationException("foo", 42, new NullPointerException("baz"));
		assertEquals("foo", ee.getMessage());
		assertEquals("baz", ee.getCause().getMessage());
		assertEquals(42, ee.getId());
	}

	@Test
	public void withEmptyMessageAndCause() {
		EvaluationException ee = new EvaluationException("", new NullPointerException("baz"));
		assertEquals("baz", ee.getMessage());
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
}
