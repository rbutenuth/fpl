package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnWrapperTest {

	@Test
	public void testInstanciateUnWrapper() {
		new UnWrapper(); // cover no-op constructor
	}
	
	@Test
	public void testUnwrapFplWrapper() {
		FplWrapper w = new FplWrapper(Integer.valueOf(42));
		Object u = UnWrapper.unwrap(w);
		assertEquals(Integer.valueOf(42), u);
	}
}
