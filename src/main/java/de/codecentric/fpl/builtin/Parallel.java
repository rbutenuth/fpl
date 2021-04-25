package de.codecentric.fpl.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.list.FplList;

public class Parallel implements ScopePopulator {
	private FplEngine engine;

	public Parallel(FplEngine engine) {
		this.engine = engine;
	}

	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("thread-pool-size", "Create a new thread-pool with the given size.",
				false, "size") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				int size = (int) evaluateToLong(scope, parameters[0]);
				ForkJoinPool oldPool = engine.getPool();
				int oldSize = oldPool.getParallelism();
				engine.setPool(new ForkJoinPool(size));
				oldPool.shutdown();
				return FplInteger.valueOf(oldSize);
			}
		});

		scope.define(new AbstractFunction("parallel",
				"Evaluate the code in parallel and return a list with the evaluation results.", true,
				"code...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {

				List<RecursiveTask<FplValue>> tasks = new ArrayList<>(parameters.length);
				for (int i = 0; i < parameters.length; i++) {
					final FplValue value = parameters[i];
					tasks.add(new RecursiveTask<FplValue>() {
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
				try {
					if (ForkJoinTask.inForkJoinPool()) {
						ForkJoinTask.invokeAll(tasks);
					} else {
						for (RecursiveTask<FplValue> task : tasks) {
							engine.getPool().execute(task);
						}
					}
					for (int i = 0; i < parameters.length; i++) {
						values[i] = tasks.get(i).get();
					}
				} catch (InterruptedException | ExecutionException e) {
					throw new EvaluationException(e);
				}
				return FplList.fromValues(values);
			}
		});

		scope.define(new AbstractFunction("parallel-map",
				"Apply a function parallel to all list elements and return list with applied elements", false,
				"function", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				int size = list.size();
				@SuppressWarnings("unchecked")
				ForkJoinTask<FplValue>[] tasks = new ForkJoinTask[size];
				int i = 0;
				for (FplValue value : list) {
					tasks[i++] = engine.getPool().submit(new RecursiveTask<FplValue>() {
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

		scope.define(new AbstractFunction("parallel-for-each",
				"Apply a function parallel to all list elements, return last result", false, "function",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				int size = list.size();
				@SuppressWarnings("unchecked")
				ForkJoinTask<FplValue>[] tasks = new ForkJoinTask[size];
				int i = 0;
				for (FplValue value : list) {
					tasks[i++] = engine.getPool().submit(new RecursiveTask<FplValue>() {
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

		scope.define(new AbstractFunction("create-future", "Create a furture. Returns a function which waits for the result.", false, "code") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				ForkJoinTask<FplValue> task = engine.getPool().submit(new RecursiveTask<FplValue>() {
					private static final long serialVersionUID = -5037994686972663758L;

					@Override
					protected FplValue compute() {
						try {
							return parameters[0].evaluate(scope);
						} catch (EvaluationException e) {
							throw new TunnelException(e);
						}
					}
				});
				return new AbstractFunction("future", "future", false) {

					@Override
					protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
						try {
							return task.get();
						} catch (InterruptedException | ExecutionException e) {
							throw new EvaluationException(e);
						}
					}
				};
			}
		});

	}
}
