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
		FplList list = create(1, 10);
		list.subList(3, 12);
	}

	@Test(expected = EvaluationException.class)
	public void smallSubListEndBeyondEndOfList() throws EvaluationException {
		FplList list = create(1, 7);
		list.subList(3, 8);
	}

	@Test
	public void subListCompleteOfShortList() throws EvaluationException {
		FplList list = create(1, 6);
		FplList subList = list.subList(0, 6);
		assertTrue(list == subList);
	}

	@Test
	public void subListCompleteOfLargeList() throws EvaluationException {
		FplList list = create(1, 23, 4, 15, 4);
		FplList subList = list.subList(0, 23);
		assertTrue(list == subList);
	}

	@Test
	public void subListOfShortList() throws EvaluationException {
		FplList list = create(1, 10);
		list = list.subList(3, 5);
		check(4, 5, list);
	}

	@Test
	public void subListStartOfShortList() throws EvaluationException {
		FplList list = create(0, 6);
		list = list.subList(0, 6);
		check(0, 5, list);
	}

	@Test
	public void subListEndOfShortList() throws EvaluationException {
		FplList list = create(0, 6);
		list = list.subList(1, 7);
		check(1, 6, list);
	}

	@Test
	public void subListFromOneSmallBucket() throws EvaluationException {
		FplList list = create(0, 15, 4, 8, 4);
		check(5, 6, list.subList(5, 7));
	}

	@Test(expected = EvaluationException.class)
	public void subListStartBeyondEndOfList() throws EvaluationException {
		FplList list = create(0, 15, 4, 8, 4);
		check(5, 6, list.subList(16, 17));
	}

	@Test
	public void subListBucketsStart() throws EvaluationException {
		FplList list = create(0, 15, 4, 8, 4);
		check(0, 7, list.subList(0, 7));
	}

	@Test
	public void subListBucketsWithin() throws EvaluationException {
		FplList list = create(0, 15, 4, 8, 4);
		check(2, 7, list.subList(2, 7));
	}

	@Test
	public void subListBucketsStartWithPartFromLastBucket() throws EvaluationException {
		FplList list = create(0, 15, 4, 8, 4);
		check(0, 13, list.subList(0, 13));
	}

	@Test
	public void subListBucketsEnd() throws EvaluationException {
		FplList list = create(0, 15, 4, 8, 4);
		check(3, 15, list.subList(3, 16));
	}

	@Test
	public void subListFromOneLargeBucket() throws EvaluationException {
		FplList list = create(0, 39, 4, 32, 4);
		check(5, 16, list.subList(5, 17));
	}

	@Test
	public void fromEqualsToResultsInEmpty() throws EvaluationException {
		FplList list = create(0, 9);
		assertEquals(0, list.subList(3, 3).size());
	}

}
