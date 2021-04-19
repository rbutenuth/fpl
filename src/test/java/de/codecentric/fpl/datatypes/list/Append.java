package de.codecentric.fpl.datatypes.list;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;

public class Append extends AbstractListTest {
	
	@Test
	public void firstEmpty() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST.append(create(0, 5));
		check(list, 0, 5);
	}

	@Test
	public void secondEmpty() throws EvaluationException {
		FplList list = create(0, 5).append(FplList.EMPTY_LIST);
		check(list, 0, 5);
	}

	@Test
	public void secondNull() throws EvaluationException {
		FplList list = create(0, 5).append(null);
		check(list, 0, 5);
	}

	@Test
	public void linearLinear2Linear() throws EvaluationException {
		FplList list = create(0, 4).append(create(4, 8));
		check(list, 0, 8);
	}

	@Test
	public void linearLinear2Shaped() throws EvaluationException {
		FplList list = create(0, 6).append(create(6, 13));
		check(list, 0, 13);
	}

	@Test
	public void shapedLinearFitsInLast2Shaped() throws EvaluationException {
		FplList list = create(0, 36, 32, 4).append(create(36, 39));
		check(list, 0, 39);
	}

	@Test
	public void shapedLinearDoesNotFitInLast2Shaped() throws EvaluationException {
		FplList list = create(0, 36, 32, 4).append(create(36, 44));
		check(list, 0, 44);
	}

	@Test
	public void linearShapedFitsInFirst2Shaped() throws EvaluationException {
		FplList list = create(0, 6).append(create(6, 106, 1, 99));
		check(list, 0, 106);
	}

	@Test
	public void linearShapedDoesNotFitInFirst2Shaped() throws EvaluationException {
		FplList list = create(0, 6).append(create(6, 106, 8, 92));
		check(list, 0, 106);
	}

	@Test
	public void shapedShapedBucketsCombinable() throws EvaluationException {
		FplList list = create(0, 10, 6, 4).append(create(10, 20, 4, 6));
		check(list, 0, 20);
	}

	@Test
	public void shapedShapedBucketsCombinableNeedReshape() throws EvaluationException {
		FplList list = create(0, 6, 1, 1, 4).append(create(6, 12, 4, 1, 1));
		check(list, 0, 12);
	}

	@Test
	public void shapedShapedWithoutReshape() throws EvaluationException {
		FplList list = create(0, 16, 8, 8).append(create(16, 32, 8, 8));
		check(list, 0, 32);
	}

	@Test
	public void shapedShapedWithReshape() throws EvaluationException {
		FplList list = create(0, 16, 2, 2, 2, 2, 8).append(create(16, 32, 8, 2, 2, 2, 2));
		check(list, 0, 32);
	}

	@Test
	public void shapedShapedWithReshape2() throws EvaluationException {
		FplList list = create(0, 16, 8, 2, 2, 2, 2).append(create(16, 32, 2, 2, 2, 2, 8));
		check(list, 0, 32);
	}

}
