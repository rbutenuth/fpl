package de.codecentric.fpl.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import de.codecentric.fpl.parser.Token.Id;

public class TokenTest {

	@Test(expected = NullPointerException.class)
	public void positionNull1() throws IOException {
		new Token(null, Id.INTEGER);
	}

	@Test(expected = NullPointerException.class)
	public void positionNull2() throws IOException {
		new Token(null, 1);
	}

	@Test(expected = NullPointerException.class)
	public void positionNull3() throws IOException {
		new Token(null, 3.14);
	}

	@Test(expected = NullPointerException.class)
	public void positionNull4() throws IOException {
		new Token(null, Id.STRING, "str", "");
	}

	@Test(expected = NullPointerException.class)
	public void positionNull5() throws IOException {
		new Token(null, Id.SYMBOL, "symbol", "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void badId1() throws IOException {
		new Token(Position.UNKNOWN, Id.INTEGER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void positionBadId2() throws IOException {
		new Token(Position.UNKNOWN, Id.DOUBLE, "symbol", "");
	}

	@Test(expected = NullPointerException.class)
	public void positionBadString() throws IOException {
		new Token(Position.UNKNOWN, Id.STRING, null, "");
	}

	@Test(expected = IllegalStateException.class)
	public void getDouble() throws IOException {
		Token t = new Token(Position.UNKNOWN, 1);
		t.getDoubleValue();
	}

	@Test(expected = IllegalStateException.class)
	public void getString() throws IOException {
		Token t = new Token(Position.UNKNOWN, 3);
		t.getStringValue();
	}

	@Test(expected = IllegalStateException.class)
	public void getInteger() throws IOException {
		Token t = new Token(Position.UNKNOWN, 3.14);
		t.getIntegerValue();
	}

	@Test
	public void colon() {
		Token t = new Token(Position.UNKNOWN, Id.COLON);
		assertEquals(":", t.toString());
	}
	
	@Test
	public void leftCurlyBacket() {
		Token t = new Token(Position.UNKNOWN, Id.LEFT_CURLY_BRACKET);
		assertEquals("{", t.toString());
	}
	
	@Test
	public void eof() {
		Token t = new Token(Position.UNKNOWN, Id.EOF);
		assertEquals("end of file", t.toString());
	}
}
