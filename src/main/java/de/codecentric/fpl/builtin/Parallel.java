package de.codecentric.fpl.builtin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class Parallel implements ScopePopulator {
	private static ForkJoinPool pool = ForkJoinPool.commonPool();

	@Override
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("parallel", comment(""), true, "code...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplValue[] values = new FplValue[parameters.length];
				@SuppressWarnings("unchecked") ForkJoinTask<FplValue>[] tasks = new ForkJoinTask[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					tasks[i] = pool.submit(new ParallelTask(scope, parameters[i]));
				}
				for (int i = 0; i < parameters.length; i++) {
					try {
						values[i] = tasks[i].get();
					} catch (InterruptedException | ExecutionException e) {
						throw new EvaluationException(e);
					}
				}
				return FplList.fromValues(values);
			}

		});
	}

	private static class ParallelTask extends RecursiveTask<FplValue> {
		private static final long serialVersionUID = 1335463209890401020L;
		private Scope scope;
		private FplValue value;

		public ParallelTask(Scope scope, FplValue value) {
			this.scope = scope;
			this.value = value;
		}

		@Override
		protected FplValue compute() {
			try {
				return value.evaluate(scope);
			} catch (EvaluationException e) {
				throw new TunnelException(e);
			}
		}
	}
}
