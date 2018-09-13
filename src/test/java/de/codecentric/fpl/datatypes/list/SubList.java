package de.codecentric.fpl.datatypes.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class SubList extends AbstractListTest {
	@Test(expected = EvaluationException.class)
	public void subListBadRange() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list.subList(10, 0);
	}

	@Test(expected = EvaluationException.class)
	public void subListNegativeFrom() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		list.subList(-1, 3);
	}

	@Test(expected = EvaluationException.class)
	public void subListEndBeyondEndOfList() throws EvaluationException {
		FplList list = create(1, 10);
		list.subList(3, 12);
	}

	@Test
	public void subListOfShortList() throws EvaluationException {
		FplList list = create(1, 10);
		list = list.subList(3, 5);
		check(4, 5, list);
	}

	@Test
	public void subListCompleteFromOneBucket() throws EvaluationException {
		for (int size = 10; size < 100; size++) {
			FplList list = create(1, size);
			list = list.subList(0, size);
			check(1, size, list);
		}
	}

	@Test
	public void subListCompleteFromShortList() throws EvaluationException {
		FplList list = create(1, 7);
		list = list.subList(0, 4);
		check(1, 4, list);
	}

	@Test
	public void subListCompleteFromSeveral() throws EvaluationException {
		FplList list = FplList.EMPTY_LIST;
		int size = 100;
		for (int i = 1; i <= size; i++) {
			list = list.addAtEnd(value(i));
		}
		list = list.subList(0, size);
		check(1, size, list);
	}

	@Test
	public void fromEqualsToResultsInEmpty() throws EvaluationException {
		FplList list = create(0, 9);
		assertEquals(0, list.subList(3, 3).size());
	}

}
