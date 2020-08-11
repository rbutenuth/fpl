package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Tests for the function interpreter.
 */
public class ArithmeticTest extends AbstractFplTest {

    @Test
    public void round() throws Exception {
    	assertLongEquals(0, evaluate("nil", "(round nil)"));
    	assertLongEquals(0, evaluate("nil", "(round 0.3)"));
    	assertLongEquals(1, evaluate("nil", "(round 0.5)"));
    	assertLongEquals(0, evaluate("nil", "(round -0.3)"));
    	assertLongEquals(4, evaluate("nil", "(round 4)"));
    }
    
    @Test
    public void toInteger() throws Exception {
    	assertLongEquals(0, evaluate("nil", "(to-integer nil)"));
    	assertLongEquals(0, evaluate("nil", "(to-integer 0.3)"));
    	assertLongEquals(0, evaluate("nil", "(to-integer 0.9)"));
    	assertLongEquals(0, evaluate("nil", "(to-integer -0.3)"));
    	assertLongEquals(4, evaluate("nil", "(to-integer 4)"));
    }

    private void assertLongEquals(long expected, FplValue actual) throws Exception {
    	assertEquals(expected, ((FplInteger)actual).getValue());
    }
    
    @Test
    public void unaryMinusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("minus", "( - 3)");
        assertEquals(-3, i.getValue());
    }

    @Test
    public void unaryMinusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("minus", "( - 3.14)");
        assertEquals(-3.14, d.getValue(), 0.00001);
        assertEquals("double", d.typeName());
        assertEquals("-3.14", d.toString());
    }

    @Test(expected = EvaluationException.class)
    public void stringFirst() throws Exception {
        evaluate("minus", "( - \"bla\" 3.14)");
    }

    @Test(expected = EvaluationException.class)
    public void stringSecond() throws Exception {
        evaluate("minus", "( - 3 \"bla\" 3.14)");
    }

    @Test
    public void integerPlusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(+ 3 4)");
        assertEquals(7, i.getValue());
    }

    @Test
    public void plusWithLongList() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(+ 3 4 3 4 3 4 3 4 3 4 3 4 3 4)");
        assertEquals(49, i.getValue());
    }

    @Test
    public void plusWithLongListFromTwoParts() throws Exception {
        FplInteger i = (FplInteger)evaluate("plus", "(eval (append '(+) '(3 4 3 4 3 4 3 4 3 4 3 4 3 4 3 4)))");
        assertEquals(56, i.getValue());
    }

    @Test
    public void integerModulusInteger() throws Exception {
        FplInteger i = (FplInteger)evaluate("mod", "(% 13 4)");
        assertEquals(1, i.getValue());
    }

    @Test
    public void integerDivideByInteger() throws Exception {
    	FplInteger i = (FplInteger)evaluate("divide", "(/ 6 3)");
        assertEquals(2, i.getValue());
    }

    @Test
    public void integerPowInteger() throws Exception {
    	FplInteger i = (FplInteger)evaluate("pow", "(** 2 8)");
        assertEquals(256, i.getValue());
    }

    @Test
    public void integerPlusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(+ 3 4.0)");
        assertEquals(7.0, d.getValue(), 0.0000001);
    }

    @Test
    public void doublePlusInteger() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(+ 3.0 4)");
        assertEquals(7.0, d.getValue(), 0.0000001);
    }

    @Test
    public void doublePlusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("plus", "(+ 3.0 4.0)");
        assertEquals(7.0, d.getValue(), 0.0000001);
    }

    @Test
    public void doubleMinusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("minus", "(- 4.0 3.0)");
        assertEquals(1.0, d.getValue(), 0.0000001);
    }

    @Test
    public void doubleDivideByDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("divide", "(/ 6.0 3.0)");
        assertEquals(2.0, d.getValue(), 0.0000001);
    }

    @Test
    public void doubleModulusDouble() throws Exception {
        FplDouble d = (FplDouble)evaluate("mod", "(% 13.0 4.0)");
        assertEquals(1, d.getValue(), 0.0000001);
    }

    @Test
    public void doubleTimesDouble() throws Exception {
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
