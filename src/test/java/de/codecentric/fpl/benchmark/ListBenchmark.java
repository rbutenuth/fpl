package de.codecentric.fpl.benchmark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class ListBenchmark {
	private static int MAX_COUNT = 16 * 1048576;
	private static long MAX_TIME = 1000L;
	private static LinkedHashMap<String, ArrayList<Result>> results = new LinkedHashMap<>();

	public static void main(String[] args) {
		for (int i = 0; i < 5; i++) {
			System.out.println("Run " + i);

			//mergeResults("ArrayList, construct", run(JavaUtilBenchmark.createArrayListAdd()));
			//mergeResults("ArrayList, get all", run(JavaUtilBenchmark.arrayListGetAll()));
			//mergeResults("ArrayList, iterator", run(JavaUtilBenchmark.arrayListIterator()));
			//mergeResults("ArrayList, rec. join", run(JavaUtilBenchmark.createArrayListJoin()));
			
			//mergeResults("ArrayList, deconstruct", run(JavaUtilBenchmark.createArrayListDeconstruct()));
			
			//mergeResults("LinkedList, construct", run(JavaUtilBenchmark.createLinkedListAdd()));
			//mergeResults("LinkedList, get all", run(JavaUtilBenchmark.createLinkedListGetAll()));
			//mergeResults("LinkedList, deconstruct", run(JavaUtilBenchmark.createLinkedListDeconstruct()));
			
			//mergeResults("ArrayDeque, construct", run(JavaUtilBenchmark.createArrayDequeAdd()));
			//mergeResults("ArrayDeque, deconstruct", run(JavaUtilBenchmark.createArrayDequeDeconstruct()));
			
			//mergeResults("FplList, construct", run(FplListBenchmark.createFplistAdd()));
			mergeResults("FplList, rec. join", run(FplListBenchmark.createFplListJoin()));
			//mergeResults("FplList, get all", run(FplListBenchmark.fplListGetAll()));
			//mergeResults("FplList, iterator", run(FplListBenchmark.fplListIterator()));
			//mergeResults("FplList, deconstruct", run(FplListBenchmark.createFplListDeconstruct()));
		}

		printAllResults();
	}

	private static void mergeResults(String name, ArrayList<Result> run) {
		System.out.println("Merge " + name);
		ArrayList<Result> existingRun = results.get(name);
		if (existingRun == null) {
			results.put(name, run);
		} else {
			merge(existingRun, run);
		}
	}

	private static void merge(ArrayList<Result> existingRun, ArrayList<Result> run) {
		while (existingRun.size() > run.size()) {
			existingRun.remove(existingRun.size() - 1);
		}
		for (int i = 0; i < existingRun.size(); i++) {
			Result e = existingRun.get(i);
			Result r = run.get(i);
			if (r.getMillis() < e.getMillis()) {
				e.setMillis(r.getMillis());
			}
		}
	}

	private static ArrayList<Result> run(Runner candidate) {
		ArrayList<Result> results = new ArrayList<>();
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
	
	private static void printAllResults() {
		for (Entry<String, ArrayList<Result>> entry : results.entrySet()) {
			printResults(entry.getKey(), entry.getValue());
		}
	}

	private static void printResults(String title, List<Result> results) {
		StringBuilder line1 = new StringBuilder();
		StringBuilder line2 = new StringBuilder();
		line1.append(title).append('\t');
		line2.append(title).append('\t');
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
		private long millis;
		
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
		
		public void setMillis(long millis) {
			this.millis = millis;
		}	
	}
}
