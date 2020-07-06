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
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

public class Parallel implements ScopePopulator {
	private static ForkJoinPool pool = ForkJoinPool.commonPool();

	@Override
	public void populate(Scope scope) throws ScopeException {

		// TODO: Documentation!
		scope.define(new AbstractFunction("parallel", comment(""), true, "code...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				@SuppressWarnings("unchecked")
				ForkJoinTask<FplValue>[] tasks = new ForkJoinTask[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					final FplValue value = parameters[i];
					tasks[i] = pool.submit(new RecursiveTask<FplValue>() {
						private static final long serialVersionUID = -5037994686972663758L;

						@Override
						protected FplValue compute() {
							try {
								return value.evaluate(scope);
							} catch (EvaluationException e) {
								throw new TunnelException(e);
							}
						}
					});
				}
				FplValue[] values = new FplValue[parameters.length];
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

		scope.define(new AbstractFunction("parallel-map", comment(""), false, "function", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplLambda function = evaluateToLambda(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				int size = list.size();
				@SuppressWarnings("unchecked")
				ForkJoinTask<FplValue>[] tasks = new ForkJoinTask[size];
				int i = 0;
				for (FplValue value : list) {
					tasks[i++] = pool.submit(new RecursiveTask<FplValue>() {
						private static final long serialVersionUID = -5037994686972663758L;

						@Override
						protected FplValue compute() {
							try {
								return function.call(scope, new FplValue[] { value });
							} catch (EvaluationException e) {
								throw new TunnelException(e);
							}
						}
					});
				}
				FplValue[] values = new FplValue[size];
				for (i = 0; i < size; i++) {
					try {
						values[i] = tasks[i].get();
					} catch (InterruptedException | ExecutionException e) {
						throw new EvaluationException(e);
					}
				}
				return FplList.fromValues(values);
			}
		});

		scope.define(new AbstractFunction("parallel-for-each", comment(""), true, "code...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplLambda function = evaluateToLambda(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				int size = list.size();
				@SuppressWarnings("unchecked")
				ForkJoinTask<FplValue>[] tasks = new ForkJoinTask[size];
				int i = 0;
				for (FplValue value : list) {
					tasks[i++] = pool.submit(new RecursiveTask<FplValue>() {
						private static final long serialVersionUID = -5037994686972663758L;

						@Override
						protected FplValue compute() {
							try {
								return function.call(scope, new FplValue[] { value });
							} catch (EvaluationException e) {
								throw new TunnelException(e);
							}
						}
					});
				}
				FplValue value = null;
				for (i = 0; i < size; i++) {
					try {
						// The last value wins
						value = tasks[i].get();
					} catch (InterruptedException | ExecutionException e) {
						throw new EvaluationException(e);
					}
				}
				return value;
			}
		});
	}
}
