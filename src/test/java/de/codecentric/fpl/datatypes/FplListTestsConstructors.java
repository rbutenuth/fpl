package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class FplListTestsConstructors extends AbstractFplListTest {
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

}
