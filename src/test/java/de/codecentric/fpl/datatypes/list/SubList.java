package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class SubList extends AbstractListTest {
	@Test(expected = EvaluationException.class)
	public void subListBadRange() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list.subList(10, 0);
	}

	@Test(expected = EvaluationException.class)
	public void subListNegativeFrom() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list.subList(-1, 3);
	}

	@Test(expected = EvaluationException.class)
	public void largeSubListEndBeyondEndOfList() throws EvaluationException {
		FplList list = create(1, 11);
		list.subList(3, 12);
	}

	@Test(expected = EvaluationException.class)
	public void smallSubListEndBeyondEndOfList() throws EvaluationException {
		FplList list = create(1, 8);
		list.subList(3, 8);
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

	@Test(expected = EvaluationException.class)
	public void subListStartBeyondEndOfList() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(16, 17), 5, 7);
	}

	@Test
	public void subListBucketsStart() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(0, 7), 0, 8);
	}

	@Test
	public void subListBucketsWithin() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(2, 7), 2, 8);
	}

	@Test
	public void subListBucketsStartWithPartFromLastBucket() throws EvaluationException {
		FplList list = create(0, 16, 4, 8, 4);
		check(list.subList(0, 13), 0, 14);
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
		check(list.subList(5, 95), 5, 96);
	}

	@Test
	public void fromEqualsToResultsInEmpty() throws EvaluationException {
		FplList list = create(0, 10);
		assertEquals(0, list.subList(3, 3).size());
	}

}
