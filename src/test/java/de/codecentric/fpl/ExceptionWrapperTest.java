package de.codecentric.fpl;

import static org.junit.jupiter.api.Assertions.*;
import static de.codecentric.fpl.ExceptionWrapper.wrapException;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class ExceptionWrapperTest {

	@Test
	void testSuccess() {
		assertEquals("hello", wrapException(() -> "hello"));
	}

	@Test
	void testException() {
		assertThrows(EvaluationException.class, () -> wrapException(() -> {throw new IOException("bäm");}));
	}

	@Test
	void testEvaluationException() {
		Throwable t = assertThrows(EvaluationException.class, () -> wrapException(new ThrowsEvaluationException()));
		assertEquals("bäm", t.getMessage());
	}

	@Test
	void testWrappedEvaluationException() {
		Throwable t = assertThrows(EvaluationException.class, () -> wrapException(new ThrowsExceptionWithEvaluationExceptionAsCause()));
		assertEquals("wrapped bäm", t.getMessage());
	}

	@Test
	void testIOException() {
		Throwable t = assertThrows(EvaluationException.class, () -> wrapException(new ThrowsIOException()));
		assertEquals("I/O", t.getMessage());
	}

	private static class ThrowsExceptionWithEvaluationExceptionAsCause implements Executable {
		@Override
		public void execute() throws Throwable {
			throw new Exception("fuu", new EvaluationException("wrapped bäm"));
		}
	}

	private static class ThrowsEvaluationException implements Executable {
		@Override
		public void execute() throws Throwable {
			throw new EvaluationException("bäm");
		}
	}

	private static class ThrowsIOException implements Executable {
		@Override
		public void execute() throws Throwable {
			throw new IOException("I/O");
		}
	}
}
