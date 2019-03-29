package de.codecentric.fpl.datatypes.list;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class Append extends AbstractListTest {
	
	@Test
	public void firstEmpty() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.append(create(0, 5));
		check(0, 4, list);
	}

	@Test
	public void secondEmpty() throws EvaluationException {
		FplList list = create(0, 5).append(FplList.EMPTY_LIST);
		check(0, 4, list);
	}

	@Test
	public void secondNull() throws EvaluationException {
		FplList list = create(0, 5).append(null);
		check(0, 4, list);
	}

	@Test
	public void linearLinear2Linear() throws EvaluationException {
		FplList list = create(0, 4).append(create(4, 8));
		check(0, 7, list);
	}

	@Test
	public void linearLinear2Shaped() throws EvaluationException {
		FplList list = create(0, 6).append(create(6, 13));
		check(0, 12, list);
	}

	@Test
	public void shapedLinearFitsInLast2Shaped() throws EvaluationException {
		FplList list = create(0, 36, 32, 4).append(create(36, 39));
		check(0, 38, list);
	}

	@Test
	public void shapedLinearDoesNotFitInLast2Shaped() throws EvaluationException {
		FplList list = create(0, 36, 32, 4).append(create(36, 44));
		check(0, 43, list);
	}

	@Test
	public void linearShapedFitsInFirst2Shaped() throws EvaluationException {
		FplList list = create(0, 6).append(create(6, 106, 1, 99));
		check(0, 105, list);
	}

	@Test
	public void linearShapedDoesNotFitInFirst2Shaped() throws EvaluationException {
		FplList list = create(0, 6).append(create(6, 106, 8, 92));
		check(0, 105, list);
	}

	@Test
	public void shapedShapedBucketsCombinable() throws EvaluationException {
		FplList list = create(0, 10, 6, 4).append(create(10, 20, 4, 6));
		check(0, 19, list);
	}

	@Test
	public void shapedShapedBucketsCombinableNeedReshape() throws EvaluationException {
		FplList list = create(0, 6, 1, 1, 4).append(create(6, 12, 4, 1, 1));
		check(0, 11, list);
	}

	@Test
	public void shapedShapedWithoutReshape() throws EvaluationException {
		FplList list = create(0, 16, 8, 8).append(create(16, 32, 8, 8));
		check(0, 31, list);
	}

	@Test
	public void shapedShapedWithReshape() throws EvaluationException {
		FplList list = create(0, 16, 2, 2, 2, 2, 8).append(create(16, 32, 8, 2, 2, 2, 2));
		check(0, 31, list);
	}

	@Test
	public void shapedShapedWithReshape2() throws EvaluationException {
		FplList list = create(0, 16, 8, 2, 2, 2, 2).append(create(16, 32, 2, 2, 2, 2, 8));
		check(0, 31, list);
	}

}
