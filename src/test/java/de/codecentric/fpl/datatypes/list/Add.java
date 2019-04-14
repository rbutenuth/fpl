package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class Add extends AbstractListTest {
	// addAtStart
	
	@Test
	public void addAtStartOnEmptyList() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list = list.addAtStart(value(0));
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
	public void addAtStartOnListWithNearlyFullFirstBucket() throws EvaluationException {
		FplList list = create(1, 8);
		list = list.addAtStart(value(0));
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
	public void addAtStartOnList_7_8_16() throws EvaluationException {
		FplList list = create(1, 32, 7, 8, 16);
		list = list.addAtStart(value(0));
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
	public void addAtStartOnListWithSmallerBucketsAtEnd() throws EvaluationException {
		FplList list = create(1, 40, 7, 24, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 40);
		checkSizes(list, 32, 8);
	}

	@Test
	public void addAtStartWithBigCarry() throws EvaluationException {
		FplList list = create(1, 101).append(create(101, 201));
		list = list.addAtStart(value(0));
		check(list, 0, 201);
	}

	@Test
	public void addAtStartOnLongArray() throws EvaluationException {
		FplList list = create(1, 101);
		list = list.addAtStart(value(0));
		check(list, 0, 101);
	}

	@Test
	public void addAtStartWithStartOverflowInBigBucket() throws EvaluationException {
		FplList list = create(1, 9).append(create(9, 109));
		list = list.addAtStart(value(0));
		check(list, 0, 109);
	}
	
	@Test
	public void addAtStartWithStartOverflowInSmallBucket() throws EvaluationException {
		FplList list = create(1, 9).append(create(9, 17));
		list = list.addAtStart(value(0));
		check(list, 0, 17);
	}

	// addAtEnd
	
	@Test
	public void testAddAtEnd() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		final int size = 1001;
		for (int i = 0; i < size; i++) {
			list = list.addAtEnd(value(i));
			assertEquals(i + 1, list.size());
			for (int j = 0; j <= i; j++) {
				assertEquals(value(j), list.get(j));
			}
		}
	}

	@Test
	public void testAddAtEndBigCarry() throws EvaluationException {
		FplList list = create(0, 100).append(create(100, 200));
		list = list.addAtEnd(value(200));
		check(list, 0, 201);
	}

	@Test
	public void testAddAtEndOnLongArray() throws EvaluationException {
		FplList list = create(0, 100);
		list = list.addAtEnd(value(100));
		check(list, 0, 101);
		checkSizes(list, 100, 1);
	}

	@Test
	public void testAddOverflowInBigBucket() throws EvaluationException {
		FplList list = create(0, 100).append(create(100, 108));
		list = list.addAtEnd(value(108));
		check(list, 0, 109);
	}
}
