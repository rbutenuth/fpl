package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.MapScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.parser.Position;

public class ObjectTest {
	MapScope outer;
	FplObject object;
	
	@Before
	public void before() throws EvaluationException {
		outer = new MapScope();
		object = new FplObject(new Position("object-test", 2, 3));
	}

	@After
	public void after() {
		outer = null;
		object = null;
	}
	
	@Test
	public void testEmptyObject() throws EvaluationException {
		assertTrue(outer.allKeys().isEmpty());
		assertNull(object.get("foo"));
		assertEquals("object-test", object.getPosition().getName());
		object.evaluate(outer);
		assertTrue(outer == object.getNext());
		assertTrue(object.getInitCode().isEmpty());
		assertNull(object.get("foo"));
	}
	
	@Test
	public void testAddInitcode() throws EvaluationException {
		object.addInitCodeValue(FplInteger.valueOf(1));
		object.addInitCodeValue(FplInteger.valueOf(2));
		object.addInitCodeValue(FplInteger.valueOf(3));
		List<FplValue> initCode = object.getInitCode();
		assertEquals(3, initCode.size());
		assertEquals(1, ((FplInteger)initCode.get(0)).getValue());
		assertEquals(2, ((FplInteger)initCode.get(1)).getValue());
		assertEquals(3, ((FplInteger)initCode.get(2)).getValue());
	}

	@Test
	public void testEvaluate() throws EvaluationException {
		AtomicInteger count = new AtomicInteger(0);
		object.addInitCodeValue(new FplValue() {
			
			@Override
			public FplValue evaluate(Scope scope) throws EvaluationException {
				count.incrementAndGet();
				assertTrue(object == scope);
				return this;
			}
		});
		assertTrue(object == object.evaluate(outer));
		assertEquals(1, count.get());
		assertTrue(outer == object.getNext());
		object.evaluate(outer);
		assertTrue(outer == object.getNext());
		assertEquals(1, count.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullName() {
		new FplObject(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullScope() throws EvaluationException {
		object.evaluate(null);
	}
	
	@Test
	public void testSeal() {
		assertFalse(object.isSealed());
		object.setSealed(true);
		assertTrue(object.isSealed());
		try {
			object.put("foo", FplInteger.valueOf(1));
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Scope is sealed", e.getMessage());
		}

	}
	
	@Test(expected = EvaluationException.class)
	public void testNullKey() throws EvaluationException {
		object.put(null, FplInteger.valueOf(0));
	}
	
	@Test(expected = EvaluationException.class)
	public void testEmptyKey() throws EvaluationException {
		object.put("", FplInteger.valueOf(0));
	}
	
	@Test
	public void testPut() throws EvaluationException {
		FplInteger one = FplInteger.valueOf(1);
		FplInteger two = FplInteger.valueOf(2);
		object.put("one", one);
		object.put("two", two);
		assertEquals(one, object.get("one"));
		object.put("one", null);
		assertNull(object.get("one"));
	}
}
