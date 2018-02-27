package de.codecentric.fpl.benchmark;

import java.util.ArrayList;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplList;
import de.codecentric.fpl.datatypes.FplValue;

public class FplListBenchmark {

	public static Runner createFplistAdd() {
		return new AbstractRunner() {

			@Override
			public void run() {
				FplList list = FplList.EMPTY_LIST;
				for (int i = 0; i < problemSize; i++) {
					list = list.addAtEnd(FplInteger.valueOf(i));
				}
			}
		};
	}

	public static Runner createFplListGetAll() {
		return new AbstractRunner() {
			private FplList list;
			private Sink<FplValue> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = FplList.EMPTY_LIST;
				for (int i = 0; i < problemSize; i++) {
					list = list.addAtEnd(FplInteger.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				try {
					for (int i = 0; i < problemSize; i++) {
						sink.use(list.get(i));
					}
				} catch (EvaluationException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void cleanup() {
				super.cleanup();
				list = null;
				sink = null;
			}
		};
	}

	public static Runner createFplListDeconstruct() {
		return new AbstractRunner() {
			private FplList list;
			private Sink<FplValue> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = FplList.EMPTY_LIST;
				for (int i = 0; i < problemSize; i++) {
					list = list.addAtEnd(FplInteger.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				try {
					for (int i = 0; i < problemSize; i++) {
						sink.use(list.first());
						list = list.removeFirst();
					}
				} catch (EvaluationException e) {
					throw new RuntimeException(e);
				}

			}

			@Override
			public void cleanup() {
				super.cleanup();
				list = null;
				sink = null;
			}
		};
	}
}
