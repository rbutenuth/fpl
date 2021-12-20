package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

public class FplEngineTest extends AbstractFplTest {

	@Test
	public void commentsInBuiltinFunctions() throws EvaluationException {
		for (Entry<String, FplValue> entry : scope) {
			FplValue value = entry.getValue();
            if (value instanceof AbstractFunction) {
                AbstractFunction f = (AbstractFunction)value;
                checkForComment(f);
            }
        }
	}
	
	@Test
	public void findPositionOfNull() {
		Position p = FplEngine.findPosition(null);
		assertEquals("<unknown>", p.getName());
	}

	@Test
	public void findPositionOfNumber() {
		Position p = FplEngine.findPosition(FplInteger.valueOf(13));
		assertEquals("<unknown>", p.getName());
	}

	@Test
	public void findPositionListWithInteger() {
		FplList l = FplList.fromValue(FplInteger.valueOf(13));
		Position p = FplEngine.findPosition(l);
		assertEquals("<unknown>", p.getName());
	}

	@Test
	public void findPositionSymbol() {
		Symbol s = new Symbol("foo", new Position("name", 42, 43), null);
		Position p = FplEngine.findPosition(s);
		assertEquals("name", p.getName());
		assertEquals(42, p.getLine());
		assertEquals(43, p.getColumn());
	}

	@Test
	public void findPositionListWithSymbol() {
		Symbol s = new Symbol("foo", new Position("name", 42, 43), null);
		FplList l = FplList.fromValue(s);
		Position p = FplEngine.findPosition(l);
		assertEquals("name", p.getName());
		assertEquals(42, p.getLine());
		assertEquals(43, p.getColumn());
	}

	@Test
	public void findPositionListWithIntegerAndSymbol() {
		Symbol s = new Symbol("foo", new Position("name", 42, 43), null);
		FplList l = FplList.fromValues(FplInteger.valueOf(13), s);
		Position p = FplEngine.findPosition(l);
		assertEquals("name", p.getName());
		assertEquals(42, p.getLine());
		assertEquals(43, p.getColumn());
	}

	private void checkForComment(AbstractFunction f) throws EvaluationException {
		String comment = f.getComment();
		if (comment.isEmpty()) {
			throw new EvaluationException("missing comment in function " + f.getName());
		}
	}
}
