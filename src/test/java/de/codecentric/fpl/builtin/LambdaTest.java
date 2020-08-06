package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Tests for the function interpreter.
 */
public class LambdaTest extends AbstractFplTest {

	private AbstractFunction lambda;

	@Before
	@Override
	public void setUp() throws ScopeException, EvaluationException {
		super.setUp();
		lambda = (AbstractFunction) scope.get("lambda");
	}

	@After
	@Override
	public void tearDown() {
		lambda = null;
		super.tearDown();
	}

	@Test
	public void coverConstructor() {
		new Lambda(); // just to cover default constructor
	}
	
	@Test(expected = EvaluationException.class)
	public void evaluateToList() throws Exception {
		evaluate("cons", "(add-front 1 2)");
	}

	@Test
	public void lambda() throws Exception {
		assertEquals(1, lambda.getMinimumNumberOfParameters());
		assertTrue(lambda.isVararg());
	}

	@Test(expected = EvaluationException.class)
	public void duplicateParameterName() throws Exception {
		evaluate("duplicate", "(def-function test (a a) a)");
	}

	@Test(expected = EvaluationException.class)
	public void duplicateDefinition() throws Exception {
		evaluate("duplicate", "(def-function test (a b) a)");
		evaluate("duplicate", "(def-function test (a b) b)");
	}

	@Test
	public void noArgs() throws Exception {
		FplValue argList = parser("no args", "()").next();
		FplValue body = parser("body", "42").next();
		AbstractFunction f = (AbstractFunction) lambda.call(scope, new FplValue[] { argList, body });
		assertEquals(0, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("(lambda () 42)", f.toString());
		FplInteger i = (FplInteger) f.call(scope, new FplValue[0]);
		assertEquals(42, i.getValue());
	}

	@Test
	public void evalNoArgs() throws Exception {
		AbstractFunction f = (AbstractFunction) evaluate("no args", "(lambda () 42)");
		assertEquals(0, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("(lambda () 42)", f.toString());
	}

	@Test
	public void twoArgs() throws Exception {
		FplValue argList = parser("two args", "(a b)").next();
		FplValue body = parser("body", "42").next();
		AbstractFunction f = (AbstractFunction) lambda.call(scope, new FplValue[] { argList, body });
		assertEquals(2, f.getMinimumNumberOfParameters());
		assertFalse(f.isVararg());
		assertEquals("(lambda (a b) 42)", f.toString());
	}

	@Test
	public void twoArgsPlusVarArgs() throws Exception {
		FplValue argList = parser("two args", "(a b c...)").next();
		FplValue body = parser("body", "42").next();
		AbstractFunction f = (AbstractFunction) lambda.call(scope, new FplValue[] { argList, body });
		assertEquals(2, f.getMinimumNumberOfParameters());
		assertTrue(f.isVararg());
		assertEquals("(lambda (a b c...) 42)", f.toString());
	}

	@Test
	public void argsFromSymbol() throws Exception {
		FplList args = FplList.fromValues(new Symbol("a"), new Symbol("b)"));
		scope.define(new Symbol("args"), args);
		Function f = (Function)evaluate("lambda", "(lambda args (+ a b))");
	}

	@Test
	public void parameterWithComment() throws Exception {
		FplLambda test = (FplLambda) evaluate("duplicate", "(def-function test (\n; nonsense comment\n a b) a)");
		assertEquals("nonsense comment", test.getParameterComment(0));
	}

	@Test(expected = EvaluationException.class)
	public void lambdaStringInsteadArgumentList() throws Exception {
		evaluate("no args", "(lambda \"foo\" 42)");
	}

	@Test(expected = EvaluationException.class)
	public void defFunctionStringInsteadArgumentList() throws Exception {
		evaluate("no args", "(def-function bad \"foo\" 42)");
	}

	@Test(expected = EvaluationException.class)
	public void lambdaArgumentNotSymbol() throws Exception {
		evaluate("no args", "(lambda (40) 42)");
	}

	@Test
	public void lambdaString() throws Exception {
		FplLambda f = (FplLambda) evaluate("lambda", "(lambda (a b c...) 42 43)");
		assertEquals(2, f.getMinimumNumberOfParameters());
		assertTrue(f.isVararg());
		assertEquals("function", f.typeName());
		assertEquals("lambda", f.getName());
		assertEquals("(lambda (a b c...) 42 43)", f.toString());
	}

	@Test
	public void defFunctionTwoCodeLists() throws Exception {
		evaluate("last-def", "(def-function letzt (a b) a b)");
		FplInteger i = (FplInteger) evaluate("last-run", "(letzt 5 6)");
		assertEquals(6, i.getValue());
	}

	@Test
	public void defFunctionSquare() throws Exception {
		FplLambda f = (FplLambda) evaluate("square", "(def-function square (x) (* x x))");
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
	public void defFunctionFactorial() throws Exception {
		FplLambda f = (FplLambda) evaluate("factorial", //
				"; compute n!\n" + //
						"(def-function factorial (; input\n n)\n" + //
						"   (if-else (le n 1)\n" + //
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
	public void varArgsFirst() throws Exception {
		FplLambda f = (FplLambda) evaluate("factorial", //
				"(def-function firstparam (a b...)\n" + //
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
	public void varArgsLast() throws Exception {
		FplLambda f = (FplLambda) evaluate("factorial", //
				"(def-function lastparam (a b...)\n" + //
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
	public void evaluateQuotedList() throws Exception {
		assertEquals(7, ((FplInteger) evaluate("eval", "(eval '(+ 3 4))")).getValue());
	}

	@Test
	public void exceptionChain() throws Exception {
		evaluate("fun1.fpl", "(def-function fun1 (a) (fun2 a))");
		evaluate("fun2.fpl", "(def-function fun2 (a) (fun3 a))");
		evaluate("fun3.fpl", "(def-function fun3 (a) (fun4 a))");
		evaluate("fun4.fpl", "(def-function fun4 (a) (x 1 0))");
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

	@Test
	public void typeOf() throws Exception {
		FplString f = (FplString) evaluate("type-of", "(type-of (def-function fun1 (a) (fun2 a)))");
		assertEquals("function", f.getContent());
		assertNull(evaluate("nil", "(type-of nil)"));
	}

	@Test
	public void duplicateUsedParameterIsEvalulatedOnlyOnce() throws Exception {
		ListResultCallback callback = evaluateResource("duplicate-used-parameter.fpl");
		List<FplValue> values = callback.getResults();
		FplValue value = values.get(3);
		assertEquals(FplInteger.valueOf(2), value);
	}
}
