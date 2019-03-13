package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;

public class FplWrapperTest extends AbstractFplTest {

	public static class Inner {
		public Object returnNull() {
			return null;
		}
	}
	
	public FplWrapperTest() {
		super(FplWrapperTest.class);
	}

	@Test
	public void testArrayListWrapper() throws Exception {
		ListResultCallback callback = evaluateResource("java-list-wrapper.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		FplWrapper wrapper = (FplWrapper) values.get(0);
		assertEquals("wrapper(java.util.ArrayList)", wrapper.typeName());
		ArrayList<?> list = (ArrayList<?>) wrapper.getInstance();
		assertEquals(0, list.size());
		FplInteger size = (FplInteger)values.get(1);
		assertEquals(0, size.getValue());
	}

	@Test
	public void testWrapNil() throws Exception {
		ListResultCallback callback = evaluateResource("java-wrap-nil.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		assertNull(values.get(1));
	}
	
	@Test
	public void testNotString() throws Exception {
		try {
			evaluate("not-list", "(java-instance 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Expect string, but got integer", e.getMessage());
		}
	}
	
	@Test
	public void testUnknownClass() throws Exception {
		try {
			evaluate("unknown", "(java-instance \"i.hope.nobody.creates.this.class\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("unknown class: i.hope.nobody.creates.this.class", e.getMessage());
		}
	}
}
