package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Tests for the function interpreter.
 */
public class LambdaTest extends AbstractFplTest {
	private AbstractFunction lambda;

	@BeforeEach
	@Override
	public void setUp() throws ScopeException, EvaluationException {
		super.setUp();
		lambda = (AbstractFunction) scope.get("lambda");
	}

	@AfterEach
	@Override
	public void tearDown() {
		lambda = null;
		super.tearDown();
	}

	@Test
	public void coverConstructor() {
		new Lambda(); // just to cover default constructor
	}

	@Test
	public void evaluateToList() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("cons", "(add-front 1 2)");
		});
	}

	@Test
	public void lambda() throws Exception {
		assertEquals(1, lambda.getMinimumNumberOfParameters());
		assertTrue(lambda.isVararg());
	}

	@Test
	public void duplicateParameterName() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("duplicate", "(def-function test (a a) a)");
		});
	}

	@Test
	public void duplicateDefinition() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("duplicate", "(def-function test (a b) a)");
			evaluate("duplicate", "(def-function test (a b) b)");
		});
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
		scope.define("args", args);
		AbstractFunction f = (AbstractFunction) evaluate("lambda", "(lambda args (+ a b))");
		assertEquals(2, f.getMinimumNumberOfParameters());
	}

	@Test
	public void parameterWithComment() throws Exception {
		FplLambda test = (FplLambda) evaluate("duplicate", "(def-function test (\n; nonsense comment\n a b) a)");
		assertEquals("nonsense comment", test.getParameterComment("a"));
	}

	@Test
	public void lambdaStringInsteadArgumentList() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("no args", "(lambda \"foo\" 42)");
		});
	}

	@Test
	public void defFunctionStringInsteadArgumentList() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("no args", "(def-function bad \"foo\" 42)");
		});
	}

	@Test
	public void lambdaArgumentNotSymbol() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("no args", "(lambda (40) 42)");
		});
	}

	@Test
	public void lambdaNil() throws Exception {
		FplValue nil = evaluate("lambda", "((lambda (a) nil) 42)");
		assertNull(nil);
	}

	@Test
	public void lambdaNilToString() throws Exception {
		String s = evaluate("lambda", "(lambda (a) nil)").toString();
		assertEquals("(lambda (a) nil)", s);
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
		assertEquals("compute n!", f.getComment());
		assertEquals("input", f.getParameterComment("n"));
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
	public void lambdaDynamic() throws Exception {
		evaluate("args", "(def args '(a b c))");
		evaluate("code", "(def code '((+ a b c)))");
		FplLambda f = (FplLambda) evaluate("lamda", "(lambda-dynamic args code)");
		assertEquals(3, f.getMinimumNumberOfParameters());
		assertEquals("(lambda (a b c) (+ a b c))", f.toString());
	}

	@Test
	public void functionDynamic() throws Exception {
		evaluate("args", "(def args '(a \"b\" c))");
		evaluate("code", "(def code '((+ a b c)))");
		FplLambda f = (FplLambda) evaluate("def", "(def-function-dynamic \"test\" args code)");
		assertEquals(3, f.getMinimumNumberOfParameters());
		assertEquals("(lambda (a b c) (+ a b c))", scope.get("test").toString());
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
			assertEquals("Not a function: x", e.getMessage());
			assertEquals(5, e.getAdded());
			StackTraceElement[] st = e.getStackTrace();
			assertEquals("x", st[0].getMethodName());
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
		assertEquals(4, values.size());
		FplValue value = values.get(3);
		assertEquals(FplInteger.valueOf(2), value);
	}

	@Test
	public void lambdaInFunctionWalksUpScopeToAccessParameter() throws Exception {
		lambdaInFunction("walk-up-scope-chain.fpl");
	}

	@Test
	public void lambdaInFunctionWalksUpScopeToAccessParameterWithShadowing() throws Exception {
		lambdaInFunction("walk-up-scope-chain-with-shadowing.fpl");
	}

	@Test
	public void lambdaInFunctionWalksUpWithIntermediateScope() throws Exception {
		lambdaInFunction("walk-up-with-intermediate-scope.fpl");
	}

	private void lambdaInFunction(String resourceName) throws Exception {
		ListResultCallback callback = evaluateResource(resourceName);
		List<FplValue> values = callback.getResults();
		assertEquals(3, values.size());
		assertNull(scope.get("message"));
		FplString value = (FplString) values.get(2);
		assertEquals("Normal (healthy weight)", value.getContent());
	}

	@Test
	public void scopeNestingWithFunctions() throws Exception {
		ListResultCallback callback = evaluateResource("sope-nesting-with-functions.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(3, values.size());
		FplList result = (FplList) values.get(2);
		assertEquals(4, result.size());
		assertEquals("outer-param", ((FplString) result.get(0)).getContent());
		assertNull(result.get(1));
		assertNull(result.get(2));
		assertNull(result.get(3));
	}

	@Test
	public void scopeNestingWithNestedFunctions() throws Exception {
		ListResultCallback callback = evaluateResource("sope-nesting-with-nested-functions.fpl");
		List<FplValue> values = callback.getResults();
		assertEquals(2, values.size());
		FplList result = (FplList) values.get(1);
		// expect: ("outer-param" "outer-param" "outer-variable" nil)
		assertEquals(4, result.size());
		assertEquals("outer-param", ((FplString) result.get(0)).getContent());
		assertEquals("outer-param", ((FplString) result.get(1)).getContent());
		assertEquals("outer-variable", ((FplString) result.get(2)).getContent());
		assertNull(result.get(3));
	}
}
