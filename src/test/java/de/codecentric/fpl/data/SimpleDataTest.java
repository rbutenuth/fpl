package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.LazyExpression;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * Simple tests for different classes in the data package.
 */
public class SimpleDataTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullSymbol() {
        new Symbol(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySymbol() {
        new Symbol("");
    }

    @Test
    public void testGoodSymbol1() {
        Symbol a = new Symbol("a");
        assertEquals("a", a.getName());
        assertEquals(Position.UNKNOWN, a.getPosition());
    }

    @Test
    public void testGoodSymbol2() {
        Symbol a = new Symbol("a", new Position("name", 1, 1), Collections.emptyList());
        assertEquals("a", a.getName());
        assertEquals(new Position("name", 1, 1), a.getPosition());
        assertEquals(new Position("name", 1, 1).hashCode(), a.getPosition().hashCode());
        assertFalse(new Position("name", 2, 1).equals(a.getPosition()));
        assertFalse(new Position("name", 1, 2).equals(a.getPosition()));
        assertFalse(new Position("x", 1, 1).equals(a.getPosition()));
    }

    @Test
    public void testPositionEquals() {
        assertFalse(new Position("bla", 1, 1).equals(null));
        Object o = "sonstwas"; // to avoid warning "unlikely argument"
        assertFalse(new Position("bla", 1, 1).equals(o));
    }

    @Test(expected = NullPointerException.class)
    public void testBadPosition1() {
        new Position(null, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadPosition2() {
        new Position("bla", -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadPosition3() {
        new Position("bla", 1, -1);
    }

    @Test
    public void testSymbolEquals() {
        Symbol a = new Symbol("a");
        assertFalse(a.equals(null));
        Object obj = "sonstwas"; // to avoid warning "unlikely argument"
		assertFalse(a.equals(obj));
        assertFalse(a.equals(new Symbol("b")));
        assertTrue(a.equals(a));
        assertTrue(a.equals(new Symbol("a")));
        // position is not part of equals
        assertTrue(a.equals(new Symbol("a", new Position("bla", 1, 1), Collections.emptyList())));
    }

    @Test
    public void testSymbolHashCode() {
        Symbol a = new Symbol("a");
        assertEquals(a.hashCode(), new Symbol("a").hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testBadLazyExpression() {
        new LazyExpression(null, null);
    }

    @Test
    public void testLazyExpression() throws EvaluationException {
        LazyExpression e = new LazyExpression(new Scope(), FplInteger.valueOf(42));
        assertEquals(42, ((FplInteger)e.getOriginalExpression()).getValue());
        assertEquals(42, ((FplInteger)e.evaluate(null)).getValue());
    }

    @Test
    public void testLazyEvaluationExpression() throws EvaluationException {
        LazyExpression expr = new LazyExpression(new Scope(), new FplList(new FplValue[] { FplInteger.valueOf(42)}));
        String message = null;
        try {
            expr.evaluate(null);
            fail("should not be reached");
        } catch (EvaluationException e) {
            message = e.getMessage();
        }
        try {
            expr.evaluate(null);
            fail("should not be reached");
        } catch (EvaluationException e) {
            assertEquals(message, e.getMessage());
        }
    }

}
