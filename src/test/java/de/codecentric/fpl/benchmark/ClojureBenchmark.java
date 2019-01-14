package de.codecentric.fpl.benchmark;

import de.codecentric.fpl.clojure.ClojurePersistentVector;

public class ClojureBenchmark {

	public static Runner createPersistentVectorCons() {
		return new AbstractRunner() {

			@Override
			public void run() {
				ClojurePersistentVector vector = ClojurePersistentVector.EMPTY;
				for (int i = 0; i < problemSize; i++) {
					vector = vector.cons(Long.valueOf(i));
				}
			}
		};
	}

	public static Runner deconstruct() {
		return new AbstractRunner() {
			private ClojurePersistentVector list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = ClojurePersistentVector.EMPTY;
				for (int i = 0; i < problemSize; i++) {
					list = list.cons(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use((Long) list.nth(list.count() - 1));
					list = list.pop();
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

	public static Runner iterator() {
		return new AbstractRunner() {
			private ClojurePersistentVector list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = ClojurePersistentVector.EMPTY;
				for (int i = 0; i < problemSize; i++) {
					list = list.cons(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (Object v : list) {
					sink.use((Long) v);
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

	public static Runner getAll() {
		return new AbstractRunner() {
			private ClojurePersistentVector list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = ClojurePersistentVector.EMPTY;
				for (int i = 0; i < problemSize; i++) {
					list = list.cons(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use((Long) list.nth(i));
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
