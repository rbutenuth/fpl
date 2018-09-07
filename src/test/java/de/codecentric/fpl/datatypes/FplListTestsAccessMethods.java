package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class FplListTestsAccessMethods extends AbstractFplListTest {
	@Test(expected = NoSuchElementException.class)
	public void testIterateTooMuch() {
		Iterator<FplValue> iter = new FplList(value(1)).iterator();
		assertTrue(iter.hasNext());
		assertEquals(value(1), iter.next());
		assertFalse(iter.hasNext());
		iter.next();
	}

	@Test
	public void testToString() {
		assertEquals("(0 1 2)", create(0, 2).toString());
	}

	@Test
	public void testFirstSizeOne() throws EvaluationException {
		FplList list = new FplList(value(1));
		assertEquals(value(1), list.first());
	}

	@Test
	public void testLastSizeOne() throws EvaluationException {
		FplList list = new FplList(value(1));
		assertEquals(value(1), list.last());
	}

	@Test
	public void testIterator() throws EvaluationException {
		FplList list = create(3, 10);
		check(3, 10, list);
	}

	@Test(expected = EvaluationException.class)
	public void testFirstEmptyFails() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		list.first();
	}

	@Test(expected = EvaluationException.class)
	public void testRestEmptyFails() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		list.removeFirst();
	}

	@Test(expected = EvaluationException.class)
	public void testLastEmptyFails() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		list.removeFirst();
	}

	@Test(expected = EvaluationException.class)
	public void testSubListBadRange() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list.subList(10, 0);
	}

	@Test(expected = EvaluationException.class)
	public void testSubListNegativeFrom() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list.subList(-1, 3);
	}

	@Test(expected = EvaluationException.class)
	public void testSubListEndBeyondEndOfList() throws EvaluationException {
		FplList list = create(1, 10);
		list.subList(3, 12);
	}

	@Test
	public void testSubListOfShortList() throws EvaluationException {
		FplList list = create(1, 10);
		list = list.subList(3, 5);
		check(4, 5, list);
	}

	@Test
	public void testSubListCompleteFromOneBucket() throws EvaluationException {
		for (int size = 10; size < 100; size++) {
			FplList list = create(1, size);
			list = list.subList(0, size);
			check(1, size, list);
		}
	}

	@Test
	public void testSubListCompleteFromShortList() throws EvaluationException {
		FplList list = create(1, 7);
		list = list.subList(0, 4);
		check(1, 4, list);
	}

	@Test
	public void testSubListCompleteFromSeveral() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		int size = 100;
		for (int i = 1; i <= size; i++) {
			list = list.addAtEnd(value(i));
		}
		list = list.subList(0, size);
		check(1, size, list);
	}

	@Test
	public void testFromEqualsToResultsInEmpty() throws EvaluationException {
		FplList list = create(0, 9);
		assertEquals(0, list.subList(3, 3).size());
	}

}
