package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;

public class AccessMethods extends AbstractListTest {

	@Test
	public void createAndCheck() throws EvaluationException {
		FplList list = create(0, 10);
		assertEquals(10, list.size());
		check(list, 0, 10);
	}

	@Test
	public void iterateTooMuchSmallList() {
		assertThrows(NoSuchElementException.class, () -> {
			Iterator<FplValue> iter = FplList.fromValue(value(1)).iterator();
			assertTrue(iter.hasNext());
			assertEquals(value(1), iter.next());
			assertFalse(iter.hasNext());
			iter.next();
		});
	}

	@Test
	public void iterateTooMuchLargeList() {
		assertThrows(NoSuchElementException.class, () -> {
			Iterator<FplValue> iter = create(0, 100).iterator();
			for (int i = 0; i <= 99; i++) {
				assertTrue(iter.hasNext());
				assertEquals(value(i), iter.next());
			}
			assertFalse(iter.hasNext());
			iter.next();
		});
	}

	@Test
	public void listToStringNumbers() {
		assertEquals("(0 1 2)", create(0, 3).toString());
	}

	@Test
	public void firstSizeOne() throws EvaluationException {
		FplList list = FplList.fromValue(value(1));
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

	@Test
	public void firstEmptyFails() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			FplList list = FplList.fromValues(new FplValue[0]);
			list.first();
		});
	}

	@Test
	public void lastSizeOne() throws EvaluationException {
		FplList list = FplList.fromValue(value(1));
		assertEquals(value(1), list.last());
	}

	@Test
	public void lastLarge() throws EvaluationException {
		FplList list = create(3, 51);
		assertEquals(value(50), list.last());
	}

	@Test
	public void removeFirstEmptyFails() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			FplList list = FplList.fromValues(new FplValue[0]);
			list.removeFirst();
		});
	}

	@Test
	public void removeFirstSmall() throws EvaluationException {
		FplList list = create(0, 6).removeFirst();
		check(list, 1, 6);
	}

	@Test
	public void removeFirstWhenFirstBucketIsOfSizeOne() throws EvaluationException {
		FplList list = create(0, 10, 1, 9).removeFirst();
		check(list, 1, 10);
	}

	@Test
	public void removeFirstWhenFirstBucketIsOfSizeTwo() throws EvaluationException {
		FplList list = create(0, 11, 2, 9).removeFirst();
		check(list, 1, 11);
	}

	@Test
	public void lastEmptyFails() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			FplList list = FplList.fromValues(new FplValue[0]);
			list.removeFirst();
		});
	}

	@Test
	public void removeLastWhenLastBucketIsOfSizeOne() throws EvaluationException {
		FplList list = create(0, 10, 9, 1).removeLast();
		check(list, 0, 9);
	}

	@Test
	public void removeLastWhenLastBucketIsOfSizeTwo() throws EvaluationException {
		FplList list = create(0, 11, 9, 2).removeLast();
		check(list, 0, 10);
	}

	@Test
	public void getFromEmptyList() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			FplList.EMPTY_LIST.get(0);
		});
	}

	@Test
	public void getSmallListIndexNegative() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			create(0, 4).get(-1);
		});
	}

	@Test
	public void getSmallListIndexOutOfBounds() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			create(0, 4).get(4);
		});
	}

	@Test
	public void getLargelListIndexOutOfBounds() throws EvaluationException {
		assertThrows(EvaluationException.class, () -> {
			create(0, 101).get(101);
		});
	}
}
