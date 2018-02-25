package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;

/**
 * Tests for the function interpreter.
 */
public class ArithmeticTest extends AbstractFplTest {

    @Test
    public void testUnaryMinusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("minus", "( - 3)");
        assertEquals(-3, i.getValue());
    }

    @Test
    public void testUnaryMinusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("minus", "( - 3.14)");
        assertEquals(-3.14, d.getValue(), 0.00001);
    }

    @Test(expected = EvaluationException.class)
    public void testStringFirst() throws Exception {
        evaluate("minus", "( - \"bla\" 3.14)");
    }

    @Test(expected = EvaluationException.class)
    public void testStringSecond() throws Exception {
        evaluate("minus", "( - 3 \"bla\" 3.14)");
    }

    @Test
    public void testIntegerPlusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(+ 3 4)");
        assertEquals(7, i.getValue());
    }

    @Test
    public void testIntegerModulusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(% 13 4)");
        assertEquals(1, i.getValue());
    }

    @Test
    public void testIntegerPlusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(+ 3 4.0)");
        assertEquals(7.0, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoublePlusInteger() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(+ 3.0 4)");
        assertEquals(7.0, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoublePlusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(+ 3.0 4.0)");
        assertEquals(7.0, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoubleTimesDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(* 3.0 4.0)");
        assertEquals(12.0, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoublePowerDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(^ 3.0 4.0)");
        assertEquals(81.0, d.getValue(), 0.0000001);
    }
}
