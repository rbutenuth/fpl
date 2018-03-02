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

}
