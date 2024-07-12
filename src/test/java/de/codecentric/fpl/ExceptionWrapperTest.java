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

}
