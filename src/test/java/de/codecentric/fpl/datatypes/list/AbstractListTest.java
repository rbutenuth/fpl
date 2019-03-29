package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class AbstractListTest {

	/**
	 * @param from First element (including)
	 * @param to   Last element in list (excluding)
	 * @param list  List to check, must contain elements from <code>start</code> and
	 *              <code>end</code>
	 */
	protected void check(int from, int to, FplList list) throws EvaluationException {
		assertEquals("List size", to - from, list.size());
		Iterator<FplValue> iter = list.iterator();
		int value = from;
		while (iter.hasNext()) {
			FplInteger next = (FplInteger) iter.next();
			assertEquals(value, next.getValue());
			value++;
		}
		assertEquals(to, value);
	}

	protected FplList create(int from, int to, int... bucketSizes) {
		return new FplList(createValues(from, to), bucketSizes);
	}

	/**
	 * @param from first element of generated list (including)
	 * @param to   last element of generated list (excluding)
	 * @return List of {@link FplInteger}, including <code>start</code> and
	 *         <code>end</code>
	 */
	protected FplList create(int from, int to) {
		return new FplList(createValues(from, to));
	}

	private FplValue[] createValues(int from, int to) {
		FplValue[] values = new FplValue[to - from];
		for (int i = from, j = 0; i < to; i++, j++) {
			values[j] = value(i);
		}
		return values;
	}

	protected FplInteger value(int i) {
		return FplInteger.valueOf(i);
	}
}
