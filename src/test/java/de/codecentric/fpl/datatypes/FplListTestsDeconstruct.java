package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;

public class FplListTestsDeconstruct extends AbstractFplListTest {
	@Test
	public void testRemoveFirstSizeOne() throws EvaluationException {
		FplList list = new FplList(new FplValue[1]);
		FplList rest = list.removeFirst();
		assertEquals(0, rest.size());
	}

	@Test
	public void testRemoveFirstSizeTwo() throws EvaluationException {
		FplList list = new FplList(value(1)).addAtEnd(value(2));
		FplList rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void testRemoveFirstSizeHundred() throws EvaluationException {
		FplList list = create(0, 99);
		FplList rest = list.removeFirst();
		check(1, 99, rest);
	}

	@Test
	public void testRemoveFirstSizeFiftyAppendFifty() throws EvaluationException {
		FplList list = create(0, 49).append(create(50, 99));
		FplList rest = list.removeFirst();
		check(1, 99, rest);
	}

	@Test
	public void testRemoveFirstSizeTwoFromAppend() throws EvaluationException {
		FplList list = create(1, 1).append(create(2, 2));
		FplList rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void testRemoveLastSizeOne() throws EvaluationException {
		FplList list = new FplList(new FplValue[1]);
		FplList rest = list.removeLast();
		assertEquals(0, rest.size());
	}

	@Test
	public void testRemoveLastSizeTwo() throws EvaluationException {
		FplList list = new FplList(value(1)).addAtEnd(value(2));
		FplList rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void testRemoveLastSizeHundred() throws EvaluationException {
		FplList list = create(0, 99);
		FplList rest = list.removeLast();
		check(0, 98, rest);
	}

	@Test
	public void testRemoveLastSizeFiftyAppendFifty() throws EvaluationException {
		FplList list = create(0, 49).append(create(50, 99));
		FplList rest = list.removeLast();
		check(0, 98, rest);
	}

	@Test
	public void testRemoveLastSizeTwoFromAppend() throws EvaluationException {
		FplList list = create(1, 1).append(create(2, 2));
		FplList rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

}
