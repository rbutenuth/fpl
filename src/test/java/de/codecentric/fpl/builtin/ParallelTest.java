package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.list.FplList;

public class ParallelTest extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new Loop();
	}

	@Test
	public void threadPoolSize() throws Exception {
		FplInteger oldSize = (FplInteger) evaluate("thread-pool-size", "(thread-pool-size 17)");
		assertTrue(oldSize.getValue() > 0);
	}

	@Test
	public void parallel() throws Exception {
		FplList list = (FplList) evaluate("parallel", "(parallel  (+ 3 4) (* 3 4) (- 3 4) )");
		assertEquals(3, list.size());
		assertEquals(FplInteger.valueOf(7), list.get(0));
		assertEquals(FplInteger.valueOf(12), list.get(1));
		assertEquals(FplInteger.valueOf(-1), list.get(2));
	}

	@Test
	public void parallelRecursive() throws Exception {
		evaluate("fibonacci", "(def-function par-fib (n)\n" //
				+ "	(if-else (le n 2)\n" //
				+ "		1\n" //
				+ "		(reduce \n" //
				+ "			(lambda (acc value i) (+ acc value))\n" //
				+ "			0\n" + "			(parallel (par-fib (- n 1)) (par-fib (- n 2)))\n" //
				+ "		)\n" //
				+ "	)\n" //
				+ ")");
		FplInteger fib10 = (FplInteger)evaluate("call-fib", "(par-fib 10)");
		assertEquals(55, fib10.getValue());
	}

	@Test
	public void parallelWithEvaluatonException() throws Exception {
		try {
			evaluate("parallel", "(parallel  (+ 3 4) (* 3 4) (/ 3 0) )");
			fail("Exception missing");
		} catch (EvaluationException e) {
			assertEquals("/ by zero", e.getMessage());
		}
	}

	@Test
	public void parallelMap() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");
		FplList squares = (FplList) evaluate("parallel-map", "(parallel-map square '(1 2 3 4 5 6 7 8 9 10))");
		assertEquals(10, squares.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

	@Test
	public void parallelMapLambdaThrowsException() throws Exception {
		evaluate("fail", "(def-function square (x) (/ 1 0))");
		try {
			evaluate("parallel-map", "(parallel-map square '(1 2 3 4))");
			fail("should not be reached.");
		} catch (EvaluationException e) {
			assertEquals("/ by zero", e.getMessage());
		}
	}

	@Test
	public void parallelForEachOfList() throws Exception {
		evaluate("parallel-for-each", "(def-function fun (x) (+ x x))");
		FplInteger result = (FplInteger) evaluate("parallel-for-each", "(parallel-for-each fun '(1 2 3 4))");
		assertEquals(FplInteger.valueOf(8), result);
	}

	@Test
	public void parallelForEachOfEmptyList() throws Exception {
		evaluate("parallel-for-each", "(def-function fun (x) (+ x x))");
		FplInteger result = (FplInteger) evaluate("parallel-for-each", "(parallel-for-each fun '())");
		assertNull(result);
	}

	@Test
	public void parallelForEachThrowsException() throws Exception {
		evaluate("fail", "(def-function fail (x) (/ 1 0))");
		try {
			evaluate("parallel-for-each", "(parallel-for-each fail '(1 2 3 4))");
			fail("should not be reached.");
		} catch (EvaluationException e) {
			assertEquals("/ by zero", e.getMessage());
		}
	}

	@Test
	public void createFuture() throws Exception {
		evaluate("create-future", "(put future (create-future (* 6 7)))");
		FplInteger result = (FplInteger) evaluate("future", "(future)");
		assertEquals(FplInteger.valueOf(42), result);
	}

	@Test
	public void createFutureWithEvaluatonException() throws Exception {
		evaluate("create-future", "(put future (create-future (/ 42 0)))");
		try {
			evaluate("future", "(future)");
			fail("Exception missing");
		} catch (EvaluationException e) {
			assertEquals("/ by zero", e.getMessage());
		}
	}
}
