package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;

public class AccessMethods extends AbstractListTest {
	
	@Test
	public void createAndCheck() throws EvaluationException {
		FplList list = create(0, 10);
		assertEquals(10, list.size());
		check(0, 9, list);
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testIterateTooMuchSmallList() {
		SizedIterator<FplValue> iter = (SizedIterator<FplValue>) new FplList(value(1)).iterator();
		assertEquals(1, iter.size());
		assertTrue(iter.hasNext());
		assertEquals(value(1), iter.next());
		assertFalse(iter.hasNext());
		iter.next();
	}

	@Test(expected = NoSuchElementException.class)
	public void testIterateTooMuchLargeList() {
		SizedIterator<FplValue> iter = (SizedIterator<FplValue>) create(0, 100).iterator();
		// check twice (cache)
		assertEquals(100, iter.size());
		assertEquals(100, iter.size());
		for (int i = 0; i <= 99; i++) {
			assertTrue(iter.hasNext());
			assertEquals(value(i), iter.next());
		}
		assertFalse(iter.hasNext());
		iter.next();
	}

	@Test
	public void listToString() {
		assertEquals("(0 1 2)", create(0, 3).toString());
	}

	@Test
	public void firstSizeOne() throws EvaluationException {
		FplList list = new FplList(value(1));
		assertEquals(value(1), list.first());
	}

	@Test
	public void firstSmall() throws EvaluationException {
		FplList list = create(3, 6);
		assertEquals(value(3), list.first());
	}

	@Test
	public void firstLarge() throws EvaluationException {
		FplList list = create(3, 51);
		assertEquals(value(3), list.first());
	}

	@Test(expected = EvaluationException.class)
	public void firstEmptyFails() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		list.first();
	}

	@Test
	public void lastSizeOne() throws EvaluationException {
		FplList list = new FplList(value(1));
		assertEquals(value(1), list.last());
	}

	@Test
	public void lastLarge() throws EvaluationException {
		FplList list = create(3, 51);
		assertEquals(value(50), list.last());
	}

	
	@Test(expected = EvaluationException.class)
	public void removeFirstEmptyFails() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		list.removeFirst();
	}

	@Test
	public void removeFirstSmall() throws EvaluationException {
		FplList list = create(0, 6).removeFirst();
		check(1, 5, list);
	}
	
	@Test
	public void removeFirstWhenFirstBucketIsOfSizeOne() throws EvaluationException {
		FplList list = create(0, 10, 1, 9).removeFirst();
		check(1, 9, list);
	}
	
	@Test
	public void removeFirstWhenFirstBucketIsOfSizeTwo() throws EvaluationException {
		FplList list = create(0, 11, 2, 9).removeFirst();
		check(1, 10, list);
	}
	
	@Test(expected = EvaluationException.class)
	public void lastEmptyFails() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		list.removeFirst();
	}

	@Test
	public void removeLastWhenLastBucketIsOfSizeOne() throws EvaluationException {
		FplList list = create(0, 10, 9, 1).removeLast();
		check(0, 8, list);
	}
	
	@Test
	public void removeLastWhenLastBucketIsOfSizeTwo() throws EvaluationException {
		FplList list = create(0, 11, 9, 2).removeLast();
		check(0, 9, list);
	}
	
	@Test(expected = EvaluationException.class)
	public void getFromEmptyList() throws EvaluationException {
		FplList.EMPTY_LIST.get(0);
	}
	
	@Test(expected = EvaluationException.class)
	public void getSmallListIndexNegative() throws EvaluationException {
		create(0, 4).get(-1);
	}
	
	@Test(expected = EvaluationException.class)
	public void getSmallListIndexOutOfBounds() throws EvaluationException {
		create(0, 4).get(4);
	}
	
	@Test(expected = EvaluationException.class)
	public void getLargelListIndexOutOfBounds() throws EvaluationException {
		create(0, 101).get(101);
	}
}
