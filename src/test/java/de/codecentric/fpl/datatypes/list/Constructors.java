package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class Constructors extends AbstractListTest {
	@Test
	public void empty() throws EvaluationException {
		FplList list = new FplList(new FplValue[0]);
		assertEquals(0, list.size());
		assertFalse(list.iterator().hasNext());
		assertEquals(0, list.numberOfBuckets());
	}

	@Test
	public void elementConstructor() throws EvaluationException {
		FplList list = new FplList(value(42));
		assertEquals(1, list.size());
		assertEquals(value(42), list.get(0));
		assertEquals(0, list.numberOfBuckets());
	}

	@Test
	public void emptyListConstructor() throws EvaluationException {
		FplList list = new FplList(Collections.emptyList());
		assertEquals(0, list.size());
	}
	
	@Test
	public void bigArrayConstructor() throws EvaluationException {
		FplValue[] values = new FplValue[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		FplList list = new FplList(values);
		check(0, values.length - 1, list);
		assertEquals(1, list.numberOfBuckets());
	}

	@Test
	public void smallListConstructor() throws EvaluationException {
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
	public void bigListConstructor() throws EvaluationException {
		FplValue[] values = new FplValue[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		FplList list = new FplList(Arrays.asList(values));
		check(0, values.length - 1, list);
		assertEquals(1, list.numberOfBuckets());
	}

	@Test(expected = IllegalArgumentException.class)
	public void badShape() throws EvaluationException {
		FplValue[] values = new FplValue[0];
		int[] bucketSizes = new int[1];
		bucketSizes[0] = 1;
		new FplList(values, bucketSizes);
	}
}
