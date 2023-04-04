package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.DefaultFplEngine;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Scanner;

public class PatchTests extends AbstractListTest {
	
	@Test
	public void patchOnEmptyList() {
		FplList result = FplList.EMPTY_LIST.patch(0, create(1, 3), 0);
		check(result, 1, 3);
	}
	
	@Test
	public void negativeFromThrowsException() {
		assertThrows(EvaluationException.class, () -> { create(0, 5).patch(-1, FplList.EMPTY_LIST, 0); });
	}

	@Test
	public void negativeNumReplacedThrowsException() {
		assertThrows(EvaluationException.class, () -> { create(0, 5).patch(0, FplList.EMPTY_LIST, -1); });
	}

	@Test
	public void fromPlusNumReplacedBeyondEndThrowsException() {
		assertThrows(EvaluationException.class, () -> { create(0, 5).patch(0, FplList.EMPTY_LIST, 6); });
	}

	@Test
	public void patchAll() {
		FplList original = create(0, 5);
		FplList patch = create(6, 10);
		FplList result = original.patch(0, patch, 5);
		check(result, 6, 10);
		checkPatched(original, result, 0, patch, 5);
	}
	
	@Test
	public void patchAtBeginning() {
		FplList original = create(0, 5);
		FplList patch = create(6, 9);
		FplList result = original.patch(0, patch, 2);
		checkValues(result, 6, 7, 8, 2, 3, 4);
		checkPatched(original, result, 0, patch, 2);
	}
	
	@Test
	public void patchAtEnd() {
		FplList original = create(0, 5);
		FplList patch = create(6, 9);
		FplList result = original.patch(3, patch, 2);
		checkValues(result, 0, 1, 2, 6, 7, 8);
		checkPatched(original, result, 3, patch, 2);
	}
	
	@Test
	public void patchInTheMiddle() {
		FplList original = create(0, 6);
		FplList patch = create(6, 8);
		FplList result = original.patch(3, patch, 2);
		checkValues(result, 0, 1, 2, 6, 7, 5);
		checkPatched(original, result, 3, patch, 2);
	}
	
	@Test
	public void patchInTheMiddleWithEvaluate() throws EvaluationException, ParseException {
    	FplEngine engine = new DefaultFplEngine();
        Scope scope = engine.getScope();

		FplList original = create(0, 6);
		scope.put("original", original);
		
		FplList patch = create(6, 8);
		scope.put("patch", patch);
		
		FplList result = (FplList)evaluate(scope, "patch", "(replace-elements original 3 patch 2)");
		checkValues(result, 0, 1, 2, 6, 7, 5);
		checkPatched(original, result, 3, patch, 2);
	}
	
    private FplValue evaluate(Scope s, String name, String input) throws ParseException, EvaluationException {
        Parser p = new Parser(new Scanner(name, new StringReader(input)));
        assertTrue(p.hasNext());
        FplValue e = p.next();
        assertFalse(p.hasNext());
        return e.evaluate(s);
    }

    private void checkPatched(FplList original, FplList result, int from, FplList patch, int numReplaced) {
		// check original is still unmodified (we assume it from 0..size)
		check(original, 0, original.size());
		int patchSize = patch.size();
		int resultSize = original.size() + patchSize - numReplaced;
		assertEquals(resultSize, result.size());
		for (int i = 0; i < resultSize; i++) {
			if (i < from) {
				assertEquals(original.get(i), result.get(i), "position: " + i);
			} else if (i < from + patchSize) {
				assertEquals(patch.get(i - from), result.get(i), "position: " + i);
			} else {
				assertEquals(original.get(i + numReplaced - patchSize), result.get(i), "position: " + i);
			}
		}
	}
}
