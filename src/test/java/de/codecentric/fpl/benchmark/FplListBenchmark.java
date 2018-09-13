package de.codecentric.fpl.benchmark;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

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

	public static Runner createFplListJoin() {
		return new AbstractRunner() {

			@Override
			public void run() {
				@SuppressWarnings("unused")
				FplList list = createRecursive(0, problemSize);
			}

			private FplList createRecursive(int start, int end) {
				FplList list = FplList.EMPTY_LIST;
				if (end - start < 8) {
					for (long i = start; i < end; i++) {
						list = list.addAtEnd(FplInteger.valueOf(i));
					}
				} else {
					int split = (end + start) / 2;
					list = list.append(createRecursive(start, split));
					list = list.append(createRecursive(split, end));
				}
				return list;
			}
		};
	}

	public static Runner fplListGetAll() {
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

	public static Runner fplListIterator() {
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
				for (FplValue v : list) {
					sink.use(v);
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
