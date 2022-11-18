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
	public void updateFirstOneSmallBucket() throws EvaluationException {
		FplList list = create(0, 8);
		list = list.set(0, number);
		checkUpdated(0, 8, list);
	}

	@Test
	public void updateFirstTwoBuckets() throws EvaluationException {
		FplList list = create(0, 20, 10, 10);
		list = list.set(0, number);
		checkUpdated(0, 20, list);
	}

	@Test
	public void updateSecondTwoBuckets() throws EvaluationException {
		FplList list = create(0, 20, 10, 10);
		list = list.set(1, number);
		checkUpdated(1, 20, list);
	}

	@Test
	public void updateFirstWithReshape() throws EvaluationException {
		FplList list = create(0, 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
		list = list.set(0, number);
		checkUpdated(0, 100, list);
	}

	@Test
	public void updateFirstWithReshape2() throws EvaluationException {
		FplList list = create(0, 100, 4, 15, 11, 10, 10, 10, 10, 10, 10, 10);
		list = list.set(10, number);
		checkUpdated(10, 100, list);
	}

	@Test
	public void updateLastOneSmallBucket() throws EvaluationException {
		FplList list = create(0, 8);
		list = list.set(7, number);
		checkUpdated(7, 8, list);
	}

	@Test
	public void updateLastTwoBuckets() throws EvaluationException {
		FplList list = create(0, 20, 10, 10);
		list = list.set(19, number);
		checkUpdated(19, 20, list);
	}

	@Test
	public void updateSecondLastTwoBuckets() throws EvaluationException {
		FplList list = create(0, 20, 10, 10);
		list = list.set(18, number);
		checkUpdated(18, 20, list);
	}

	@Test
	public void updateLastWithReshape() throws EvaluationException {
		FplList list = create(0, 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
		list = list.set(99, number);
		checkUpdated(99, 100, list);
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
