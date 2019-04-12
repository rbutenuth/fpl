package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class AbstractListTest {

	/**
	 * @param list  List to check, must contain elements from <code>start</code> and
	 *              <code>end</code>
	 * @param from First element (including)
	 * @param to   Last element in list (excluding)
	 */
	public static void check(FplList list, int from, int to) throws EvaluationException {
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

	public static void checkSizes(FplList list, int... sizes) throws EvaluationException {
		int[] listSizes = list.bucketSizes();
		assertEquals("Wrong number of buckets", sizes.length, listSizes.length);
		for (int i = 0; i < listSizes.length; i++) {
			assertEquals("Size of bucket " + i, sizes[i], listSizes[i]);
		}
	}
	
	public static FplList create(int from, int to, int... bucketSizes) {
		return FplList.fromValuesWithShape(createValues(from, to), bucketSizes);
	}

	/**
	 * @param from first element of generated list (including)
	 * @param to   last element of generated list (excluding)
	 * @return List of {@link FplInteger}, including <code>start</code> and
	 *         <code>end</code>
	 */
	public static FplList create(int from, int to) {
		return FplList.fromValues(createValues(from, to));
	}

	public static  FplValue[] createValues(int from, int to) {
		FplValue[] values = new FplValue[to - from];
		for (int i = from, j = 0; i < to; i++, j++) {
			values[j] = value(i);
		}
		return values;
	}

	public static FplInteger value(int i) {
		return FplInteger.valueOf(i);
	}
}
