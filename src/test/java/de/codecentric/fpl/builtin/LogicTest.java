package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplInteger;

/**
 * Tests for the function interpreter.
 */
public class LogicTest extends AbstractFplTest {

    @Test
    public void testNot() throws Exception {
        FplInteger i = (FplInteger)evaluate("not", "(not 0)");
        assertEquals(1, i.getValue());
        assertNull(evaluate("not", "(not 1)"));
    }

    @Test
    public void testAnd() throws Exception {
        FplInteger i = (FplInteger)evaluate("and", "(and 1 1)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("and", "(and 1)");
        assertEquals(1, i.getValue());
        assertNull(evaluate("and", "(and 1 0)"));
        assertNull(evaluate("and", "(and 1 1 0)"));
        assertNull(evaluate("and", "(and 0 1)"));
        assertNull(evaluate("and", "(and 0)"));
    }

    @Test
    public void testOr() throws Exception {
        FplInteger i = (FplInteger)evaluate("or", "(or 0 1)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("or", "(or 1 0)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("or", "(or 0 0 1 0)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("or", "(or 1)");
        assertEquals(1, i.getValue());
        assertNull(evaluate("or", "(or 0 0)"));
        assertNull(evaluate("or", "(or 0)"));
    }

    @Test
    public void testXor() throws Exception {
        FplInteger i = (FplInteger)evaluate("xor", "(or 0 1)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("xor", "(xor 1 0)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("xor", "(xor 0 0 1 0)");
        assertEquals(1, i.getValue());
        i = (FplInteger)evaluate("xor", "(xor 1)");
        assertEquals(1, i.getValue());
        assertNull(evaluate("xor", "(xor 0 0)"));
        assertNull(evaluate("xor", "(xor 1 1)"));
        assertNull(evaluate("xor", "(xor 0)"));
    }

    @Test
    public void testIsSymbol() throws Exception {
        assertEquals(1, ((FplInteger) evaluate("is-symbol", "(is-symbol 'x)")).getValue());
        assertNull(evaluate("is-symbol", "(is-symbol x)"));
        assertNull(evaluate("is-symbol", "(is-symbol (eval x))"));
        assertNull(evaluate("is-symbol", "(is-symbol 17)"));
        evaluate("assign", "(put a 'x)");
        assertEquals(1, ((FplInteger)evaluate("is-symbol", "(is-symbol a)")).getValue());
    }

    @Test
    public void testIsInteger() throws Exception {
        assertEquals(1, ((FplInteger) evaluate("is-integer", "(is-integer 1)")).getValue());
        assertNull(evaluate("is-integer", "(is-integer 1.5)"));
    }

    @Test
    public void testIsDouble() throws Exception {
        assertEquals(1, ((FplInteger) evaluate("is-double", "(is-double 1.5)")).getValue());
        assertNull(evaluate("is-double", "(is-double 1)"));
    }

    @Test
    public void testIsList() throws Exception {
        assertEquals(1, ((FplInteger) evaluate("is-list", "(is-list (list 1 2 3))")).getValue());
        assertNull(evaluate("is-list", "(is-list 1)"));
    }

    @Test
    public void testIsFunction() throws Exception {
        assertEquals(1, ((FplInteger) evaluate("is-function", "(is-function is-function)")).getValue());
        assertEquals(1, ((FplInteger) evaluate("is-function", "(is-function (lambda (x) (* x x)))")).getValue());
        assertNull(evaluate("is-function", "(is-function 1)"));
    }
}
