package de.codecentric.fpl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Tests for {@link Parser}.
 */
public class ParserTest extends AbstractFplTest {

	@Test
	public void scannerNull() throws ParseException {
		assertThrows(NullPointerException.class, () -> {
			try (Parser p = new Parser(null)) {
				// not reached
			}
		});
	}

	@Test
	public void emptyList() throws Exception {
		Parser p = parser("empty list", "()");
		assertTrue(p.hasNext());
		FplList l = (FplList) p.next();
		assertEquals(0, l.size());
		assertFalse(p.hasNext());
		p.close();
	}

	@Test
	public void stringConstant() throws Exception {
		Parser p = parser("string", "\"a string\"");
		assertTrue(p.hasNext());
		FplValue e = p.next();
		assertEquals(FplString.class, e.getClass());
		FplString s = (FplString) e;
		assertEquals("a string", s.getContent());
		assertFalse(p.hasNext());
	}

	@Test
	public void integerConstant() throws Exception {
		Parser p = parser("integer", "42");
		assertTrue(p.hasNext());
		FplValue e = p.next();
		assertEquals(FplInteger.class, e.getClass());
		FplInteger i = (FplInteger) e;
		assertEquals(42, i.getValue());
		assertFalse(p.hasNext());
	}

	@Test
	public void simpleList() throws Exception {
		Parser p = parser("simple list", "(symbol 42 3.1415 \"a string\")");
		verifySimpleList(p);
	}

	private void verifySimpleList(Parser p) throws ParseException {
		assertTrue(p.hasNext());
		FplList l = (FplList) p.next();
		assertEquals(4, l.size());
		Iterator<FplValue> iter = l.iterator();
		assertTrue(iter.hasNext());
		Symbol s = (Symbol) iter.next();
		assertEquals("symbol", s.getName());
		assertTrue(iter.hasNext());
		FplInteger i = (FplInteger) iter.next();
		assertEquals(42, i.getValue());
		assertTrue(iter.hasNext());
		FplDouble d = (FplDouble) iter.next();
		assertEquals(3.1415, d.getValue(), 0.000001);
		assertTrue(iter.hasNext());
		FplString str = (FplString) iter.next();
		assertEquals("a string", str.getContent());
		assertFalse(iter.hasNext());
		assertFalse(p.hasNext());
	}

	@Test
	public void nestedList() throws Exception {
		Parser p = parser("nested list", "(symbol (42 3.1415) \"a string\")");
		assertTrue(p.hasNext());
		FplList l = (FplList) p.next();
		assertEquals(3, l.size());
		Iterator<FplValue> iter = l.iterator();
		assertTrue(iter.hasNext());
		Symbol s = (Symbol) iter.next();
		assertEquals("symbol", s.getName());
		assertTrue(iter.hasNext());

		FplList sub = (FplList) iter.next();
		Iterator<FplValue> subIter = sub.iterator();
		assertEquals(2, sub.size());

		FplInteger i = (FplInteger) subIter.next();
		assertEquals(42, i.getValue());
		assertTrue(subIter.hasNext());
		FplDouble d = (FplDouble) subIter.next();
		assertEquals(3.1415, d.getValue(), 0.000001);
		assertFalse(subIter.hasNext());

		assertTrue(iter.hasNext());
		FplString str = (FplString) iter.next();
		assertEquals("a string", str.getContent());
		assertFalse(iter.hasNext());
		assertFalse(p.hasNext());
	}

	@Test
	public void unterminatedList() throws Exception {
		Parser p = parser("syntax error", "(symbol");
		try {
			assertTrue(p.hasNext());
			p.next();
			fail("Exception missing");
		} catch (ParseException e) {
			assertEquals("Unexpected end of source in list", e.getMessage());
			assertEquals("syntax error", e.getPosition().getName());
			assertEquals(1, e.getPosition().getLine());
			assertEquals(3, e.getPosition().getColumn());
		}
	}

	@Test
	public void quote() throws Exception {
		Parser p = parser("quote", "'('symbol 42 3.1415 \"a string\")");
		assertTrue(p.hasNext());
		FplList l = (FplList) p.next();
		assertEquals(2, l.size());
		assertEquals("(quote ((quote symbol) 42 3.1415 \"a string\"))", l.toString());
	}

	@Test
	public void unbalancedParenthesesResultInSyntaxError1() throws Exception {
		Parser p = parser("syntax error", "())");
		assertTrue(p.hasNext());
		p.next(); // parse ()
		try {
			assertTrue(p.hasNext());
			p.next();
			fail("Exception missing");
		} catch (ParseException e) {
			assertEquals("unexpected token: )", e.getMessage());
			assertEquals("syntax error", e.getPosition().getName());
			assertEquals(1, e.getPosition().getLine());
			assertEquals(5, e.getPosition().getColumn());
		}
	}

	@Test
	public void unbalancedParenthesesResultInSyntaxError2() throws Exception {
		Parser p = parser("syntax error", "(");
		try {
			assertTrue(p.hasNext());
			p.next();
			fail("Exception missing");
		} catch (ParseException e) {
			assertEquals("Unexpected end of source in list", e.getMessage());
			assertEquals("syntax error", e.getPosition().getName());
			assertEquals(1, e.getPosition().getLine());
			assertEquals(2, e.getPosition().getColumn());
		}
	}

	@Test
	public void nextAtEndOfSourceThrowsException() throws Exception {
		Parser p = parser("symbol", "symbol");
		assertTrue(p.hasNext());
		FplValue symbol = p.next();
		assertEquals("symbol", symbol.typeName());
		assertThrows(NoSuchElementException.class, () -> {
			p.next();
		});
	}

	@Test
	public void checkParseException() {
		ParseException p = new ParseException(null, "foo");
		assertEquals("foo", p.getMessage());
		assertEquals(Position.UNKNOWN, p.getPosition());
	}

	@Test
	public void checkParseExceptionWithCause() {
		ParseException p = new ParseException(null, "foo", new NullPointerException("bar"));
		assertEquals("foo", p.getMessage());
		assertEquals(Position.UNKNOWN, p.getPosition());
		assertEquals("bar", p.getCause().getMessage());
	}
}
