package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class AbstractFplListTest {


	/**
	 * @param start
	 *            First element
	 * @param end
	 *            Last element in list
	 * @param list
	 *            List to check
	 */
	protected void check(int start, int end, FplList list) throws EvaluationException {
		assertEquals("List size", end - start + 1, list.size());
		Iterator<FplValue> iter = list.iterator();
		int value = start;
		while (iter.hasNext()) {
			FplInteger next = (FplInteger) iter.next();
			assertEquals(value, next.getValue());
			value++;
		}
		assertEquals(end + 1, value);
	}

	protected FplList create(int start, int end) {
		FplValue[] values = new FplValue[end - start + 1];
		for (int i = start, j = 0; i <= end; i++, j++) {
			values[j] = value(i);
		}
		return new FplList(values);
	}

	protected FplInteger value(int i) {
		return FplInteger.valueOf(i);
	}
}
