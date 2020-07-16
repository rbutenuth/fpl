package de.codecentric.fpl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;
import de.codecentric.fpl.parser.Token;
import de.codecentric.fpl.parser.Token.Id;

/**
 * Tests for {@link Scanner}
 */
public class ScannerTest {

	@Test(expected = NullPointerException.class)
	public void nameNull() throws IOException {
		try (Scanner s = new Scanner(null, new StringReader(""))) {
			// not reached
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void badLineNumber() throws IOException {
		try (Scanner s = new Scanner("bla", 0, new StringReader(""))) {
			// not reached
		}
	}

	@Test(expected = ParseException.class)
	public void unterminatedString() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("'( bla \") ; sinnfrei"))) {
			Token t = sc.next();
			while (t != null) {
				t = sc.next();
			}
		}
	}

	@Test
	public void commentsAndSymbol() throws Exception {
		try (Scanner sc = new Scanner("test",
				new StringReader(";   commentLine1\n; commentLine2\n;commentLine3\n symbol"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("symbol", t.toString());
			List<String> comments = t.getCommentLines();
			assertEquals(3, comments.size());
			assertEquals("  commentLine1", comments.get(0));
			assertEquals("commentLine2", comments.get(1));
			assertEquals("commentLine3", comments.get(2));
		}
	}

	@Test
	public void symbolStartsWithMinus() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("-a"))) {
			Token t = sc.next();
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("-a", t.toString());
			t = sc.next();
			assertEquals(Id.EOF, t.getId());
		}
	}

	@Test
	public void parenthesisAndSymbol() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("'( bla \n\r) ; sinnfrei\n\r;leer\n{:}"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.QUOTE, t.getId());
			assertEquals("'", t.toString());
			Position p = t.getPosition();
			assertEquals("test", p.getName());
			assertEquals(1, p.getLine());
			assertEquals(1, p.getColumn());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.LEFT_PAREN, t.getId());
			assertEquals("(", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("bla", t.getStringValue());
			assertEquals("Position[name=\"test\", line=1, column=4]", t.getPosition().toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.RIGHT_PAREN, t.getId());
			assertEquals(")", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.LEFT_CURLY_BRACKET, t.getId());
			assertEquals("{", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.COLON, t.getId());
			assertEquals(":", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.RIGHT_CURLY_BRACKET, t.getId());
			assertEquals("}", t.toString());

			t = sc.next();
			assertEquals(Id.EOF, t.getId());
		}
	}

	@Test
	public void number() throws Exception {
		try (Scanner sc = new Scanner("test",
				new StringReader("123\t-456 ;comment \n1.23e4\n-31.4e-1\n2.78E+0\n3.14"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.INTEGER, t.getId());
			assertEquals(123, t.getIntegerValue());
			assertEquals("123", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.INTEGER, t.getId());
			assertEquals(-456, t.getIntegerValue());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.DOUBLE, t.getId());
			assertEquals(1.23e4, t.getDoubleValue(), 0.001);
			assertEquals("12300.0", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.DOUBLE, t.getId());
			assertEquals(-31.4e-1, t.getDoubleValue(), 0.001);

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.DOUBLE, t.getId());
			assertEquals(2.78, t.getDoubleValue(), 0.001);

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.DOUBLE, t.getId());
			assertEquals(3.14, t.getDoubleValue(), 0.001);

			Position p = t.getPosition();
			assertEquals("test", p.getName());
			assertEquals(5, p.getLine());
			assertEquals(1, p.getColumn());
		}
	}

	@Test
	public void badNumber() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("123ef456"))) {
			try {
				sc.next();
			} catch (ParseException pe) {
				assertEquals("Bad number: 123ef456", pe.getMessage());
			}
		}
	}

	@Test
	public void string() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("(\"a\\\"bc\ndef\\nhij\" \r\n\"a\\tb\\rc\\n\")"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.LEFT_PAREN, t.getId());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.STRING, t.getId());
			assertEquals("a\"bc\ndef\nhij", t.getStringValue());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.STRING, t.getId());
			assertEquals("a\tb\rc\n", t.getStringValue());
			assertEquals("\"a\tb\rc\n\"", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.RIGHT_PAREN, t.getId());

			assertEquals(Id.EOF, sc.next().getId());
		}
	}

	@Test
	public void jsonEscapes() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("\"\\/\\f\\b\")"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.STRING, t.getId());
			assertEquals("/\f\b", t.getStringValue());
		}
	}

	@Test
	public void hexEscape() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("\"\\u12ab\")"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.STRING, t.getId());
			String s = t.getStringValue();
			assertEquals(1, s.length());
			char ch = s.charAt(0);
			assertEquals(0x12ab, ch);
		}
	}

	@Test
	public void badHexDigit() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("\"\\u12z4\""))) {
			sc.next();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("Illegal hex digit: z", pe.getMessage());
		}
	}

	@Test
	public void shortHexSequence() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("\"\\u12\""))) {
			sc.next();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("Illegal hex digit: \"", pe.getMessage());
		}
	}

	@Test
	public void badQuoting() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("\"\\"))) {
			sc.next();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("Unterminated \\ at end of input", pe.getMessage());
		}
	}

	@Test
	public void endOfSourceInHexSequence() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("\"\\u12"))) {
			sc.next();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("Unterminated string at end of input", pe.getMessage());
		}
	}
}
