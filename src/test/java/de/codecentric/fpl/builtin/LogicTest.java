package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Tests for the function interpreter.
 */
public class LogicTest extends AbstractFplTest {

	@Test
	public void not() throws Exception {
		assertTrue(asBoolean(evaluate("not", "(not 0)")));
		assertTrue(asBoolean(evaluate("not", "(not 0.0)")));
		assertTrue(asBoolean(evaluate("not", "(not nil)")));
		assertTrue(asBoolean(evaluate("not", "(not '())")));
		assertTrue(asBoolean(evaluate("not", "(not \"\")")));
		assertFalse(asBoolean(evaluate("not", "(not 1)")));
		assertFalse(asBoolean(evaluate("not", "(not '(1))")));
		assertFalse(asBoolean(evaluate("not", "(not 1.0)")));
		assertFalse(asBoolean(evaluate("not", "(not \"a\")")));
	}

	@Test
	public void and() throws Exception {
		assertTrue(asBoolean(evaluate("and", "(and 1 1)")));
		assertTrue(asBoolean(evaluate("and", "(and 1)")));
		assertFalse(asBoolean(evaluate("and", "(and 1 0)")));
		assertFalse(asBoolean(evaluate("and", "(and 1 1 0)")));
		assertFalse(asBoolean(evaluate("and", "(and 0 1)")));
		assertFalse(asBoolean(evaluate("and", "(and 0)")));
	}

	@Test
	public void or() throws Exception {
		assertTrue(asBoolean(evaluate("or", "(or 0 1)")));
		assertTrue(asBoolean(evaluate("or", "(or 1 0)")));
		assertTrue(asBoolean(evaluate("or", "(or 0 0 1 0)")));
		assertTrue(asBoolean(evaluate("or", "(or 1)")));
		assertFalse(asBoolean(evaluate("or", "(or 0 0)")));
		assertFalse(asBoolean(evaluate("or", "(or 0)")));
	}

	@Test
	public void xor() throws Exception {
		assertTrue(asBoolean(evaluate("xor", "(or 0 1)")));
		assertTrue(asBoolean(evaluate("xor", "(xor 1 0)")));
		assertTrue(asBoolean(evaluate("xor", "(xor 0 0 1 0)")));
		assertTrue(asBoolean(evaluate("xor", "(xor 1)")));
		assertFalse(asBoolean(evaluate("xor", "(xor 0 0)")));
		assertFalse(asBoolean(evaluate("xor", "(xor 1 1)")));
		assertFalse(asBoolean(evaluate("xor", "(xor 0)")));
	}

	@Test
	public void isSymbol() throws Exception {
		assertTrue(asBoolean(evaluate("is-symbol", "(is-symbol 'x)")));
		assertFalse(asBoolean(evaluate("is-symbol", "(is-symbol x)")));
		assertFalse(asBoolean(evaluate("is-symbol", "(is-symbol (eval x))")));
		assertFalse(asBoolean(evaluate("is-symbol", "(is-symbol 17)")));
		evaluate("assign", "(put a 'x)");
		assertTrue(asBoolean(evaluate("is-symbol", "(is-symbol a)")));
	}

	@Test
	public void isInteger() throws Exception {
		assertTrue(asBoolean(evaluate("is-integer", "(is-integer 1)")));
		assertFalse(asBoolean(evaluate("is-integer", "(is-integer 1.5)")));
	}

	@Test
	public void isDouble() throws Exception {
		assertTrue(asBoolean(evaluate("is-double", "(is-double 1.5)")));
		assertFalse(asBoolean(evaluate("is-double", "(is-double 1)")));
	}

	@Test
	public void isNumber() throws Exception {
		assertTrue(asBoolean(evaluate("is-number", "(is-number 1.5)")));
		assertTrue(asBoolean(evaluate("is-number", "(is-number 2)")));
		assertFalse(asBoolean(evaluate("is-number", "(is-number \"bla\")")));
	}

	@Test
	public void isList() throws Exception {
		assertTrue(asBoolean(evaluate("is-list", "(is-list (list 1 2 3))")));
		assertFalse(asBoolean(evaluate("is-list", "(is-list 1)")));
	}

	@Test
	public void isObject() throws Exception {
		assertTrue(asBoolean(evaluate("class", "(is-object (class (def-field a0 (this))))")));
		assertFalse(asBoolean(evaluate("is-object", "(is-object (dict foo bar))")));
		assertFalse(asBoolean(evaluate("is-object", "(is-object 1)")));
	}

	@Test
	public void isString() throws Exception {
		assertTrue(asBoolean(evaluate("is-string", "(is-string \"hello\")")));
		assertFalse(asBoolean(evaluate("is-string", "(is-string 1)")));
	}

	@Test
	public void isFunction() throws Exception {
		assertTrue(asBoolean(evaluate("is-function", "(is-function is-function)")));
		assertTrue(asBoolean(evaluate("is-function", "(is-function (lambda (x) (* x x)))")));
		assertFalse(asBoolean(evaluate("is-function", "(is-function 1)")));
	}

	private boolean asBoolean(FplValue value) throws Exception {
		if (value instanceof FplInteger) {
			long l = ((FplInteger) value).getValue();
			if (l == 1) {
				return true;
			} else if (l == 0) {
				return false;
			} else {
				throw new Exception("unexpected: " + l);
			}
		} else {
			throw new Exception("not an FplInteger: " + value);
		}
	}
}
