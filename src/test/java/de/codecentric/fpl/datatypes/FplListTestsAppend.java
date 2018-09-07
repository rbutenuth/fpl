package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class FplListTestsAppend extends AbstractFplListTest {
	@Test
	public void testAppendFirstEmpty() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.append(create(0, 9));
		check(0, 9, list);
	}

	@Test
	public void testSecondEmpty() throws EvaluationException {
		FplList list = create(0, 9).append(FplList.EMPTY_LIST);
		check(0, 9, list);
	}

	@Test
	public void testSecondNull() throws EvaluationException {
		FplList list = create(0, 9).append(null);
		check(0, 9, list);
	}

	@Test
	public void testAppendBothNotEmpty() throws EvaluationException {
		FplList list = create(0, 5).append(create(6, 19));
		check(0, 19, list);
	}

	@Test
	public void testReshape() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		for (int i = 0; i < 10; i++) {
			list = list.append(create(i * 5, i * 5 + 4));
			check(0, (i + 1) * 5 - 1, list);
		}
		check(0, 49, list);
		// The "5" depends on BASE_SIZE and FACTOR in FplList
		assertEquals(5, list.numberOfBuckets());
	}

}
