package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.AbstractListTest;
import de.codecentric.fpl.datatypes.list.FplList;

public class LoopTest extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new Loop();
	}
	
	@Test
	public void mapOfSmallList() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");;
		FplInteger square = (FplInteger) evaluate("call", "(square 4)");
		assertEquals(FplInteger.valueOf(16), square);
		FplList squares = (FplList) evaluate("map", "(map square '(1 2 3 4))");
		assertEquals(4, squares.size());
		for (int i = 1; i <= 4; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

	@Test
	public void mapOfLargeList() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");;
		FplInteger square = (FplInteger) evaluate("call", "(square 4)");
		assertEquals(FplInteger.valueOf(16), square);
		FplList squares = (FplList) evaluate("map", "(map square '(1 2 3 4 5 6 7 8 9 10))");
		assertEquals(10, squares.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

    @Test
    public void testMapNotAList() throws Exception {
        evaluate("input", "(def input 4)");
		evaluate("square", "(def-function square (x) (* x x))");;
        try {
        	evaluate("map-test", "(map square input)");
        	fail("should not be reached.");
        } catch (EvaluationException expected) {
        	assertEquals("Not a list: 4", expected.getMessage());
        }
    }
    
    @Test
    public void testMapNotALambda() throws Exception {
        evaluate("input", "(def input '(1 2 3))");
        evaluate("square", "(put square 4)");
        try {
        	evaluate("map-test", "(map square input)");
        	fail("should not be reached.");
        } catch (EvaluationException expected) {
        	assertEquals("Not a lambda: 4", expected.getMessage());
        }
    }
    
    @Test
    public void testMapLambdaThrowsException() throws Exception {
    	evaluate("fail", "(def-function square (x) (/ 1 0))");;
        try {
    		evaluate("map", "(map square '(1 2 3 4))");
        	fail("should not be reached.");
        } catch (EvaluationException expected) {
        	assertEquals("java.lang.ArithmeticException: / by zero", expected.getMessage());
        }
    }
    
	@Test
	public void iterateWithLambda() throws Exception {
		FplLambda lambda = (FplLambda) evaluate("lambda", "(lambda (x) x)");
		Iterator<FplValue> iter = AbstractListTest.create(0, 10).lambdaIterator(scope, lambda);
		for (int i = 0; i < 10; i++) {
			assertTrue(iter.hasNext());
			FplValue value = iter.next();
			assertEquals(FplInteger.valueOf(i), value);
		}
		assertFalse(iter.hasNext());
	}
	
}
