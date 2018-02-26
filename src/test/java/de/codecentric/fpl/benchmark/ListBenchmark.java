package de.codecentric.fpl.benchmark;

import java.util.ArrayList;
import java.util.List;

public class ListBenchmark {
	private static int MAX_COUNT = 1048576;
	private static long MAX_TIME = 1000L;

	public static void main(String[] args) {
		printResults("ArrayList, construct", run(JavaUtilBenchmark.createArrayListAdd()));
		printResults("ArrayList, get all", run(JavaUtilBenchmark.createArrayListGetAll()));
		printResults("ArrayList, deconstruct", run(JavaUtilBenchmark.createArrayListDeconstruct()));

		printResults("LinkedList, construct", run(JavaUtilBenchmark.createLinkedListAdd()));
		printResults("LinkedList, get all", run(JavaUtilBenchmark.createLinkedListGetAll()));
		printResults("LinkedList, deconstruct", run(JavaUtilBenchmark.createLinkedListDeconstruct()));

		printResults("ArrayDeque, construct", run(JavaUtilBenchmark.createArrayDequeAdd()));
		printResults("ArrayDeque, deconstruct", run(JavaUtilBenchmark.createArrayDequeDeconstruct()));
	}

	private static List<Result> run(Runner candidate) {
		List<Result> results = new ArrayList<>();
		for (int count = 1; count <= MAX_COUNT; count *= 4) {
			StopWatch sw = new StopWatch();
			candidate.prepare(count);
			sw.start();
			candidate.run();
			sw.stop();
			candidate.cleanup();
			results.add(new Result(count, sw.totalTime()));
			if (sw.totalTime() > MAX_TIME) {
				break;
			}
		}
		return results;
	}
	
	private static void printResults(String title, List<Result> results) {
		StringBuilder line1 = new StringBuilder();
		StringBuilder line2 = new StringBuilder();
		line1.append(title).append('\t');
		for (int i = 0; i < title.length(); i++) {
			line2.append(' ');
		}
		line2.append('\t');
		for (Result r : results) {
			line1.append(r.getProblemSize()).append('\t');
			line2.append(r.getMillis()).append('\t');
		}
		System.out.println(line1);
		System.out.println(line2);
		System.out.println();
	}
	
	private static class Result {
		private final int problemSize;
		private final long millis;
		
		public Result(int problemSize, long millis) {
			this.problemSize = problemSize;
			this.millis = millis;
		}

		public int getProblemSize() {
			return problemSize;
		}

		public long getMillis() {
			return millis;
		}
	}
}
