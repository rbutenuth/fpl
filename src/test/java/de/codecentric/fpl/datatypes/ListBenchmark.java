package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.StopWatch;

public class ListBenchmark {
	private static int COUNT = 200_000;

	public static void main(String[] args) {
		JavaUtilListBenchmarkAddGet addGet = new JavaUtilListBenchmarkAddGet(new ArrayList<>());
		StopWatch sw = new StopWatch().start();
		addGet.run();
		long time = sw.stop();
		System.out.println("ArrayList, add/get: " + time + "ms");

		if (COUNT < 1_000_000) {
			addGet = new JavaUtilListBenchmarkAddGet(new LinkedList<>());
			sw = new StopWatch().start();
			addGet.run();
			time = sw.stop();
			System.out.println("LinkedList, add/get: " + time + "ms");
		}

		FplListBenchmarkAddGet fplAddGet = new FplListBenchmarkAddGet();
		sw = new StopWatch().start();
		fplAddGet.run();
		time = sw.stop();
		System.out.println("FplList, add/get: " + time + "ms");

		JavaUtilListBenchmarkQueue queue = new JavaUtilListBenchmarkQueue(new ArrayList<>());
		sw = new StopWatch().start();
		queue.run();
		time = sw.stop();
		System.out.println("ArrayList, queue: " + time + "ms");

		if (COUNT < 1_000_000) {
			queue = new JavaUtilListBenchmarkQueue(new LinkedList<>());
			sw = new StopWatch().start();
			queue.run();
			time = sw.stop();
			System.out.println("LinkedList, queue: " + time + "ms");
		}

		FplListBenchmarkQueue fplQueue = new FplListBenchmarkQueue();
		sw = new StopWatch().start();
		fplQueue.run();
		time = sw.stop();
		System.out.println("FplList, queue: " + time + "ms");
	}

	private static class JavaUtilListBenchmarkAddGet implements Runnable {
		private List<FplValue> list = new ArrayList<>();

		public JavaUtilListBenchmarkAddGet(List<FplValue> list) {
			this.list = list;
		}

		@Override
		public void run() {
			for (int i = 0; i < COUNT; i++) {
				list.add(FplInteger.valueOf(i));
			}
			for (int i = 0; i < COUNT; i++) {
				sink(list.get(i));
			}
		}
	}

	private static class JavaUtilListBenchmarkQueue implements Runnable {
		private List<FplValue> list = new ArrayList<>();

		public JavaUtilListBenchmarkQueue(List<FplValue> list) {
			this.list = list;
		}

		@Override
		public void run() {
			for (int i = 0; i < COUNT; i++) {
				list.add(FplInteger.valueOf(i));
			}
			for (int i = 0; i < COUNT; i++) {
				sink(list.remove(0));
			}
		}
	}

	private static class FplListBenchmarkAddGet implements Runnable {
		private FplList list = FplList.EMPTY_LIST;

		@Override
		public void run() {
			try {
				for (int i = 0; i < COUNT; i++) {
					list = list.addAtEnd(FplInteger.valueOf(i));
				}
				for (int i = 0; i < COUNT; i++) {
					sink(list.get(i));
				}
			} catch (EvaluationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class FplListBenchmarkQueue implements Runnable {
		private FplList list = FplList.EMPTY_LIST;

		@Override
		public void run() {
			try {
				for (int i = 0; i < COUNT; i++) {
					list = list.addAtEnd(FplInteger.valueOf(i));
				}
				for (int i = 0; i < COUNT; i++) {
					sink(list.first());
					list = list.removeFirst();
				}
			} catch (EvaluationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unused")
	private static volatile FplValue svalue;

	private static void sink(FplValue value) {
		svalue = value;
	}
}
