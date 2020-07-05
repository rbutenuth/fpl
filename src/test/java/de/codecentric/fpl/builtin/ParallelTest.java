package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.list.FplList;

public class ParallelTest  extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new Loop();
	}

	@Test
	public void parallel() throws Exception {
		FplList list = (FplList)evaluate("parallel", "(parallel  (+ 3 4) (* 3 4) (- 3 4) )");
		assertEquals(3, list.size());
		assertEquals(FplInteger.valueOf(7), list.get(0));
		assertEquals(FplInteger.valueOf(12), list.get(1));
		assertEquals(FplInteger.valueOf(-1), list.get(2));
	}

	@Test
	public void parallelWithEvaluatonException() throws Exception {
		try {
			evaluate("parallel", "(parallel  (+ 3 4) (* 3 4) (/ 3 0) )");
			fail("Exception missing");
		} catch (EvaluationException e) {
			assertEquals("java.lang.ArithmeticException: / by zero", e.getMessage());
		}
	}
}
