package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;

public class MapTest extends AbstractListTest {

	@Test
	public void map() throws EvaluationException {
		FplList list = create(0, 3);
		list = list.map(new Function<FplValue, FplValue>() {

			@Override
			public FplValue apply(FplValue v) {
				FplInteger i = (FplInteger) v;
				return FplInteger.valueOf(i.getValue() + 1);
			}
		});
		check(list, 1, 4);
	}

	@Test
	public void flatMap() throws EvaluationException {
		FplList list = FplList.fromValues(create(0, 3), create(3, 7));
		list = list.flatMap(new Function<FplValue, FplValue>() {

			@Override
			public FplValue apply(FplValue t) {
				return t;
			}
		});
		check(list, 0, 7);
	}

	@Test
	public void flatMapElementNotAList() throws EvaluationException {
		FplList list = create(0, 7);
		assertThrows(TunnelException.class, () -> {
			list.flatMap(new Function<FplValue, FplValue>() {

				@Override
				public FplValue apply(FplValue t) {
					return t;
				}
			});
		});
	}
}
