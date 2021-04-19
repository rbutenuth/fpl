package de.codecentric.fpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplInteger;

public class ListResultCallbackTest {
	private ListResultCallback callback;
	
	@BeforeEach
	public void setUp() throws Exception {
		callback = new ListResultCallback();
	}

	@AfterEach
	public void tearDown() throws Exception {
		callback = null;
	}

	@Test
	public void empty() {
		assertFalse(callback.hasException());
		assertNull(callback.getException());
		assertTrue(callback.getResults().isEmpty());
	}

	@Test
	public void exception() {
		assertFalse(callback.handleException(new Exception("bam")));
		assertTrue(callback.hasException());
		assertEquals("bam", callback.getException().getMessage());
		assertTrue(callback.getResults().isEmpty());
	}

	@Test
	public void oneResult() {
		assertTrue(callback.handleSuccess(FplInteger.valueOf(42)));
		assertFalse(callback.hasException());
		assertNull(callback.getException());
		assertEquals(1, callback.getResults().size());
		assertEquals(42, ((FplInteger)callback.getResults().get(0)).getValue());
	}
}
