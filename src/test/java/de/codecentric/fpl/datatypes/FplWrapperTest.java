package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;

public class FplWrapperTest extends AbstractFplTest {

	public static class Inner {
		public Object returnNull() {
			return null;
		}

		public Byte returnByte() {
			return Byte.valueOf((byte) 3);
		}

		public Short returnShort() {
			return Short.valueOf((short) 255);
		}

		public Integer returnInteger() {
			return Integer.valueOf(1024);
		}

		public Long returnLong() {
			return Long.valueOf(4_000_000_000L);
		}

		public FplValue returnFplValue() {
			return FplInteger.valueOf(42);
		}

		public String returnString() {
			return "Hello world!";
		}

		public Character returnCharacter() {
			return 'a';
		}

		public Boolean returnFalse() {
			return Boolean.FALSE;
		}

		public Boolean returnTrue() {
			return Boolean.TRUE;
		}
		
		public Float returnFloat() {
			return 3.14f;
		}

		public Double returnDouble() {
			return 2.78;
		}

		public Map<String, String> returnHashMap() {
			Map<String, String> map = new HashMap<>();
			map.put("foo", "bar");
			return map;
		}
		
		public void methodWithException() {
			throw new NullPointerException("nil");
		}
	}

	public static class MissingNoArgConstructor {
		public MissingNoArgConstructor(String foo) {
			// nothing to do
		}
	}
	
	public static class PrivateConstructor {
		private PrivateConstructor() {
			// nothing to do
		}
	}
	
	public static abstract class AbstractClass {
		// nothing needed
	}
	
	public static class ConstructorException {
		public ConstructorException() {
			throw new IllegalArgumentException("bumm");
		}
	}
	
	public FplWrapperTest() {
		super(FplWrapperTest.class);
	}

	@Test(expected = NullPointerException.class)
	public void testNull() {
		new FplWrapper((Object)null);
	}
	
	@Test
	public void testNoArgArrayListWrapper() throws Exception {
		ListResultCallback callback = evaluateResource("java-list-wrapper.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		FplWrapper wrapper = (FplWrapper) values.get(0);
		assertEquals("wrapper(java.util.ArrayList)", wrapper.typeName());
		ArrayList<?> list = (ArrayList<?>) wrapper.getInstance();
		assertEquals(0, list.size());
		FplInteger size = (FplInteger) values.get(1);
		assertEquals(0, size.getValue());
	}

	@Test
	public void testIntegerArgArrayListWrapper() throws Exception {
		FplWrapper wrapper = (FplWrapper) evaluate("array-list", "(java-instance \"java.util.ArrayList\" 10)");
		assertEquals("wrapper(java.util.ArrayList)", wrapper.typeName());
		ArrayList<?> list = (ArrayList<?>) wrapper.getInstance();
		assertEquals(0, list.size());
	}
	
	@Test
	public void testWrapValues() throws Exception {
		ListResultCallback callback = evaluateResource("java-wrap-values.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(14, values.size());
		assertNull(values.get(1));
		assertEquals(3, ((FplInteger)values.get(2)).getValue());
		assertEquals(255, ((FplInteger)values.get(3)).getValue());
		assertEquals(1024, ((FplInteger)values.get(4)).getValue());
		assertEquals(4_000_000_000L, ((FplInteger)values.get(5)).getValue());
		assertEquals(42, ((FplInteger)values.get(6)).getValue());
		assertEquals("Hello world!", ((FplString)values.get(7)).getContent());
		assertEquals("a", ((FplString)values.get(8)).getContent());
		assertNull(values.get(9));
		assertEquals(1, ((FplInteger)values.get(10)).getValue());
		assertEquals(3.14, ((FplDouble)values.get(11)).getValue(), 0.00001);
		assertEquals(2.78, ((FplDouble)values.get(12)).getValue(), 0.00001);
		FplObject w = (FplObject) values.get(13);
		assertEquals("bar", ((FplString)w.get("foo")).getContent());
	}

	@Test
	public void testNotString() throws Exception {
		try {
			evaluate("not-list", "(java-instance 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Expect string or symbol, but got integer", e.getMessage());
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

	@Test
	public void testMissingNoArgConstructor() throws Exception {
		try {
			evaluate("wrong-cons", "(java-instance\"de.codecentric.fpl.datatypes.FplWrapperTest$MissingNoArgConstructor\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No matching method with name de.codecentric.fpl.datatypes.FplWrapperTest$MissingNoArgConstructor found", e.getMessage());
		}
	}

	@Test
	public void testPrivateConstructor() throws Exception {
		try {
			evaluate("wrong-cons", "(java-instance\"de.codecentric.fpl.datatypes.FplWrapperTest$PrivateConstructor\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No matching method with name de.codecentric.fpl.datatypes.FplWrapperTest$PrivateConstructor found", e.getMessage());
		}
	}

	@Test
	public void testAbstractClass() throws Exception {
		try {
			evaluate("abstract-class", "(java-instance\"de.codecentric.fpl.datatypes.FplWrapperTest$AbstractClass\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("java.lang.InstantiationException", e.getMessage());
		}
	}

	@Test
	public void testConstructorWithException() throws Exception {
		try {
			evaluate("cons-exception", "(java-instance\"de.codecentric.fpl.datatypes.FplWrapperTest$ConstructorException\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("bumm", e.getMessage());
		}
	}

	@Test
	public void testBadMethod() throws Exception {
		try {
			evaluate("bad-method", "((java-instance \"de.codecentric.fpl.datatypes.FplWrapperTest$Inner\") iDontKnowThisMethod)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No matching method with name iDontKnowThisMethod found", e.getMessage());
		}
	}
	
	@Test
	public void testMethodWithException() throws Exception {
		try {
			evaluate("method-with-exception", "((java-instance de.codecentric.fpl.datatypes.FplWrapperTest$Inner) methodWithException)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("nil", e.getMessage());
		}
	}
	
	@Test
	public void testStaticCallString() throws Exception {
		FplInteger i = (FplInteger) evaluate("static", "((java-class \"java.lang.Integer\") valueOf 10)");
		assertEquals(FplInteger.valueOf(10), i);
	}
	
	@Test
	public void testStaticCallSymbol() throws Exception {
		FplInteger i = (FplInteger) evaluate("static", "((java-class java.lang.Integer) valueOf 10)");
		assertEquals(FplInteger.valueOf(10), i);
	}
	
	@Test
	public void testStaticNeitherStringNotSymbol() throws Exception {
		try {
			evaluate("static-with-exception", "(java-class 42)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Expect string or symbol, but got integer", e.getMessage());
		}
	}
	
	@Test
	public void testStaticClassNotFound() throws Exception {
		try {
			evaluate("static-with-exception", "(java-class foo.bar.UknownClass)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("unknown class: foo.bar.UknownClass", e.getMessage());
		}
	}
}
