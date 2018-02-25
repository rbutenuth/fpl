package de.codecentric.fpl.parser;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

/**
 * Tests for {@link Token}
 */
public class TokenTest {

    @Test(expected = NullPointerException.class)
    public void testPositionNull1() throws IOException {
        new Token(null, Token.Id.INTEGER);
    }

    @Test(expected = NullPointerException.class)
    public void testPositionNull2() throws IOException {
        new Token(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testPositionNull3() throws IOException {
        new Token(null, 3.14);
    }

    @Test(expected = NullPointerException.class)
    public void testPositionNull4() throws IOException {
        new Token(null, Token.Id.STRING, "str", Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testPositionNull5() throws IOException {
        new Token(null, Token.Id.SYMBOL, "symbol", Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadId1() throws IOException {
        new Token(Position.UNKNOWN, Token.Id.INTEGER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPositionBadId2() throws IOException {
        new Token(Position.UNKNOWN, Token.Id.DOUBLE, "symbol", Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testPositionBadString() throws IOException {
        new Token(Position.UNKNOWN, Token.Id.STRING, null, Collections.emptyList());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDouble() throws IOException {
        Token t = new Token(Position.UNKNOWN, 1);
        t.getDoubleValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetString() throws IOException {
        Token t = new Token(Position.UNKNOWN, 3);
        t.getStringValue();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetInteger() throws IOException {
        Token t = new Token(Position.UNKNOWN, 3.14);
        t.getIntegerValue();
    }

}
