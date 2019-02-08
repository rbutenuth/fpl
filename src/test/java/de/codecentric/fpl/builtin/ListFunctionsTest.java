package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.LazyExpression;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Tests for list functions.
 */
public class ListFunctionsTest extends AbstractFplTest {

    @Test
    public void testQuote() throws Exception {
        FplList l = (FplList) evaluate("quote", "'(1 2 3)");
        assertEquals(3, l.size());

        Symbol s = (Symbol) evaluate("symbol", "'fasel");
        assertEquals("fasel", s.getName());

        Symbol s2 = (Symbol) evaluate("symbol", "(quote bla)");
        assertEquals("bla", s2.getName());
    }

    @Test
    public void testIndirectQuote() throws Exception {
    	evaluate("my-qoute", "(defun myquote (x) (quote x))");
    	LazyExpression lazy = (LazyExpression) evaluate("call", "(myquote (+ 3 4))");
    	FplInteger result = (FplInteger) lazy.evaluate(null);
    	assertEquals(FplInteger.valueOf(7), result);
    }
    
    @Test
    public void testSize() throws Exception {
        FplInteger i = (FplInteger) evaluate("size", "(size '(1 2 3))");
        assertEquals(3, i.getValue());
    }

    @Test
    public void testEmptyList() throws Exception {
        FplList l = (FplList) evaluate("list", "(list)");
        assertEquals(0, l.size());
        Iterator<FplValue> iter = l.iterator();
        assertFalse(iter.hasNext());

        l = (FplList) evaluate("list", "'()");
        assertEquals(0, l.size());
        iter = l.iterator();
        assertFalse(iter.hasNext());
    }

    @Test
    public void testList() throws Exception {
        FplList l = (FplList) evaluate("list", "(list 1 2 (+ 1 2))");
        assertEquals(3, l.size());
        // Elements via get
        for (int i = 0; i < l.size(); i++) {
            FplInteger li = (FplInteger) l.get(i);
            assertEquals(1 + i, li.getValue());
        }
        // Elements via iterator
        int i = 0;
        for (FplValue e : l) {
            FplInteger li = (FplInteger) e;
            assertEquals(1 + i++, li.getValue());
        }
        assertEquals(3, i);
    }

    @Test
    public void testFirst() throws Exception {
        FplInteger li = (FplInteger) evaluate("first", "(first '(1 2 3))");
        assertEquals(1, li.getValue());
    }

    @Test
    public void testLast() throws Exception {
        FplInteger li = (FplInteger) evaluate("last", "(last '(1 2 3))");
        assertEquals(3, li.getValue());
    }

    @Test
    public void testRest() throws Exception {
        FplList list = (FplList) evaluate("rest", "(rest '(1 2 3))");
        assertEquals(2, list.size());
        for (int i = 0; i < list.size(); i++) {
            FplInteger li = (FplInteger) list.get(i);
            assertEquals(2 + i, li.getValue());
        }
    }

    @Test
    public void testCons() throws Exception {
        FplList list = (FplList) evaluate("cons", "(cons 1 '(2 3))");
        assertEquals(3, list.size());
        for (int i = 0; i < list.size(); i++) {
            FplInteger li = (FplInteger) list.get(i);
            assertEquals(1 + i, li.getValue());
        }

        FplList front = (FplList) evaluate("front", "'(1 2 3)");
        scope.put("front", front);
        FplList back = (FplList) evaluate("back", "'(4 5 6)");
        scope.put("back", back);
        FplList both = (FplList) evaluate("append", "(append front back)");
        assertEquals(6, both.size());
        for (int i = 0; i < both.size(); i++) {
            FplInteger li = (FplInteger) both.get(i);
            assertEquals(1 + i, li.getValue());
        }
        FplList cons = (FplList) evaluate("cons", "(cons 42 front)");
        assertEquals(4, cons.size());
        long[] values = new long[] { 42, 1, 2, 3 };
        for (int i = 0; i < cons.size(); i++) {
            FplInteger li = (FplInteger) cons.get(i);
            assertEquals(values[i], li.getValue());
        }
    }

    @Test
    public void testAdd() throws Exception {
        FplList list = (FplList) evaluate("add", "(add '(1 2) 3)");
        assertEquals(3, list.size());
        for (int i = 0; i < list.size(); i++) {
            FplInteger li = (FplInteger) list.get(i);
            assertEquals(1 + i, li.getValue());
        }

        list = (FplList) evaluate("add", "(add '() 3)");
        assertEquals(1, list.size());
        for (int i = 0; i < list.size(); i++) {
            FplInteger li = (FplInteger) list.get(i);
            assertEquals(3 + i, li.getValue());
        }

        FplList front = (FplList) evaluate("front", "'(1 2 3)");
        scope.put("front", front);
        FplList back = (FplList) evaluate("back", "'(4 5 6)");
        scope.put("back", back);
        FplList both = (FplList) evaluate("append", "(append front back)");
        assertEquals(6, both.size());
        for (int i = 0; i < both.size(); i++) {
            FplInteger li = (FplInteger) both.get(i);
            assertEquals(1 + i, li.getValue());
        }
        FplList add = (FplList) evaluate("add", "(add front 42)");
        assertEquals(4, add.size());
        long[] values = new long[] { 1, 2, 3, 42 };
        for (int i = 0; i < add.size(); i++) {
            FplInteger li = (FplInteger) add.get(i);
            assertEquals(values[i], li.getValue());
        }
    }

    @Test
    public void testAppend() throws Exception {
        FplList front = (FplList) evaluate("front", "'(1 2 3)");
        scope.put("front", front);
        FplList back = (FplList) evaluate("back", "'(4 5 6)");
        scope.put("back", back);
        FplList both = (FplList) evaluate("append", "(append front back)");
        assertEquals(6, both.size());
        for (int i = 0; i < both.size(); i++) {
            FplInteger li = (FplInteger) both.get(i);
            assertEquals(1 + i, li.getValue());
        }
        FplList both2 = (FplList) evaluate("append2", "(append front '(42 43))");
        assertEquals(5, both2.size());
        long[] values = new long[] { 1, 2, 3, 42, 43 };
        for (int i = 0; i < both2.size(); i++) {
            FplInteger li = (FplInteger) both2.get(i);
            assertEquals(values[i], li.getValue());
        }
    }

    @Test
    public void testMap() throws Exception {
        FplList input = (FplList) evaluate("input", "'(1 2 3)");
        scope.put("input", input);
        evaluate("square", "(defun square (x) (* x x))");
        FplList result = (FplList) evaluate("map-test", "(map input square)");
        for (int i = 1; i <= 3; i++) {
        	FplInteger r = (FplInteger) result.get(i - 1);
        	assertEquals(i * i, r.getValue());
        }
    }
    
    @Test
    public void testMapNotAFunction() throws Exception {
        FplList input = (FplList) evaluate("input", "'(1 2 3)");
        scope.put("input", input);
        evaluate("square", "(put square 4)");
        try {
        	evaluate("map-test", "(map input square)");
        	fail("should not be reached.");
        } catch (EvaluationException expected) {
        	assertEquals("Second parameter of map must be function.", expected.getMessage());
        }
    }
    
    @Test(expected = EvaluationException.class)
    public void testTooManyParameters() throws Exception {
    	evaluate("pair", "(defun pair (a b) (list a b))");
    	FplList list = (FplList) evaluate("incorrect call", "(pair 1 2 3)");
    	assertEquals(2, list.size());
    }
}
