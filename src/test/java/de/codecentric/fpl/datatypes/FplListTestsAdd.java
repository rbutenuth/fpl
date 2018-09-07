package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class FplListTestsAdd extends AbstractFplListTest {
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
		FplList list = create(0, 99).append(create(100, 199));
		list = list.addAtEnd(value(200));
		check(0, 200, list);
	}

	@Test
	public void testAddOnLongArray() throws EvaluationException {
		FplList list = create(0, 99);
		list = list.addAtEnd(value(100));
		check(0, 100, list);
	}

	@Test
	public void testAddOverflowInBigBucket() throws EvaluationException {
		FplList list = create(0, 99).append(create(100, 107));
		list = list.addAtEnd(value(108));
		check(0, 108, list);
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
		FplList list = create(1, 100).append(create(101, 200));
		list = list.addAtStart(value(0));
		check(0, 200, list);
	}

	@Test
	public void testAddAtFrontOnLongArray() throws EvaluationException {
		FplList list = create(1, 100);
		list = list.addAtStart(value(0));
		check(0, 100, list);
	}

	@Test
	public void testAddAtFrontOverflowInBigBucket() throws EvaluationException {
		FplList list = create(1, 8).append(create(9, 108));
		list = list.addAtStart(value(0));
		check(0, 108, list);
	}
}
