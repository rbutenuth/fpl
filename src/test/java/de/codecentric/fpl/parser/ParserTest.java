package de.codecentric.fpl.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Tests for {@link Parser}.
 */
public class ParserTest extends AbstractFplTest {

	@Test(expected = NullPointerException.class)
	public void testScannerNull() throws ParseException, IOException {
		try (Parser p = new Parser(null)) {
			// not reached
		}
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

	private void verifySimpleList(Parser p) throws ParseException, IOException {
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
			assertEquals(2, e.getPosition().getColumn());
		}
	}

	@Test
	public void quote() throws Exception {
		Parser p = parser("quote", "'('symbol 42 3.1415 \"a string\")");
		assertTrue(p.hasNext());
		FplList l = (FplList) p.next();
		assertEquals(2, l.size());
		assertEquals("(quote <list>)", l.toString());
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
			assertEquals(4, e.getPosition().getColumn());
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
			assertEquals(1, e.getPosition().getColumn());
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void nextAtEndOfSourceThrowsException() throws Exception {
		Parser p = parser("symbol", "symbol");
		assertTrue(p.hasNext());
		FplValue symbol = p.next();
		assertEquals("symbol", symbol.typeName());
		p.next();
		fail("Exception missing");
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

	@Test
	public void creayteEmptyObject() throws Exception {
		Parser p = parser("empty object", "{}");
		assertTrue(p.hasNext());
		FplObject object = (FplObject) p.next();
		assertTrue(object.isEmpty());
	}

	@Test
	public void unterminatedObjectThrowsException() throws Exception {
		Parser p = parser("unterminated object", "{");
		try {
			assertTrue(p.hasNext());
			p.next();
			fail("Exception missing");
		} catch (ParseException e) {
			assertEquals("Symbol or } expected.", e.getMessage());
			assertEquals("unterminated object", e.getPosition().getName());
			assertEquals(1, e.getPosition().getLine());
			assertEquals(2, e.getPosition().getColumn());
		}
	}

	@Test
	public void incompletePairThrowsException() throws Exception {
		Parser p = parser("one pair", "{ foo: }");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("exception missing");
		} catch (ParseException e) {
			assertEquals("unexpected token: }", e.getMessage());
		}
	}

	@Test
	public void incompletePairAtEOFThrowsException() throws Exception {
		Parser p = parser("one pair", "{ foo:");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("exception missing");
		} catch (ParseException e) {
			assertEquals("Unexpected end of source in object", e.getMessage());
		}
	}

	@Test
	public void twoValuesBehindKeyThrowsException() throws Exception {
		Parser p = parser("pair and two values", "{ key: 1 2");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("exception missing");
		} catch (ParseException e) {
			assertEquals("Symbol or } expected.", e.getMessage());
		}
	}

	@Test
	public void colonWithMissingInPairThrowsException() throws Exception {
		Parser p = parser("pair and two values", "{ key 1 }");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("exception missing");
		} catch (ParseException e) {
			assertEquals("Expect : after key.", e.getMessage());
		}
	}

	@Test
	public void keyOnlyThrowsException() throws Exception {
		Parser p = parser("key only", "{ (a b) key nonsense");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("exception missing");
		} catch (ParseException e) {
			assertEquals("Expect key (symbol), but got (", e.getMessage());
		}
	}

	@Test
	public void nilValueInMapThrowsException() throws Exception {
		Parser p = parser("null value", "{ key: nil }");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("exception missing");
		} catch (ParseException e) {
			assertEquals("Null no allowed as value.", e.getMessage());
		}
	}

	@Test
	public void mapWithOneKey() throws Exception {
		Parser p = parser("one pair", "{ foo: 2}");
		assertTrue(p.hasNext());
		FplObject object = (FplObject) p.next();
		assertFalse(object.isEmpty());
	}

	@Test
	public void mapWithTwoPairs() throws Exception {
		Parser p = parser("two pairs", "{ foo: 2 bar: 4}");
		assertTrue(p.hasNext());
		FplObject object = (FplObject) p.next();
		assertFalse(object.isEmpty());
	}

	@Test
	public void duplicateKeyInMapThrowsException() throws Exception {
		Parser p = parser("duplicate-key", "{ foo: 2 foo: 4}");
		assertTrue(p.hasNext());
		try {
			p.next();
			fail("Missing Exception");
		} catch (ParseException e) {
			assertEquals("Duplicate key: foo", e.getMessage());
		}
	}
}
