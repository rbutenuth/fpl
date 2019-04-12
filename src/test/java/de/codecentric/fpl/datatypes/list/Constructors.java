package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;

public class Constructors extends AbstractListTest {
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

	@Test(expected = IllegalArgumentException.class)
	public void badShape() throws EvaluationException {
		FplValue[] values = new FplValue[0];
		int[] bucketSizes = new int[1];
		bucketSizes[0] = 1;
		FplList.fromValuesWithShape(values, bucketSizes);
	}
	
	@Test
	public void smallFromStandardIterator() throws Exception {
		int from = 0;
		int to = 8;
		FplList list = FplList.fromIterator(new Iterator<FplValue>() {
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
		});
		check(list, 0, 8);
	}
	
	@Test
	public void smallFromIterator() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 8));
		check(list, 0, 8);
	}
	
	@Test
	public void largeFromIterator() throws Exception {
		FplList list = FplList.fromIterator(createIterator(0, 100));
		check(list, 0, 100);
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
