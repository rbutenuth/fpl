package de.codecentric.fpl.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import de.codecentric.fpl.parser.Token.Id;

/**
 * Tests for {@link Token}
 */
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
		new Token(null, Id.STRING, "str", Collections.emptyList());
	}

	@Test(expected = NullPointerException.class)
	public void positionNull5() throws IOException {
		new Token(null, Id.SYMBOL, "symbol", Collections.emptyList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void badId1() throws IOException {
		new Token(Position.UNKNOWN, Id.INTEGER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void positionBadId2() throws IOException {
		new Token(Position.UNKNOWN, Id.DOUBLE, "symbol", Collections.emptyList());
	}

	@Test(expected = NullPointerException.class)
	public void positionBadString() throws IOException {
		new Token(Position.UNKNOWN, Id.STRING, null, Collections.emptyList());
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
	public void eof() {
		Token t = new Token(Position.UNKNOWN, Id.EOF);
		assertEquals("end of file", t.toString());
	}
}
