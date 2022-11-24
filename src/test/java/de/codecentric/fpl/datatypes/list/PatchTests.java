package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;

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
		FplList result = create(0, 5).patch(0, create(6, 10), 5);
		check(result, 6, 10);
	}
	
	@Test
	public void patchAtBeginning() {
		FplList result = create(0, 5).patch(0, create(6, 9), 2);
		checkValues(result, 6, 7, 8, 2, 3, 4);
	}
	
	@Test
	public void patchAtEnd() {
		FplList result = create(0, 5).patch(3, create(6, 9), 2);
		checkValues(result, 0, 1, 2, 6, 7, 8);
	}
	
	@Test
	public void patchInTheMiddle() {
		FplList result = create(0, 6).patch(3, create(6, 8), 2);
		checkValues(result, 0, 1, 2, 6, 7, 5);
	}
}
