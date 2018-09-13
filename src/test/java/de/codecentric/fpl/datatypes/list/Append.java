package de.codecentric.fpl.datatypes.list;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class Append extends AbstractListTest {
	
	@Test
	public void firstEmpty() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.append(create(0, 4));
		check(0, 4, list);
	}

	@Test
	public void secondEmpty() throws EvaluationException {
		FplList list = create(0, 4).append(FplList.EMPTY_LIST);
		check(0, 4, list);
	}

	@Test
	public void secondNull() throws EvaluationException {
		FplList list = create(0, 4).append(null);
		check(0, 4, list);
	}

	@Test
	public void linearLinear2Linear() throws EvaluationException {
		FplList list = create(0, 3).append(create(4, 7));
		check(0, 7, list);
	}

	@Test
	public void linearLinear2Shaped() throws EvaluationException {
		FplList list = create(0, 5).append(create(6, 12));
		check(0, 12, list);
	}

	@Test
	public void shapedLinearFitsInLast2Shaped() throws EvaluationException {
		FplList list = create(0, 35, 32, 4).append(create(36, 38));
		check(0, 38, list);
	}

	@Test
	public void shapedLinearDoesNotFitInLast2Shaped() throws EvaluationException {
		FplList list = create(0, 35, 32, 4).append(create(36, 43));
		check(0, 43, list);
	}

	@Test
	public void linearShapedFitsInFirst2Shaped() throws EvaluationException {
		FplList list = create(0, 5).append(create(6, 105, 1, 99));
		check(0, 105, list);
	}

	@Test
	public void linearShapedDoesNotFitInFirst2Shaped() throws EvaluationException {
		FplList list = create(0, 5).append(create(6, 105, 8, 92));
		check(0, 105, list);
	}

	@Test
	public void shapedShapedBucketsCombinable() throws EvaluationException {
		FplList list = create(0, 9, 6, 4).append(create(10, 19, 4, 6));
		check(0, 19, list);
	}

	@Test
	public void shapedShapedBucketsCombinableNeedReshape() throws EvaluationException {
		FplList list = create(0, 5, 1, 1, 4).append(create(6, 11, 4, 1, 1));
		check(0, 11, list);
	}

	@Test
	public void shapedShapedWithoutReshape() throws EvaluationException {
		FplList list = create(0, 15, 8, 8).append(create(16, 31, 8, 8));
		check(0, 31, list);
	}

	@Test
	public void shapedShapedWithReshape() throws EvaluationException {
		FplList list = create(0, 15, 2, 2, 2, 2, 8).append(create(16, 31, 8, 2, 2, 2, 2));
		check(0, 31, list);
	}

}
