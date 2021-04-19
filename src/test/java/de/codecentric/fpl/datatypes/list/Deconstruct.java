package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;

public class Deconstruct extends AbstractListTest {
	@Test
	public void removeFirstSizeOne() throws EvaluationException {
		FplList list = FplList.fromValues(new FplValue[1]);
		FplList rest = list.removeFirst();
		assertEquals(0, rest.size());
	}

	@Test
	public void removeFirstSizeTwo() throws EvaluationException {
		FplList list = FplList.fromValue(value(1)).addAtEnd(value(2));
		FplList rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void removeFirstSizeHundred() throws EvaluationException {
		FplList list = create(0, 100);
		FplList rest = list.removeFirst();
		check(rest, 1, 100);
	}

	@Test
	public void removeFirstSizeFiftyAppendFifty() throws EvaluationException {
		FplList list = create(0, 50).append(create(50, 100));
		FplList rest = list.removeFirst();
		check(rest, 1, 100);
	}

	@Test
	public void removeFirstSizeTwoFromAppend() throws EvaluationException {
		FplList list = create(1, 2).append(create(2, 3));
		FplList rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void removeLastSizeOne() throws EvaluationException {
		FplList list = FplList.fromValues(new FplValue[1]);
		FplList rest = list.removeLast();
		assertEquals(0, rest.size());
	}

	@Test
	public void removeLastSizeTwo() throws EvaluationException {
		FplList list = FplList.fromValue(value(1)).addAtEnd(value(2));
		FplList rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void removeLastSizeHundred() throws EvaluationException {
		FplList list = create(0, 100);
		FplList rest = list.removeLast();
		check(rest, 0, 99);
	}

	@Test
	public void removeLastSizeFiftyAppendFifty() throws EvaluationException {
		FplList list = create(0, 50).append(create(50, 100));
		FplList rest = list.removeLast();
		check(rest, 0, 99);
	}

	@Test
	public void removeLastSizeTwoFromAppend() throws EvaluationException {
		FplList list = create(1, 2).append(create(2, 3));
		FplList rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void lowerHalfFromEmptyList() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.lowerHalf();
		assertTrue(list.isEmpty());
	}

	@Test
	public void lowerHalfFromListWithSizeOne() throws EvaluationException {
		FplList list = create(0, 1).lowerHalf();
		assertTrue(list.isEmpty());
	}

	@Test
	public void lowerHalfFromSmallList() throws EvaluationException {
		FplList list = create(0, 16);
		FplList lower = list.lowerHalf();
		check(lower, 0, 8);
		checkSizes(lower, 8);
	}

	@Test
	public void lowerHalfFromMediumList() throws EvaluationException {
		FplList list = create(0, 100, 30, 40, 30);
		FplList lower = list.lowerHalf();
		check(lower, 0, 50);
		checkSizes(lower, 6, 6, 6, 7, 6, 6, 6, 7);
	}

	@Test
	public void lowerHalfFromBigList() throws EvaluationException {
		FplList list = create(0, 1000000);
		FplList lower = list.lowerHalf();
		check(lower, 0, 500000);
		assertEquals(32, lower.bucketSizes().length);
		lower = lower.lowerHalf();
		check(lower, 0, 250000);
		assertEquals(16, lower.bucketSizes().length);
		while (lower.bucketSizes().length > 1) {
			lower = lower.lowerHalf();
		}
		lower = lower.lowerHalf();
		assertEquals(16, lower.bucketSizes().length);
	}

	@Test
	public void upperHalfFromEmptyList() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.upperHalf();
		assertTrue(list.isEmpty());
	}

	@Test
	public void upperHalfFromListWithSizeOne() throws EvaluationException {
		FplList list = create(0, 1).upperHalf();
		check(list, 0, 1);
	}

	@Test
	public void upperHalfFromSmallList() throws EvaluationException {
		FplList list = create(0, 16);
		FplList upper = list.upperHalf();
		check(upper, 8, 16);
		checkSizes(upper, 8);
	}

	@Test
	public void upperHalfFromMediumList() throws EvaluationException {
		FplList list = create(0, 100, 30, 40, 30);
		FplList upper = list.upperHalf();
		check(upper, 50, 100);
		checkSizes(upper, 6, 6, 6, 7, 6, 6, 6, 7);
	}

	@Test
	public void upperHalfFromBigList() throws EvaluationException {
		FplList list = create(0, 1000000);
		FplList upper = list.upperHalf();
		check(upper, 500000, 1000000);
		assertEquals(32, upper.bucketSizes().length);
		upper = upper.upperHalf();
		check(upper, 750000, 1000000);
		assertEquals(16, upper.bucketSizes().length);
	}
}
