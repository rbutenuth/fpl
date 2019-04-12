package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.list.FplList;

public class Add extends AbstractListTest {
	// addAtStart
	
	@Test
	public void testAddOnEmptyList() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list = list.addAtStart(value(0));
		check(list, 0, 1);
		checkSizes(list, 1);
	}

	@Test
	public void testAddOnListWithSpaceInFirstBucket() throws EvaluationException {
		FplList list = create(1, 7);
		list = list.addAtStart(value(0));
		check(list, 0, 7);
		checkSizes(list,  7);
	}

	@Test
	public void testAddOnListWithNearlyFullFirstBucket() throws EvaluationException {
		FplList list = create(1, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 8);
		checkSizes(list, 8);
	}

	@Test
	public void testAddOnListWithFullFirstBucket() throws EvaluationException {
		FplList list = create(1, 9);
		list = list.addAtStart(value(0));
		check(list, 0, 9);
		checkSizes(list, 1, 8);
	}

	@Test
	public void testAddOnListWithTwo8Buckets() throws EvaluationException {
		FplList list = create(1, 17, 8, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 17);
		checkSizes(list, 1, 8, 8);
	}

	@Test
	public void testAddOnListWithBigFirstBucket() throws EvaluationException {
		FplList list = create(1, 100);
		list = list.addAtStart(value(0));
		check(list, 0, 100);
		checkSizes(list, 1, 99);
	}

	@Test
	public void testAddAtStart() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		final int size = 1000;
		for (int i = size - 1; i >= 0; i--) {
			list = list.addAtStart(value(i));
			assertEquals(size - i, list.size());
			for (int j = i; j < size; j++) {
				assertEquals(value(j), list.get(j - i));
			}
		}
	}

	@Test
	public void testAddAtFrontBigCarry() throws EvaluationException {
		FplList list = create(1, 101).append(create(101, 201));
		list = list.addAtStart(value(0));
		check(list, 0, 201);
	}

	@Test
	public void testAddAtFrontOnLongArray() throws EvaluationException {
		FplList list = create(1, 101);
		list = list.addAtStart(value(0));
		check(list, 0, 101);
	}

	@Test
	public void testAddAtFrontOverflowInBigBucket() throws EvaluationException {
		FplList list = create(1, 9).append(create(9, 109));
		list = list.addAtStart(value(0));
		check(list, 0, 109);
	}
	
	@Test
	public void testAddAtFrontOverflowInSmallBucket() throws EvaluationException {
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
