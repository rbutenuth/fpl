package de.codecentric.fpl.datatypes.list;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class Add extends AbstractListTest {
	
	@Test
	public void addAtStartOnEmptyList() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list = list.addAtStart(value(0));
		check(list, 0, 1);
		checkSizes(list, 1);
	}

	@Test
	public void addAtEndOnEmptyList() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list = list.addAtEnd(value(0));
		check(list, 0, 1);
		checkSizes(list, 1);
	}

	@Test
	public void addAtStartOnListWithSpaceInFirstBucket() throws EvaluationException {
		FplList list = create(1, 7);
		list = list.addAtStart(value(0));
		check(list, 0, 7);
		checkSizes(list,  7);
	}

	@Test
	public void addAtEndOnListWithSpaceInLastBucket() throws EvaluationException {
		FplList list = create(0, 6);
		list = list.addAtEnd(value(6));
		check(list, 0, 7);
		checkSizes(list,  7);
	}

	@Test
	public void addAtStartOnListWithNearlyFullFirstBucket() throws EvaluationException {
		FplList list = create(1, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 8);
		checkSizes(list, 8);
	}

	@Test
	public void addAtEndOnListWithNearlyFullLastBucket() throws EvaluationException {
		FplList list = create(0, 7);
		list = list.addAtEnd(value(7));
		check(list, 0, 8);
		checkSizes(list, 8);
	}

	@Test
	public void addAtStartOnListWithFullFirstBucket() throws EvaluationException {
		FplList list = create(1, 9);
		list = list.addAtStart(value(0));
		check(list, 0, 9);
		checkSizes(list, 1, 8);
	}

	@Test
	public void addAtEndOnListWithFullLastBucket() throws EvaluationException {
		FplList list = create(0, 8);
		list = list.addAtEnd(value(8));
		check(list, 0, 9);
		checkSizes(list, 8, 1);
	}

	@Test
	public void addAtStartOnList_7_8_16() throws EvaluationException {
		FplList list = create(1, 32, 7, 8, 16);
		list = list.addAtStart(value(0));
		check(list, 0, 32);
		checkSizes(list, 16, 16);
	}

	@Test
	public void addAtEndOnList_16_8_7() throws EvaluationException {
		FplList list = create(0, 31, 16, 8, 7);
		list = list.addAtEnd(value(31));
		check(list, 0, 32);
		checkSizes(list, 16, 16);
	}

	@Test
	public void addAtStartOnListWithBigFirstBucket() throws EvaluationException {
		FplList list = create(1, 100);
		list = list.addAtStart(value(0));
		check(list, 0, 100);
		checkSizes(list, 1, 99);
	}

	@Test
	public void addAtEndOnListWithBigLastBucket() throws EvaluationException {
		FplList list = create(0, 99);
		list = list.addAtEnd(value(99));
		check(list, 0, 100);
		checkSizes(list, 99, 1);
	}

	@Test
	public void addAtStartOnListWithSmallerBucketsAtEnd() throws EvaluationException {
		FplList list = create(1, 40, 7, 24, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 40);
		checkSizes(list, 32, 8);
	}

	@Test
	public void addAtEndOnListWithSmallerBucketsAtStart() throws EvaluationException {
		FplList list = create(0, 39, 8, 24, 7);
		list = list.addAtEnd(value(39));
		check(list, 0, 40);
		checkSizes(list, 8, 32);
	}

	@Test
	public void addAtStartWithBigCarry() throws EvaluationException {
		FplList list = create(1, 101).append(create(101, 201));
		list = list.addAtStart(value(0));
		check(list, 0, 201);
	}

	@Test
	public void addAtEndWithBigCarry() throws EvaluationException {
		FplList list = create(0, 100).append(create(100, 200));
		list = list.addAtEnd(value(200));
		check(list, 0, 201);
	}

	@Test
	public void addAtStartOnLongArray() throws EvaluationException {
		FplList list = create(1, 101);
		list = list.addAtStart(value(0));
		check(list, 0, 101);
	}

	@Test
	public void addAtEndOnLongArray() throws EvaluationException {
		FplList list = create(0, 100);
		list = list.addAtEnd(value(100));
		check(list, 0, 101);
	}

	@Test
	public void addAtStartWithStartOverflowInBigBucket() throws EvaluationException {
		FplList list = create(1, 9).append(create(9, 109));
		list = list.addAtStart(value(0));
		check(list, 0, 109);
	}
	
	@Test
	public void addAtEndWithStartOverflowInBigBucket() throws EvaluationException {
		FplList list = create(0, 100).append(create(100, 109));
		list = list.addAtEnd(value(109));
		check(list, 0, 110);
	}
	
	@Test
	public void addAtStartWithStartOverflowInSmallBucket() throws EvaluationException {
		FplList list = create(1, 9).append(create(9, 17));
		list = list.addAtStart(value(0));
		check(list, 0, 17);
	}

	@Test
	public void addAtEndWithStartOverflowInSmallBucket() throws EvaluationException {
		FplList list = create(0, 8).append(create(8, 16));
		list = list.addAtEnd(value(16));
		check(list, 0, 17);
	}

}
