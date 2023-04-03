package de.codecentric.fpl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.parser.Token.Id;

public class ScannerTest {
	private static String NL = System.lineSeparator();

	@Test
	public void nameNull() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			try (Scanner s = new Scanner(null, new StringReader(""))) {
				// not reached
			}
		});
	}

	@Test
	public void badLineNumber() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			try (Scanner s = new Scanner("bla", 0, 1, new StringReader(""))) {
				// not reached
			}
		});
	}

	@Test
	public void badColumnNumber() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			try (Scanner s = new Scanner("bla", 1, 0, new StringReader(""))) {
				// not reached
			}
		});
	}

	@Test
	public void unterminatedString() throws Exception {
		assertThrows(ParseException.class, () -> {
			try (Scanner sc = new Scanner("test", new StringReader("'( bla \") ; sinnfrei"))) {
				Token t = sc.next();
				while (t != null) {
					t = sc.next();
				}
			}
		});
	}

	@Test
	public void symbol() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("symbol"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("symbol", t.toString());
		}
	}

	@Test
	public void symbolAndWhitespace() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("symbol   "))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("symbol", t.toString());
		}
	}

	@Test
	public void symbolAndLeftParenthesis() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("symbol("))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("symbol", t.toString());
			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.LEFT_PAREN, t.getId());
		}
	}

	@Test
	public void commentsAndSymbol() throws Exception {
		String COMMENT = "commentLine1" + NL + "; commentLine2" + NL + ";commentLine3";
		try (Scanner sc = new Scanner("test", new StringReader(";   " + COMMENT + NL + " symbol"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("symbol", t.toString());
			assertEquals(COMMENT.replace(";", "").replace(" ", ""), t.getComment());
		}
	}

	@Test
	public void commentAtEndOfFile() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("symbol\n; xxx"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("symbol", t.toString());
			t = sc.next();
			assertEquals(Id.EOF, t.getId());
		}
	}

	@Test
	public void symbolEmptyCommentSymbol() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("bla\n;\rblubber"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("bla", t.toString());
			String comment = t.getComment();
			assertEquals(0, comment.length());
			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("blubber", t.toString());
		}
	}

	@Test
	public void symbolStartsWithMinus() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("-a"))) {
			Token t = sc.next();
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("-a", t.toString());
			assertEquals(1, t.getPosition().getLine());
			t = sc.next();
			assertEquals(Id.EOF, t.getId());
		}
	}

	@Test
	public void firstLineWithHash() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("#!/bin/fpl" + NL + "test"))) {
			Token t = sc.next();
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("test", t.toString());
			assertEquals(2, t.getPosition().getLine());
			t = sc.next();
			assertEquals(Id.EOF, t.getId());
		}
	}

	@Test
	public void parenthesisAndSymbol() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("'( bla \n\r) ; sinnfrei\n\r;leer\n"))) {
			Token t = sc.next();
			assertNotNull(t);
			assertEquals(Id.QUOTE, t.getId());
			assertEquals("'", t.toString());
			Position p = t.getPosition();
			assertEquals("test", p.getName());
			assertEquals(1, p.getLine());
			assertEquals(2, p.getColumn());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.LEFT_PAREN, t.getId());
			assertEquals("(", t.toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.SYMBOL, t.getId());
			assertEquals("bla", t.getStringValue());
			assertEquals("Position[name=\"test\", line=1, column=5]", t.getPosition().toString());

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.RIGHT_PAREN, t.getId());
			assertEquals(")", t.toString());

			t = sc.next();
			assertEquals(Id.EOF, t.getId());
		}
	}

	@Test
	public void number() throws Exception {
		try (Scanner sc = new Scanner("test",
				new StringReader("123\t-456 ;comment \n1.23e4\n-31.4e-1\n2.78E+0\n3.14\n3E2\n-.5"))) {
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

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.DOUBLE, t.getId());
			assertEquals(300, t.getDoubleValue(), 0.001);

			t = sc.next();
			assertNotNull(t);
			assertEquals(Id.DOUBLE, t.getId());
			assertEquals(-0.5, t.getDoubleValue(), 0.001);

			Position p = t.getPosition();
			assertEquals("test", p.getName());
			assertEquals(7, p.getLine());
			assertEquals(2, p.getColumn());
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

	@Test
	public void illegalSymbolCharacter() throws Exception {
		try (Scanner sc = new Scanner("test", new StringReader("{"))) {
			sc.next();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("Illegal character for symbol: {", pe.getMessage());
		}
	}

	@Test
	public void exceptionOnRead() throws Exception {
		try (Scanner sc = new Scanner("test", 1, 1, new OnReadExceptionReader())) {
			sc.next();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("bäm", pe.getMessage());
		}
	}
	
	private static class OnReadExceptionReader extends Reader {

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			throw new IOException("bäm");
		}

		@Override
		public void close() throws IOException {
		}
		
	}
	
	@Test
	public void exceptionOnClose() throws Exception {
		try (Scanner sc = new Scanner("test", 1, 1, new OnCloseExceptionReader())) {
			sc.next();
			sc.close();
			fail("missing exception");
		} catch (ParseException pe) {
			assertEquals("wont close", pe.getMessage());
		}
	}
	
	private static class OnCloseExceptionReader extends Reader {

		@Override
		public void close() throws IOException {
			throw new IOException("wont close");
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			cbuf[0] = ')';
			return 1;
		}
	}
}
