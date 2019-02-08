package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

/**
 * Tests for the function interpreter.
 */
public class LambdaTest extends AbstractFplTest {

	private Function lambda;

	@Before
	public void setUp() throws ScopeException, EvaluationException {
		super.setUp();
		lambda = (Function) scope.get("lambda");
	}

	@After
	public void tearDown() {
		lambda = null;
		super.tearDown();
	}

	@Test(expected = EvaluationException.class)
	public void testEvaluateToList() throws Exception {
		evaluate("cons", "(cons 1 2)");
	}

	@Test
	public void testLambda() throws Exception {
		assertEquals(2, lambda.getMinimumNumberOfParameters());
		assertFalse(lambda.isVararg());
	}

	@Test(expected = EvaluationException.class)
	public void testDuplicateParameterName() throws Exception {
		evaluate("duplicate", "(defun test (a a) a)");
	}

	@Test(expected = EvaluationException.class)
	public void testDuplicateDefinition() throws Exception {
		evaluate("duplicate", "(defun test (a b) a)");
		evaluate("duplicate", "(defun test (a b) b)");
	}

	@Test
	public void testNoArgs() throws Exception {
		FplValue argList = parser("no args", "()").next();
		FplValue body = parser("body", "42").next();
		Function f = (Function) lambda.call(scope, new FplValue[] { argList, body });
		assertEquals(0, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("(lambda () 42)", f.toString());
		FplInteger i = (FplInteger) f.call(scope, new FplValue[0]);
		assertEquals(42, i.getValue());
	}

	@Test
	public void testEvalNoArgs() throws Exception {
		Function f = (Function) evaluate("no args", "(lambda () 42)");
		assertEquals(0, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("(lambda () 42)", f.toString());
	}

	@Test
	public void testTwoArgs() throws Exception {
		FplValue argList = parser("two args", "(a b)").next();
		FplValue body = parser("body", "42").next();
		Function f = (Function) lambda.call(scope, new FplValue[] { argList, body });
		assertEquals(2, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("(lambda (a b) 42)", f.toString());
	}

	@Test
	public void testTwoPlusVarArgs() throws Exception {
		FplValue argList = parser("two args", "(a b c...)").next();
		FplValue body = parser("body", "42").next();
		Function f = (Function) lambda.call(scope, new FplValue[] { argList, body });
		assertEquals(2, f.getMinimumNumberOfParameters());
		assertTrue(f.isVararg());
		assertEquals("(lambda (a b c...) 42)", f.toString());
	}

	@Test(expected = EvaluationException.class)
	public void testLambdaStringInsteadArgumentList() throws Exception {
		evaluate("no args", "(lambda \"foo\" 42)");
	}

	@Test(expected = EvaluationException.class)
	public void testDefunStringInsteadArgumentList() throws Exception {
		evaluate("no args", "(defun bad \"foo\" 42)");
	}

	@Test(expected = EvaluationException.class)
	public void testDefunNameNotSymbol() throws Exception {
		evaluate("no args", "(defun \"foo\" () 42)");
	}

	@Test(expected = EvaluationException.class)
	public void testLambdaArgumentNotSymbol() throws Exception {
		evaluate("no args", "(lambda (40) 42)");
	}

	@Test
	public void testLambdaString() throws Exception {
		FplFunction f = (FplFunction) evaluate("lambda", "(lambda (a b c...) 42)");
		assertEquals(2, f.getMinimumNumberOfParameters());
		assertTrue(f.isVararg());
		assertEquals("lambda", f.getName());
		assertEquals("(lambda (a b c...) 42)", f.toString());
		Position p = f.getPosition();
		assertEquals("lambda", p.getName());
		assertEquals(1, p.getLine());
		assertEquals(2, p.getColumn());
	}

	@Test
	public void testDefunTwoCodeLists() throws Exception {
		evaluate("last-def", "(defun last (a b) a b)");
		FplInteger i = (FplInteger) evaluate("last-run", "(last 5 6)");
		assertEquals(6, i.getValue());
	}
	
	@Test
	public void testDefunSquare() throws Exception {
		FplFunction f = (FplFunction) evaluate("square", "(defun square (x) (* x x))");
		assertEquals(1, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("square", f.getName());
		assertTrue(f == scope.get("square"));
		assertEquals("(lambda (x) (* x x))", f.toString());
		FplInteger i = (FplInteger) evaluate("square i", "(square 2)");
		assertEquals(4, i.getValue());
		FplDouble d = (FplDouble) evaluate("square d", "(square 3.0)");
		assertEquals(9.0, d.getValue(), 0.000001);
	}

	@Test
	public void testDefunFactorial() throws Exception {
		FplFunction f = (FplFunction) evaluate("factorial", //
				"; compute n!\n" + //
						"(defun factorial (; input\n n)\n" + //
						"   (if (le n 1)\n" + //
						"       1\n" + //
						"       (* n (factorial (- n 1)))\n" + //
						"   )\n" + //
						")\n");
		assertEquals(1, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("factorial", f.getName());
		assertEquals("compute n!", f.getComment().get(0));
		assertEquals("input", f.getParameterComment(0));
		assertTrue(f == scope.get("factorial"));
		FplInteger i = (FplInteger) evaluate("factorial 5", "(factorial 5)");
		assertEquals(120, i.getValue());
	}

	@Test
	public void testVarArgsFirst() throws Exception {
		FplFunction f = (FplFunction) evaluate("factorial", //
				"(defun firstparam (a b...)\n" + //
						"   a\n" + //
						")\n");
		assertEquals(1, f.getMinimumNumberOfParameters());
		assertTrue(f.isVararg());
		assertEquals("firstparam", f.getName());
		assertTrue(f == scope.get("firstparam"));
		FplInteger i = (FplInteger) evaluate("first", "(firstparam 5 6 7)");
		assertEquals(5, i.getValue());
	}

	@Test
	public void testVarArgsLast() throws Exception {
		FplFunction f = (FplFunction) evaluate("factorial", //
				"(defun lastparam (a b...)\n" + //
						"   b\n" + //
						")\n");
		assertEquals(1, f.getMinimumNumberOfParameters());
		assertTrue(f.isVararg());
		assertEquals("lastparam", f.getName());
		assertTrue(f == scope.get("lastparam"));
		FplList list = (FplList) evaluate("last", "(lastparam 5 6 7)");
		assertEquals(2, list.size());
		assertEquals(FplInteger.valueOf(6), list.get(0));
		assertEquals(FplInteger.valueOf(7), list.get(1));
	}

	@Test
	public void testEvaluate() throws Exception {
		assertEquals(7, ((FplInteger) evaluate("eval", "(eval '(+ 3 4))")).getValue());
	}

	@Test
	public void testException() throws Exception {
		evaluate("fun1.fpl", "(defun fun1 (a) (fun2 a))");
		evaluate("fun2.fpl", "(defun fun2 (a) (fun3 a))");
		evaluate("fun3.fpl", "(defun fun3 (a) (fun4 a))");
		evaluate("fun4.fpl", "(defun fun4 (a) (x 1 0))");
		try {
			evaluate("bam", "(fun1 42)");
		} catch (EvaluationException e) {
			assertEquals("Not a function: null", e.getMessage());
			assertEquals(4, e.getAdded());
			StackTraceElement[] st = e.getStackTrace();
			assertEquals("fun4", st[0].getMethodName());
			assertEquals("fun4.fpl", st[0].getFileName());
			assertEquals(1, st[0].getLineNumber());
		}
	}

}
