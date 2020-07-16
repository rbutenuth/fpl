package de.codecentric.fpl.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.LazyExpression;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.parser.Position;

/**
 * Simple tests for different classes in the data package.
 */
public class SimpleDataTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullSymbol() {
        new Symbol(null);
    }

    @Test
    public void emptySymbol() {
        Symbol s = new Symbol("");
        assertEquals("", s.getName());
    }

    @Test
    public void goodSymbol1() {
        Symbol a = new Symbol("a");
        assertEquals("a", a.getName());
        assertEquals(Position.UNKNOWN, a.getPosition());
    }

    @Test
    public void goodSymbol2() {
        Symbol a = new Symbol("a", new Position("name", 1, 1), Collections.emptyList());
        assertEquals("a", a.getName());
        assertEquals(new Position("name", 1, 1), a.getPosition());
        assertEquals(new Position("name", 1, 1).hashCode(), a.getPosition().hashCode());
        assertFalse(new Position("name", 2, 1).equals(a.getPosition()));
        assertFalse(new Position("name", 1, 2).equals(a.getPosition()));
        assertFalse(new Position("x", 1, 1).equals(a.getPosition()));
    }

    @Test
    public void positionEquals() {
        assertFalse(new Position("bla", 1, 1).equals(null));
        Object o = "sonstwas"; // to avoid warning "unlikely argument"
        assertFalse(new Position("bla", 1, 1).equals(o));
    }

    @Test(expected = NullPointerException.class)
    public void badPosition1() {
        new Position(null, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badPosition2() {
        new Position("bla", -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badPosition3() {
        new Position("bla", 1, -1);
    }

    @Test
    public void symbolEquals() {
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
    public void symbolHashCode() {
        Symbol a = new Symbol("a");
        assertEquals(a.hashCode(), new Symbol("a").hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void badLazyExpression() {
        new LazyExpression(null, null);
    }

    @Test
    public void lazyExpression() throws EvaluationException {
        LazyExpression e = new LazyExpression(new Scope("test"), FplInteger.valueOf(42));
        assertEquals(42, ((FplInteger)e.getOriginalExpression()).getValue());
        assertEquals(42, ((FplInteger)e.evaluate(null)).getValue());
        assertEquals("integer", e.typeName());
    }
}
