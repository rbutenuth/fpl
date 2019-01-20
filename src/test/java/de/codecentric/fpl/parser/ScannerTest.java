package de.codecentric.fpl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;
import de.codecentric.fpl.parser.Token;

/**
 * Tests for {@link Scanner}
 */
public class ScannerTest {

    @Test(expected = NullPointerException.class)
    public void testNameNull() throws IOException {
        new Scanner(null, new StringReader(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadLineNumber() throws IOException {
        new Scanner("bla", 0, new StringReader(""));
    }

    @Test(expected = ParseException.class)
    public void testUnterminatedString() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("'( bla \") ; sinnfrei"));
        Token t = sc.next();
        while (t != null) {
            t = sc.next();
        }
    }

    @Test
    public void testCommentsAndSymbol() throws Exception {
        Scanner sc = new Scanner("test", new StringReader(";   commentLine1\n; commentLine2\n;commentLine3\n symbol"));
        Token t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.SYMBOL, t.getId());
        assertEquals("symbol", t.toString());
        List<String> comments = t.getCommentLines();
        assertEquals(3, comments.size());
        assertEquals("  commentLine1", comments.get(0));
        assertEquals("commentLine2", comments.get(1));
        assertEquals("commentLine3", comments.get(2));
    }
    
    @Test
    public void testParenthesisAndSymbol() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("'( bla \n\r) ; sinnfrei\n\r;leer\n{[,]}"));
        Token t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.QUOTE, t.getId());
        assertEquals("'", t.toString());
        Position p = t.getPosition();
        assertEquals("test", p.getName());
        assertEquals(1, p.getLine());
        assertEquals(1, p.getColumn());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.LEFT_PAREN, t.getId());
        assertEquals("(", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.SYMBOL, t.getId());
        assertEquals("bla", t.getStringValue());
        assertEquals("Position[name=\"test\", line=1, column=4]", t.getPosition().toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.RIGHT_PAREN, t.getId());
        assertEquals(")", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.LEFT_CURLY_BRACKET, t.getId());
        assertEquals("{", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.LEFT_SQUARE_BRACKET, t.getId());
        assertEquals("[", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.COMMA, t.getId());
        assertEquals(",", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.RIGHT_SQUARE_BRACKET, t.getId());
        assertEquals("]", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.RIGHT_CURLY_BRACKET, t.getId());
        assertEquals("}", t.toString());

        t = sc.next();
        assertNull(t);
    }

    @Test
    public void testNumber() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("123\t-456 ;comment \n1.23e4\n-31.4e-1\n2.78E+0\n3.14"));
        Token t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.INTEGER, t.getId());
        assertEquals(123, t.getIntegerValue());
        assertEquals("123", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.INTEGER, t.getId());
        assertEquals(-456, t.getIntegerValue());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.DOUBLE, t.getId());
        assertEquals(1.23e4, t.getDoubleValue(), 0.001);
        assertEquals("12300.0", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.DOUBLE, t.getId());
        assertEquals(-31.4e-1, t.getDoubleValue(), 0.001);
        
        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.DOUBLE, t.getId());
        assertEquals(2.78, t.getDoubleValue(), 0.001);
        
        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.DOUBLE, t.getId());
        assertEquals(3.14, t.getDoubleValue(), 0.001);
        
        Position p = t.getPosition();
        assertEquals("test", p.getName());
        assertEquals(5, p.getLine());
        assertEquals(1, p.getColumn());
    }

    @Test()
    public void testBadNumber() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("123ef456"));
        try {
            sc.next();
        } catch (ParseException pe) {
            assertEquals("Bad number: 123ef456", pe.getMessage());
        }
    }

    @Test()
    public void testString() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("(\"a\\\"bc\ndef\\nhij\" \r\n\"a\\tb\\rc\\n\")"));
        Token t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.LEFT_PAREN, t.getId());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.STRING, t.getId());
        assertEquals("a\"bc\ndef\nhij", t.getStringValue());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.STRING, t.getId());
        assertEquals("a\tb\rc\n", t.getStringValue());
        assertEquals("\"a\tb\rc\n\"", t.toString());

        t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.RIGHT_PAREN, t.getId());

        assertNull(sc.next());
    }
    
    @Test
    public void testJsonEscapes() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("\"\\/\\f\\b\")"));
        Token t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.STRING, t.getId());
        assertEquals("/\f\b", t.getStringValue());
    }
    
    @Test
    public void testHexEscape() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("\"\\u12ab\")"));
        Token t = sc.next();
        assertNotNull(t);
        assertEquals(Token.Id.STRING, t.getId());
        String s = t.getStringValue();
        assertEquals(1, s.length());
        char ch = s.charAt(0);
        assertEquals(0x12ab, ch);
    }
    
    @Test()
    public void testBadHexDigit() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("\"\\u12z4\""));
        try {
            sc.next();
        } catch (ParseException pe) {
            assertEquals("Illegal hex digit: z", pe.getMessage());
        }
    }
    
    @Test()
    public void testShortHexSequence() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("\"\\u12\""));
        try {
            sc.next();
        } catch (ParseException pe) {
            assertEquals("Illegal hex digit: \"", pe.getMessage());
        }
    }
    
    @Test()
    public void testBadQuoting() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("\"\\"));
        try {
            sc.next();
        } catch (ParseException pe) {
            assertEquals("Unterminated \\ at end of input", pe.getMessage());
        }
    }
    
    @Test()
    public void testEndOfSourceInHexSequence() throws Exception {
        Scanner sc = new Scanner("test", new StringReader("\"\\u12"));
        try {
            sc.next();
        } catch (ParseException pe) {
            assertEquals("Unterminated string at end of input", pe.getMessage());
        }
    }
}
