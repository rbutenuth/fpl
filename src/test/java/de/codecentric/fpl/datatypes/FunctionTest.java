package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

public class FunctionTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNullName() throws Exception {
		new TestFunction(null, false);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyName() throws Exception {
		new TestFunction("", false);
	}
	
	@Test
	public void testPositionUnknown() throws Exception {
		Function f = new TestFunction("foo", false);
		assertEquals(Position.UNKNOWN, f.getPosition());
	}
	
	@Test
	public void testPositionNull() throws Exception {
		Function f = new TestFunction(null, "foo", false, new String[0]);
		assertEquals(Position.UNKNOWN, f.getPosition());
		assertEquals("foo", f.getName());
		assertEquals(0, f.getMinimumNumberOfParameters());
		assertEquals(0, f.getParameterNames().length);
	}
	
	@Test
	public void testEvaluateEmptyListToBoolean() throws Exception {
		Function f = new TestFunction(null, "foo", false, new String[0]);
		assertFalse(f.evaluateToBoolean(null, new FplList(new FplValue[0])));
	}
	
	@Test
	public void testEvaluateNonEmptyListToBoolean() throws Exception {
		Function f = new TestFunction(null, "foo", false, new String[0]);
		Scope scope = new Scope();
		scope.put("x", new FplList(new FplValue[] { new FplString("baz") }));
		assertTrue(f.evaluateToBoolean(scope, new Symbol("x")));
	}
	
	@Test
	public void testPositionKnown() throws Exception {
		Function f = new TestFunction(new Position("foo.fpl", 1, 42), "foo", false);
		assertEquals(new Position("foo.fpl", 1, 42), f.getPosition());
	}
}
