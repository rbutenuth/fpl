package de.codecentric.fpl.datatypes;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;

public class FplWrapperTest extends AbstractFplTest {

	public FplWrapperTest() {
		super(FplWrapperTest.class);
	}

	@Test
	public void testWrapperConstructor() {
		new FplWrapper();
	}
}
