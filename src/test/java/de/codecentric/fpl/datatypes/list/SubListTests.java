package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;

public class SubListTests extends AbstractListTest {
	@Test
	public void subListBadRange() {
		assertThrows(EvaluationException.class, () -> {
			FplList list = FplList.EMPTY_LIST;
			list.subList(10, 0);
		});
	}

	@Test
	public void subListNegativeFrom() {
		assertThrows(EvaluationException.class, () -> {
			FplList list = FplList.EMPTY_LIST;
			list.subList(-1, 3);
		});
	}

	@Test
	public void largeSubListEndBeyondEndOfList() {
		assertThrows(EvaluationException.class, () -> {
			FplList list = create(1, 11);
			list.subList(3, 12);
		});
	}

	@Test
	public void smallSubListEndBeyondEndOfList() {
		assertThrows(EvaluationException.class, () -> {
			FplList list = create(1, 8);
			list.subList(3, 8);
		});
	}

	@Test
	public void subListCompleteOfShortList() throws EvaluationException {
		FplList list = create(1, 7);
		FplList subList = list.subList(0, 6);
		assertTrue(list == subList);
	}

	@Test
	public void subListCompleteOfLargeList() throws EvaluationException {
		FplList list = create(1, 24, 4, 15, 4);
		FplList subList = list.subList(0, 23);
		assertTrue(list == subList);
	}

	@Test
	public void subListOfShortList() throws EvaluationException {
		FplList list = create(1, 11);
		list = list.subList(3, 5);
		check(list, 4, 6);
	}

	@Test
	public void subListStartOfShortList() throws EvaluationException {
		FplList list = create(0, 7);
		list = list.subList(0, 6);
		check(list, 0, 6);
	}

	@Test
	public void subListEndOfShortList() throws EvaluationException {
		FplList list = create(0, 7);
		list = list.subList(1, 7);
		check(list, 1, 7);
	}

	@Test
	public void subListFromOneSmallBucket() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(5, 7), 5, 7);
	}

	@Test
	public void subListStartBeyondEndOfList() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			FplList list = create(0, 16, 4, 8, 4);
			check(list.subList(16, 17), 5, 7);
		});
	}

	@Test
	public void subListBucketsStart() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(0, 7), 0, 7);
	}

	@Test
	public void subListBucketsWithin() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(2, 7), 2, 7);
	}

	@Test
	public void subListBucketsStartWithPartFromLastBucket() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(0, 13), 0, 13);
	}

	@Test
	public void subListBucketsEnd() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(3, 16), 3, 16);
	}

	@Test
	public void subListFromOneLargeBucket() throws EvaluationException {
		FplList list = create(0, 40, 4, 32, 4);
		check(list.subList(5, 17), 5, 17);
	}

	@Test
	public void subListFromSeveralLargeBuckets() throws EvaluationException {
		FplList list = create(0, 100, 20, 20, 20, 20, 20);
		check(list, 0, 100);
		check(list.subList(5, 95), 5, 95);
	}

	@Test
	public void subListFromShape() throws EvaluationException {
		FplList list = create(0, 30, 4, 22, 4);
		check(list.subList(0, 5), 0, 5);
	}

	@Test
	public void fromEqualsToResultsInEmpty() throws EvaluationException {
		FplList list = create(0, 10);
		assertEquals(0, list.subList(3, 3).size());
	}

	@Test
	public void subListFromSpecialShape1() throws EvaluationException {
		FplList list = create(0, 45, 3, 37, 5);
		FplList leftSubList = list.subList(0, 38);
		check(leftSubList, 0, 38);
	}

	@Test
	public void subListFromSpecialShape2() throws EvaluationException {
		FplList list = create(0, 10, 2, 8);
		FplList leftSubList = list.subList(0, 9);
		check(leftSubList, 0, 9);
	}
}
