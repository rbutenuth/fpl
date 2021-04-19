package de.codecentric.fpl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.parser.Token.Id;

public class TokenTest {

	@Test
	public void positionNull1() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			new Token(null, Id.INTEGER);
		});
	}

	@Test
	public void positionNull2() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			new Token(null, 1);
		});
	}

	@Test
	public void positionNull3() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			new Token(null, 3.14);
		});
	}

	@Test
	public void positionNull4() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			new Token(null, Id.STRING, "str", "");
		});
	}

	@Test
	public void positionNull5() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			new Token(null, Id.SYMBOL, "symbol", "");
		});
	}

	@Test
	public void badId1() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			new Token(Position.UNKNOWN, Id.INTEGER);
		});
	}

	@Test
	public void positionBadId2() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			new Token(Position.UNKNOWN, Id.DOUBLE, "symbol", "");
		});
	}

	@Test
	public void positionBadString() throws IOException {
		assertThrows(NullPointerException.class, () -> {
			new Token(Position.UNKNOWN, Id.STRING, null, "");
		});
	}

	@Test
	public void getDouble() throws IOException {
		assertThrows(IllegalStateException.class, () -> {
			Token t = new Token(Position.UNKNOWN, 1);
			t.getDoubleValue();
		});
	}

	@Test
	public void getString() throws IOException {
		assertThrows(IllegalStateException.class, () -> {
			Token t = new Token(Position.UNKNOWN, 3);
			t.getStringValue();
		});
	}

	@Test
	public void getInteger() throws IOException {
		assertThrows(IllegalStateException.class, () -> {
			Token t = new Token(Position.UNKNOWN, 3.14);
			t.getIntegerValue();
		});
	}

	@Test
	public void eof() {
		Token t = new Token(Position.UNKNOWN, Id.EOF);
		assertEquals("end of file", t.toString());
	}
}
