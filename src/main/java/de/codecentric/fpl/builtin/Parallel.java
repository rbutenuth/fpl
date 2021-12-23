package de.codecentric.fpl.builtin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ScopePopulator;
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

		scope.define(new AbstractFunction("thread-pool-size", "Create a new thread-pool with the given size.", "size") {
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
				"Evaluate the code in parallel and return a list with the evaluation results.", "code...") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {

				List<RecursiveTask<FplValue>> tasks = new ArrayList<>(parameters.length);
				for (int i = 0; i < parameters.length; i++) {
					final FplValue value = parameters[i];
					tasks.add(new RecursiveTask<FplValue>() {
						private static final long serialVersionUID = -5037994686972663758L;

						@Override
						protected FplValue compute() {
							return evaluateToAny(scope, value);
						}
					});
				}

				return executeTasks(tasks);
			}
		});

		scope.define(new AbstractFunction("parallel-map",
				"Apply a function parallel to all list elements and return list with applied elements", "function",
				"list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				List<RecursiveTask<FplValue>> tasks = new ArrayList<>(list.size());
				for (FplValue value : list) {
					tasks.add(new RecursiveTask<FplValue>() {
						private static final long serialVersionUID = -5037994686972663758L;

						@Override
						protected FplValue compute() {
							return function.call(scope, new FplValue[] { value });
						}
					});
				}
				return executeTasks(tasks);
			}
		});

		scope.define(new AbstractFunction("parallel-for-each",
				"Apply a function parallel to all list elements, return last result", "function", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Function function = evaluateToFunction(scope, parameters[0]);
				FplList list = evaluateToList(scope, parameters[1]);
				int size = list.size();
				if (size == 0) {
					return null;
				} else {
					List<RecursiveTask<FplValue>> tasks = new ArrayList<>(size);
					for (FplValue value : list) {
						tasks.add(new RecursiveTask<FplValue>() {
							private static final long serialVersionUID = -5037994686972663758L;

							@Override
							protected FplValue compute() {
								return function.call(scope, new FplValue[] { value });
							}
						});
					}
					return executeTasks(tasks).last();
				}
			}
		});

		scope.define(new AbstractFunction("create-future",
				"Create a future. Returns a function which waits for the result.", "code") {
			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				ForkJoinTask<FplValue> task = engine.getPool().submit(new RecursiveTask<FplValue>() {
					private static final long serialVersionUID = -5037994686972663758L;

					@Override
					protected FplValue compute() {
						return evaluateToAny(scope, parameters[0]);
					}
				});
				return new AbstractFunction("future", "future") {

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

	private FplList executeTasks(List<RecursiveTask<FplValue>> tasks) throws EvaluationException {
		int size = tasks.size();
		if (ForkJoinTask.inForkJoinPool()) {
			ForkJoinTask.invokeAll(tasks);
		} else {
			for (RecursiveTask<FplValue> task : tasks) {
				engine.getPool().execute(task);
			}
		}
		return FplList.fromIterator(new Iterator<FplValue>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < size;
			}

			@Override
			public FplValue next() {
				try {
					return tasks.get(i++).get();
				} catch (InterruptedException | ExecutionException e) {
					throw new EvaluationException(e);
				}
			}
		}, size);
	}
}
