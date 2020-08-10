package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

public class FunctionTest {

	@Test(expected = IllegalArgumentException.class)
	public void nullName() throws Exception {
		new TestFunction(null, false);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void emptyName() throws Exception {
		new TestFunction("", false);
	}
	
	@Test
	public void positionUnknown() throws Exception {
		AbstractFunction f = new TestFunction("foo", false);
		assertEquals(Position.UNKNOWN, f.getPosition());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullComment() throws Exception {
		AbstractFunction f = new TestFunction("foo", false);
		f.setParameterComment(0, null);
	}
	
	@Test
	public void positionNull() throws Exception {
		AbstractFunction f = new TestFunction(null, "foo", false, new String[0]);
		assertEquals(Position.UNKNOWN, f.getPosition());
		assertEquals("foo", f.getName());
		assertEquals(0, f.getMinimumNumberOfParameters());
		assertEquals(0, f.getParameterNames().length);
	}
	
	@Test
	public void evaluateEmptyListToBoolean() throws Exception {
		assertFalse(AbstractFunction.evaluateToBoolean(null, FplList.fromValues(new FplValue[0])));
	}
	
	@Test
	public void evaluateNonEmptyListToBoolean() throws Exception {
		Scope scope = new Scope("test");
		scope.put("x", FplList.fromValues(new FplValue[] { new FplString("baz") }));
		assertTrue(AbstractFunction.evaluateToBoolean(scope, new Symbol("x")));
	}
	
	@Test
	public void positionKnown() throws Exception {
		AbstractFunction f = new TestFunction(new Position("foo.fpl", 1, 42), "foo", false);
		assertEquals(new Position("foo.fpl", 1, 42), f.getPosition());
	}
}
