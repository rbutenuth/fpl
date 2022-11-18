package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;

public class SetTests extends AbstractListTest {
	private static FplString str = FplString.make("foo");
	private static FplInteger number = FplInteger.valueOf(-1);
	
	@Test
	public void setOnEmptyThrowsException() {
		assertThrows(EvaluationException.class, () -> { FplList.EMPTY_LIST.set(0, str); });
	}
	
	@Test
	public void setWithNegativeIndexThrowsException() {
		assertThrows(EvaluationException.class, () -> { create(0, 10).set(-1, str); });
	}
	
	@Test
	public void setWithIndexOutOfBoundsOneBucketThrowsException() {
		assertThrows(EvaluationException.class, () -> { create(0, 5).set(5, str); });
	}
	
	@Test
	public void setWithIndexOutOfBoundsThrowsException() {
		assertThrows(EvaluationException.class, () -> { create(0, 10, 5, 5).set(10, str); });
	}
	
	@Test
	public void updateFirst() throws EvaluationException {
		FplList list = create(0, 10);
		list = list.set(0, number);
		checkUpdated(0, 10, list);
	}

	@Test
	public void updateLast() throws EvaluationException {
		FplList list = create(0, 10);
		list = list.set(9, number);
		checkUpdated(9, 10, list);
	}

	private void checkUpdated(int updatedIndex, int expectedSize, FplList list) {
		assertEquals(expectedSize, list.size());
		for (int i = 0; i < expectedSize; i++) {
			if (i == updatedIndex) {
				assertEquals(number, list.get(i));
			} else {
				assertEquals(FplInteger.valueOf(i), list.get(i));
			}
		}
	}
}
