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
        assertEquals("double", d.typeName());
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
    public void testPlusWithLongList() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(+ 3 4 3 4 3 4 3 4 3 4 3 4 3 4)");
        assertEquals(49, i.getValue());
    }

    @Test
    public void testPlusWithLongListFromTwoParts() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(eval (append '(+) '(3 4 3 4 3 4 3 4 3 4 3 4 3 4 3 4)))");
        assertEquals(56, i.getValue());
    }

    @Test
    public void testIntegerModulusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("mod", "(% 13 4)");
        assertEquals(1, i.getValue());
    }

    @Test
    public void testIntegerDivideByInteger() throws Exception {
    	FplInteger i = (FplInteger)evaluate("divide", "(/ 6 3)");
        assertEquals(2, i.getValue());
    }

    @Test
    public void testIntegerPowInteger() throws Exception {
    	FplInteger i = (FplInteger)evaluate("pow", "(** 2 8)");
        assertEquals(256, i.getValue());
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
    public void testDoubleMinusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("minus", "(- 4.0 3.0)");
        assertEquals(1.0, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoubleDivideByDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("divide", "(/ 6.0 3.0)");
        assertEquals(2.0, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoubleModulusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("mod", "(% 13.0 4.0)");
        assertEquals(1, d.getValue(), 0.0000001);
    }

    @Test
    public void testDoubleTimesDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("times", "(* 3.0 4.0)");
        assertEquals(12.0, d.getValue(), 0.0000001);
    }

    @Test
    public void doublePowerDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("power", "(** 3.0 4.0)");
        assertEquals(81.0, d.getValue(), 0.0000001);
    }
    
    @Test(expected = EvaluationException.class)
    public void doublePlusNullFails() throws Exception {
        evaluate("plus", "(+ 3.0 nil)");
    }
    
    @Test(expected = EvaluationException.class)
    public void nullPlusDoubleFails() throws Exception {
        evaluate("plus", "(+ nil 3.0)");
    }
}
