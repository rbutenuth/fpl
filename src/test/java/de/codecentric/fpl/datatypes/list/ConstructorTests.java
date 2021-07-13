package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;

public class ConstructorTests extends AbstractListTest {
	@Test
	public void empty() throws EvaluationException {
		FplList list = FplList.fromValues(new FplValue[0]);
		assertEquals(0, list.size());
		assertFalse(list.iterator().hasNext());
		assertEquals(0, list.bucketSizes().length);
	}

	@Test
	public void elementConstructor() throws EvaluationException {
		FplList list = FplList.fromValue(value(42));
		assertEquals(1, list.size());
		assertEquals(value(42), list.get(0));
		assertEquals(1, list.bucketSizes().length);
	}

	@Test
	public void emptyListConstructor() throws EvaluationException {
		FplList list = FplList.fromValues(Collections.emptyList());
		assertEquals(0, list.size());
	}

	@Test
	public void bigArrayConstructor() throws EvaluationException {
		FplValue[] values = new FplValue[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		FplList list = FplList.fromValues(values);
		check(list, 0, values.length);
		assertEquals(1, list.bucketSizes().length);
	}

	@Test
	public void bigListConstructor() throws EvaluationException {
		FplValue[] values = new FplValue[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		FplList list = FplList.fromValues(Arrays.asList(values));
		check(list, 0, values.length);
		assertEquals(1, list.bucketSizes().length);
	}

	@Test
	public void badShape() throws EvaluationException {
		assertThrows(IllegalArgumentException.class, () -> {
			FplValue[] values = new FplValue[0];
			int[] bucketSizes = new int[1];
			bucketSizes[0] = 1;
			FplList.fromValuesWithShape(values, bucketSizes);
		});
	}

	@Test
	public void emptyFromIterator() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 0));
		check(list, 0, 0);
	}

	@Test
	public void fromIteratorOneBucket() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 5));
		check(list, 0, 5);
		checkSizes(list, 5);
	}

	@Test
	public void fromIteratorTwoBuckets() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 10));
		check(list, 0, 10);
		checkSizes(list, 8, 2);
	}

	@Test
	public void fromIteratorThreeBuckets() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 163));
		check(list, 0, 163);
		checkSizes(list, 128, 32, 3);
	}

	@Test
	public void fromIteratorThreeBucketsWithLastFull() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 167));
		check(list, 0, 167);
		checkSizes(list, 128, 32, 7);
	}

	@Test
	public void fromIteratorWithBadSize() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			FplList.fromIterator(createIterator(0, 2), 1);
		});
	}

	private Iterator<FplValue> createIterator(int from, int to) {
		return new Iterator<FplValue>() {
			int nextValue = from;

			@Override
			public FplValue next() {
				if (hasNext()) {
					return FplInteger.valueOf(nextValue++);
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public boolean hasNext() {
				return nextValue < to;
			}
		};
	}
}
