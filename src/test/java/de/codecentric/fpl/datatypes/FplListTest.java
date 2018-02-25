package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class FplListTest {
	// TODO: More operations: remove(int index), insert(FplValue value, int index), set(FplValue value, int index)
	
	@Test
	public void testEmpty() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		assertEquals(0, list.size());
		assertFalse(list.iterator().hasNext());
	}
	
	@Test
	public void testElementConstructor() throws EvaluationException {
		FplList list = new FplList(value(42));
		assertEquals(1, list.size());
		assertEquals(value(42), list.get(0));
	}
	
	@Test
	public void testFromIterator() throws EvaluationException {
		final int size = 100;
		FplList list = new FplList(new Iterator<FplValue>() {
			int count = 0;

			@Override
			public boolean hasNext() {
				return count < size;
			}

			@Override
			public FplValue next() {
				return value(count++);
			}
		});
		assertEquals(size, list.size());
		
		// Check with Iterator
		Iterator<FplValue> iter = list.iterator();
		int c = 0;
		while (iter.hasNext()) {
			assertEquals(value(c), iter.next());
			c++;
		}
		assertEquals(size, c);
		
		// Check with get
		for (int i = 0; i < size; i++) {
			assertEquals(value(i), list.get(i));
		}
		
		// Check with check method
		check(0, size - 1, list);
		
		// Check out of bounds
		try {
			list.get(-1);
			fail("Exception missing");
		} catch (EvaluationException e) {
			// expected
		}
		try {
			list.get(size);
			fail("Exception missing");
		} catch (EvaluationException e) {
			// expected
		}
	}

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
	public void testRemoveFirstSizeOne() throws EvaluationException {
		FplList list = new FplList(new FplValue[1]);
		FplList rest = list.removeFirst();
		assertEquals(0, rest.size());
	}

	@Test
	public void testRemoveFirstSizeTwo() throws EvaluationException {
		FplList list = new FplList(value(1)).addAtEnd(value(2));
		FplList rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void testRemoveFirstSizeHundred() throws EvaluationException {
		FplList list = create(0, 99);
		FplList rest = list.removeFirst();
		assertEquals(99, rest.size());
		check(1, 99, rest);
	}

	@Test
	public void testRemoveFirstSizeFiftyAppendFifty() throws EvaluationException {
		FplList list = create(0, 49).append(create(50, 99));
		FplList rest = list.removeFirst();
		assertEquals(99, rest.size());
		check(1, 99, rest);
	}

	@Test
	public void testRemoveFirstSizeTwoFromAppend() throws EvaluationException {
		FplList list = create(1, 1).append(create(2, 2));
		FplList rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void testRemoveLastSizeOne() throws EvaluationException {
		FplList list = new FplList(new FplValue[1]);
		FplList rest = list.removeLast();
		assertEquals(0, rest.size());
	}

	@Test
	public void testRemoveLastSizeTwo() throws EvaluationException {
		FplList list = new FplList(value(1)).addAtEnd(value(2));
		FplList rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void testRemoveLastSizeHundred() throws EvaluationException {
		FplList list = create(0, 99);
		FplList rest = list.removeLast();
		assertEquals(99, rest.size());
		check(0, 98, rest);
	}

	@Test
	public void testRemoveLastSizeFiftyAppendFifty() throws EvaluationException {
		FplList list = create(0, 49).append(create(50, 99));
		FplList rest = list.removeLast();
		assertEquals(99, rest.size());
		check(0, 98, rest);
	}

	@Test
	public void testRemoveLastSizeTwoFromAppend() throws EvaluationException {
		FplList list = create(1, 1).append(create(2, 2));
		FplList rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void testListConstructor() throws EvaluationException {
		int start = 3;
		int end = 10;
		FplValue[] values = new FplValue[end - start + 1];
		for (int i = start, j = 0; i <= end; i++, j++) {
			values[j] = value(i);
		}
		FplList list = new FplList(Arrays.asList(values));
		check(start, end, list);
	}
	
	@Test
	public void testEmptyListConstructor() throws EvaluationException {
		FplList list = new FplList(Collections.emptyList());
		assertEquals(0, list.size());
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
	
	@Test
	public void testAdd() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		final int size = 100;
		for (int i = 0; i < size; i++) {
			list = list.addAtEnd(value(i));
			assertEquals(i + 1, list.size());
			for (int j = 0; j <= i; j++) {
				assertEquals(value(j), list.get(j));
			}
		}
	}

	@Test
	public void testAddBigCarry() throws EvaluationException {
		FplList list = create(0, 99).append(create(100, 199));
		list = list.addAtEnd(value(200));
		assertEquals(201, list.size());
		check(0, 200, list);
	}

	@Test
	public void testAddOnLongArray() throws EvaluationException {
		FplList list = create(0, 99);
		list = list.addAtEnd(value(100));
		assertEquals(101, list.size());
		check(0, 100, list);
	}

	@Test
	public void testCons() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		final int size = 100;
		for (int i = size - 1; i >= 0; i--) {
			list = list.addAtStart(value(i));
			assertEquals(size - i, list.size());
			for (int j = i; j < size; j++) {
				assertEquals(value(j), list.get(j - i));
			}
		}
	}

	@Test
	public void testConsBigCarry() throws EvaluationException {
		FplList list = create(1, 100).append(create(101, 200));
		list = list.addAtStart(value(0));
		assertEquals(201, list.size());
		check(0, 200, list);
	}

	@Test
	public void testConsOnLongArray() throws EvaluationException {
		FplList list = create(1, 100);
		list = list.addAtStart(value(0));
		assertEquals(101, list.size());
		check(0, 100, list);
	}

	@Test
	public void testAppendFirstEmpty() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.append(create(0, 9));
		check(0, 9, list);
	}
	
	@Test
	public void testSecondEmpty() throws EvaluationException {
		FplList list = create(0, 9).append(FplList.EMPTY_LIST);
		check(0, 9, list);
	}
	
	@Test
	public void testSecondNull() throws EvaluationException {
		FplList list = create(0, 9).append(null);
		check(0, 9, list);
	}
	
	@Test
	public void testAppendBothNotEmpty() throws EvaluationException {
		FplList list = create(0, 5).append(create(6, 19));
		check(0, 19, list);
	}
	
	@Test
	public void testReshape() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		for (int i = 0; i < 10; i++) {
			list = list.append(create(i * 5, i * 5 + 4));
		}
		check(0, 49, list);
		assertEquals(5, list.numberOfBuckets());
	}
	
	/**
	 * @param start First element
	 * @param end Last element in list
	 * @param list List to check
	 */
	private void check(int start, int end, FplList list) throws EvaluationException {
		//assertEquals(end - start, list.size());
		Iterator<FplValue> iter = list.iterator();
		int value = start;
		while (iter.hasNext()) {
			FplInteger next = (FplInteger) iter.next();
			assertEquals(value, next.getValue());
			value++;
		}
		assertEquals(end + 1, value);
	}
	
	private FplList create(int start, int end) {
		FplValue[] values = new FplValue[end - start + 1];
		for (int i = start, j = 0; i <= end; i++, j++) {
			values[j] = value(i);
		}
		return new FplList(values);
	}
	
	private FplInteger value(int i) {
		return FplInteger.valueOf(i);
	}
}
