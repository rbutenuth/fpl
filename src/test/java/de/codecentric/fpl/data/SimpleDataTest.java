package de.codecentric.fpl.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplLazy;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.parser.Position;

/**
 * Simple tests for different classes in the data package.
 */
public class SimpleDataTest {

	@Test
	public void nullSymbol() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Symbol(null);
		});
	}

	@Test
	public void symbolComment() {
		Symbol symbol = new Symbol("sym", Position.UNKNOWN, "comment");
		assertEquals("comment", symbol.getComment());
		symbol = new Symbol("sym", Position.UNKNOWN, "");
		assertEquals("", symbol.getComment());
		symbol = new Symbol("sym", Position.UNKNOWN, null);
		assertEquals("", symbol.getComment());
	}

	@Test
	public void emptySymbol() {
		Symbol s = new Symbol("");
		assertEquals("", s.getName());
	}

	@Test
	public void goodSymbol1() {
		Symbol a = new Symbol("a");
		assertEquals("a", a.getName());
		assertEquals(Position.UNKNOWN, a.getPosition());
	}

	@Test
	public void goodSymbol2() {
		Symbol a = new Symbol("a", new Position("name", 1, 1), "");
		assertEquals("a", a.getName());
		assertEquals(new Position("name", 1, 1), a.getPosition());
		assertEquals(new Position("name", 1, 1).hashCode(), a.getPosition().hashCode());
		assertFalse(new Position("name", 2, 1).equals(a.getPosition()));
		assertFalse(new Position("name", 1, 2).equals(a.getPosition()));
		assertFalse(new Position("x", 1, 1).equals(a.getPosition()));
	}

	@Test
	public void positionEquals() {
		assertFalse(new Position("bla", 1, 1).equals(null));
		Object o = "sonstwas"; // to avoid warning "unlikely argument"
		assertFalse(new Position("bla", 1, 1).equals(o));
	}

	@Test
	public void badPosition1() {
		assertThrows(NullPointerException.class, () -> {
			new Position(null, 1, 1);
		});
	}

	@Test
	public void badPosition2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Position("bla", -1, 1);
		});
	}

	@Test
	public void badPosition3() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Position("bla", 1, -1);
		});
	}

	@Test
	public void symbolEquals() {
		Symbol a = new Symbol("a");
		assertFalse(a.equals(null));
		Object obj = "sonstwas"; // to avoid warning "unlikely argument"
		assertFalse(a.equals(obj));
		assertFalse(a.equals(new Symbol("b")));
		assertTrue(a.equals(a));
		assertTrue(a.equals(new Symbol("a")));
		// position is not part of equals
		assertTrue(a.equals(new Symbol("a", new Position("bla", 1, 1), "")));
	}

	@Test
	public void symbolHashCode() {
		Symbol a = new Symbol("a");
		assertEquals(a.hashCode(), new Symbol("a").hashCode());
	}

	@Test
	public void badLazyExpression() {
		assertThrows(NullPointerException.class, () -> {
			FplLazy.make(null, null);
		});
	}
}
