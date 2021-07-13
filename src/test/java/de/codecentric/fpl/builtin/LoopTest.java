package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class LoopTest extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new Loop();
	}

	@Test
	public void forEachOfList() throws Exception {
		evaluate("result", "(def result '())");
		evaluate("for-each", "(def-function fun (x) (set result (add-end result x)))");
		evaluate("for-each", "(for-each fun '(1 2 nil 4))");
		FplList result = (FplList) scope.get("result");
		assertEquals(4, result.size());
		assertEquals(1, ((FplInteger) result.get(0)).getValue());
		assertEquals(2, ((FplInteger) result.get(1)).getValue());
		assertNull(result.get(2));
		assertEquals(4, ((FplInteger) result.get(3)).getValue());
	}

	@Test
	public void forEachThrowsException() throws Exception {
		evaluate("fail", "(def-function fail (x) (/ 1 0))");
		try {
			evaluate("for-each", "(for-each fail '(1 2 3 4))");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("java.lang.ArithmeticException: / by zero", expected.getMessage());
		}
	}

	@Test
	public void filterOfList() throws Exception {
		evaluate("filter", "(def-function my-filter (x) (lt x 3))");
		FplList filtered = (FplList) evaluate("filter", "(filter my-filter '(1 2 3 4))");
		assertEquals(2, filtered.size());
		for (int i = 1; i <= 2; i++) {
			assertEquals(FplInteger.valueOf(i), filtered.get(i - 1));
		}
	}

	@Test
	public void filterWithLayzSymbol() throws Exception {
		evaluate("filter", "(def-function ge-filter (t src)\r\n" + //
				"	(filter (lambda (x) (ge x t)) src)\r\n" + //
				")");
		FplList filtered = (FplList) evaluate("filter", "(ge-filter (+ 3 2) '( 1 2 3 4 5 6 7 8 9 10))");
		assertEquals(6, filtered.size());
		for (int i = 5; i <= 10; i++) {
			assertEquals(FplInteger.valueOf(i), filtered.get(i - 5));
		}
	}

	@Test
	public void mapOfSmallList() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");
		FplList squares = (FplList) evaluate("map", "(map square '(1 2 3 4))");
		assertEquals(4, squares.size());
		for (int i = 1; i <= 4; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

	@Test
	public void mapOfLargeList() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");
		FplList squares = (FplList) evaluate("map", "(map square '(1 2 3 4 5 6 7 8 9 10))");
		assertEquals(10, squares.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

	@Test
	public void mapNotAList() throws Exception {
		evaluate("input", "(def input 4)");
		evaluate("square", "(def-function square (x) (* x x))");
		try {
			evaluate("map-test", "(map square input)");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("Not a list: 4", expected.getMessage());
		}
	}

	@Test
	public void mapNotALambda() throws Exception {
		evaluate("input", "(def input '(1 2 3))");
		evaluate("square", "(put square 4)");
		try {
			evaluate("map-test", "(map square input)");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("Not a function: 4", expected.getMessage());
		}
	}

	@Test
	public void mapLambdaThrowsException() throws Exception {
		evaluate("fail", "(def-function square (x) (/ 1 0))");
		try {
			evaluate("map", "(map square '(1 2 3 4))");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("java.lang.ArithmeticException: / by zero", expected.getMessage());
		}
	}

	@Test
	public void whileLoop() throws Exception {
		evaluate("empty", "(def li '())");
		evaluate("count", "(def count 0)");
		evaluate("while", "(while (lt count 10) (set li (add-front count li)) (set count (+ count 1)))");
		FplValue count = scope.get("count");
		assertEquals(FplInteger.valueOf(10), count);
		FplList list = (FplList) scope.get("li");
		assertEquals(10, list.size());
		for (int i = 0; i < 9; i++) {
			assertEquals(FplInteger.valueOf(i), list.get(9 - i));
		}
	}

	@Test
	public void reduce() throws Exception {
		FplValue sum = evaluate("reduce", "(reduce (lambda (acc value) (+ acc value)) 0 '(1 2 3 4 5 6))");
		assertEquals(FplInteger.valueOf(21), sum);
	}
}
