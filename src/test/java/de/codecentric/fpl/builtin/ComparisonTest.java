package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.parser.ParseException;

/**
 * Tests for comparison operations.
 */
public class ComparisonTest extends AbstractFplTest {

	@Test(expected = EvaluationException.class)
    public void testCompareNull1() throws Exception {
        evaluateToBoolean("lt", "(lt nil 1)");
    }

    @Test(expected = EvaluationException.class)
    public void testCompareNull2() throws Exception {
        evaluateToBoolean("lt", "(lt 1 nil)");
    }

    @Test(expected = EvaluationException.class)
    public void testCompareNull3() throws Exception {
        evaluateToBoolean("lt", "(lt nil nil)");
    }

    @Test
    public void testLt() throws Exception {
        assertTrue(evaluateToBoolean("lt", "(lt 1 2)"));
        assertTrue(evaluateToBoolean("lt", "(lt 1.0 2.0)"));
        assertTrue(evaluateToBoolean("lt", "(lt 1.0 2)"));
        assertTrue(evaluateToBoolean("lt", "(lt 1 2.0)"));
        assertTrue(evaluateToBoolean("lt", "(lt \"a\" \"b\")"));

        assertFalse(evaluateToBoolean("lt", "(lt 1 1)"));
        assertFalse(evaluateToBoolean("lt", "(lt 1.0 1.0)"));
        assertFalse(evaluateToBoolean("lt", "(lt 1.0 1)"));
        assertFalse(evaluateToBoolean("lt", "(lt 1 1.0)"));
        assertFalse(evaluateToBoolean("lt", "(lt \"a\" \"a\")"));
        assertFalse(evaluateToBoolean("lt", "(lt 1 \"a\")"));
        assertFalse(evaluateToBoolean("lt", "(lt \"a\" 1)"));
        assertFalse(evaluateToBoolean("lt", "(lt \"a\" '())"));
        assertFalse(evaluateToBoolean("lt", "(lt '() \"a\")"));
    }

    @Test
    public void testGt() throws Exception {
        assertTrue(evaluateToBoolean("gt", "(gt 3 2)"));
        assertTrue(evaluateToBoolean("gt", "(gt 3.0 2.0)"));
        assertTrue(evaluateToBoolean("gt", "(gt 3.0 2)"));
        assertTrue(evaluateToBoolean("gt", "(gt 3 2.0)"));
        assertTrue(evaluateToBoolean("gt", "(gt \"c\" \"b\")"));

        assertFalse(evaluateToBoolean("gt", "(gt 1 1)"));
        assertFalse(evaluateToBoolean("gt", "(gt 1.0 1.0)"));
        assertFalse(evaluateToBoolean("gt", "(gt 1.0 1)"));
        assertFalse(evaluateToBoolean("gt", "(gt 1 1.0)"));
        assertFalse(evaluateToBoolean("gt", "(gt \"a\" \"a\")"));
        assertFalse(evaluateToBoolean("gt", "(gt \"a\" 1)"));
        assertFalse(evaluateToBoolean("gt", "(gt 1 \"a\")"));
        assertFalse(evaluateToBoolean("gt", "(gt \"a\" 1.0)"));
        assertFalse(evaluateToBoolean("gt", "(gt 1.0 \"a\")"));
        assertFalse(evaluateToBoolean("gt", "(gt '() \"a\")"));
        assertFalse(evaluateToBoolean("gt", "(gt \"a\" '())"));
        assertFalse(evaluateToBoolean("gt", "(gt  \"a\" '())"));
        assertFalse(evaluateToBoolean("gt", "(gt '() 1)"));
        assertFalse(evaluateToBoolean("gt", "(gt  1 '())"));
        assertFalse(evaluateToBoolean("gt", "(gt '() 1.0)"));
        assertFalse(evaluateToBoolean("gt", "(gt  1.0 '())"));
    }

    @Test
    public void testLe() throws Exception {
        assertTrue(evaluateToBoolean("le", "(le 1 2)"));
        assertTrue(evaluateToBoolean("le", "(le 1.0 2.0)"));
        assertTrue(evaluateToBoolean("le", "(le 1.0 2)"));
        assertTrue(evaluateToBoolean("le", "(le 1 2.0)"));
        assertTrue(evaluateToBoolean("le", "(le \"a\" \"b\")"));

        assertTrue(evaluateToBoolean("le", "(le 1 1)"));
        assertTrue(evaluateToBoolean("le", "(le 1.0 1.0)"));
        assertTrue(evaluateToBoolean("le", "(le 1.0 1)"));
        assertTrue(evaluateToBoolean("le", "(le 1 1.0)"));
        assertTrue(evaluateToBoolean("le", "(le \"a\" \"a\")"));

        assertFalse(evaluateToBoolean("le", "(le 2 1)"));
        assertFalse(evaluateToBoolean("le", "(le 2.0 1.0)"));
        assertFalse(evaluateToBoolean("le", "(le 2.0 1)"));
        assertFalse(evaluateToBoolean("le", "(le 2 1.0)"));
        assertFalse(evaluateToBoolean("le", "(le \"b\" \"a\")"));
        assertFalse(evaluateToBoolean("le", "(le 1 \"a\")"));
        assertFalse(evaluateToBoolean("le", "(le \"b\" 1)"));
    }

