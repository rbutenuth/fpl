package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class AbstractListTest {

	/**
	 * @param start First element (including)
	 * @param end   Last element in list (including)
	 * @param list  List to check, must contain elements from <code>start</code> and
	 *              <code>end</code>
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

	protected FplList create(int start, int end, int... bucketSizes) {
		return new FplList(createValues(start, end), bucketSizes);
	}

	/**
	 * @param start first element of generated list (including)
	 * @param end   last element of generated list (including)
	 * @return List of {@link FplInteger}, including <code>start</code> and
	 *         <code>end</code>
	 */
	protected FplList create(int start, int end) {
		return new FplList(createValues(start, end));
	}

	private FplValue[] createValues(int start, int end) {
		FplValue[] values = new FplValue[end - start + 1];
		for (int i = start, j = 0; i <= end; i++, j++) {
			values[j] = value(i);
		}
		return values;
	}

	protected FplInteger value(int i) {
		return FplInteger.valueOf(i);
	}
}