    @Test
    public void testGe() throws Exception {
        assertTrue(evaluateToBoolean("ge", "(ge 3 2)"));
        assertTrue(evaluateToBoolean("ge", "(ge 3.0 2.0)"));
        assertTrue(evaluateToBoolean("ge", "(ge 3.0 2)"));
        assertTrue(evaluateToBoolean("ge", "(ge 3 2.0)"));
        assertTrue(evaluateToBoolean("ge", "(ge \"c\" \"b\")"));

        assertTrue(evaluateToBoolean("ge", "(ge 1 1)"));
        assertTrue(evaluateToBoolean("ge", "(ge 1.0 1.0)"));
        assertTrue(evaluateToBoolean("ge", "(ge 1.0 1)"));
        assertTrue(evaluateToBoolean("ge", "(ge 1 1.0)"));
        assertTrue(evaluateToBoolean("ge", "(ge \"a\" \"a\")"));

        assertFalse(evaluateToBoolean("ge", "(ge 2 3)"));
        assertFalse(evaluateToBoolean("ge", "(ge 2.0 3.0)"));
        assertFalse(evaluateToBoolean("ge", "(ge 2.0 3)"));
        assertFalse(evaluateToBoolean("ge", "(ge 2 3.0)"));
        assertFalse(evaluateToBoolean("ge", "(ge \"b\" \"c\")"));
        assertFalse(evaluateToBoolean("ge", "(ge \"b\" 1)"));
        assertFalse(evaluateToBoolean("ge", "(ge 1 \"c\")"));
    }

    @Test
    public void testEq() throws Exception {
        assertTrue(evaluateToBoolean("eq", "(eq nil nil)"));
        assertTrue(evaluateToBoolean("eq", "(eq 3 3)"));
        assertTrue(evaluateToBoolean("eq", "(eq 3.0 3)"));
        assertTrue(evaluateToBoolean("eq", "(eq 3 3.0)"));
        assertTrue(evaluateToBoolean("eq", "(eq 3.0 3.0)"));
        assertTrue(evaluateToBoolean("eq", "(eq \"a\" \"a\")"));

        assertFalse(evaluateToBoolean("eq", "(eq nil 1)"));
        assertFalse(evaluateToBoolean("eq", "(eq nil 1.0)"));
        assertFalse(evaluateToBoolean("eq", "(eq nil \"a\")"));
        assertFalse(evaluateToBoolean("eq", "(eq 1 nil)"));
        assertFalse(evaluateToBoolean("eq", "(eq 1.0 nil)"));
        assertFalse(evaluateToBoolean("eq", "(eq \"a\" nil)"));
        assertFalse(evaluateToBoolean("eq", "(eq 3 4)"));
        assertFalse(evaluateToBoolean("eq", "(eq 3.0 4)"));
        assertFalse(evaluateToBoolean("eq", "(eq 3 4.0)"));
        assertFalse(evaluateToBoolean("eq", "(eq 3.0 4.0)"));
        assertFalse(evaluateToBoolean("eq", "(eq \"a\" \"b\")"));
        assertFalse(evaluateToBoolean("eq", "(eq \"a\" 1)"));
        assertFalse(evaluateToBoolean("eq", "(eq 1 \"a\")"));
        assertFalse(evaluateToBoolean("eq", "(eq \"a\" 1.0)"));
    }

    @Test
    public void testNe() throws Exception {
        assertFalse(evaluateToBoolean("ne", "(ne nil nil)"));
        assertFalse(evaluateToBoolean("ne", "(ne 3 3)"));
        assertFalse(evaluateToBoolean("ne", "(ne 3.0 3)"));
        assertFalse(evaluateToBoolean("ne", "(ne 3 3.0)"));
        assertFalse(evaluateToBoolean("ne", "(ne 3.0 3.0)"));
        assertFalse(evaluateToBoolean("ne", "(ne \"a\" \"a\")"));

        assertTrue(evaluateToBoolean("ne", "(ne nil 1)"));
        assertTrue(evaluateToBoolean("ne", "(ne nil 1.0)"));
        assertTrue(evaluateToBoolean("ne", "(ne nil \"a\")"));
        assertTrue(evaluateToBoolean("ne", "(ne 1 nil)"));
        assertTrue(evaluateToBoolean("ne", "(ne 1.0 nil)"));
        assertTrue(evaluateToBoolean("ne", "(ne \"a\" nil)"));
        assertTrue(evaluateToBoolean("ne", "(ne 3 4)"));
        assertTrue(evaluateToBoolean("ne", "(ne 3.0 4)"));
        assertTrue(evaluateToBoolean("ne", "(ne 3 4.0)"));
        assertTrue(evaluateToBoolean("ne", "(ne 3.0 4.0)"));
        assertTrue(evaluateToBoolean("ne", "(ne \"a\" \"b\")"));
        assertTrue(evaluateToBoolean("ne", "(ne \"a\" 1)"));
        assertTrue(evaluateToBoolean("ne", "(ne 1 \"a\")"));
        assertTrue(evaluateToBoolean("ne", "(ne \"a\" 1.0)"));
        assertTrue(evaluateToBoolean("ne", "(ne 1.0 \"a\")"));
    }

    private boolean evaluateToBoolean(String name, String input)  throws ParseException, IOException, EvaluationException {
        FplValue value = evaluate(name, input);
        if (value == null) {
            return false;
        } else {
            assertEquals(1, ((FplInteger)value).getValue());
            return true;
        }
    }
}
