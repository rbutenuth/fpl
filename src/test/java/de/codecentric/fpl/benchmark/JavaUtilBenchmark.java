package de.codecentric.fpl.benchmark;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class JavaUtilBenchmark {

	public static Runner createArrayListAdd() {
		return new AbstractRunner() {

			@Override
			public void run() {
				List<Long> list = new ArrayList<>();
				for (int i = 0; i < problemSize; i++) {
					list.add(Long.valueOf(i));
				}
			}
		};
	}

	public static Runner createArrayListGetAll() {
		return new AbstractRunner() {
			private List<Long> list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = new ArrayList<>(problemSize);
				for (int i = 0; i < problemSize; i++) {
					list.add(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use(list.get(i));
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

	public static Runner createArrayListDeconstruct() {
		return new AbstractRunner() {
			private List<Long> list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = new ArrayList<>(problemSize);
				for (int i = 0; i < problemSize; i++) {
					list.add(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use(list.remove(0));
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
	
	public static Runner createLinkedListAdd() {
		return new AbstractRunner() {

			@Override
			public void run() {
				List<Long> list = new LinkedList<>();
				for (int i = 0; i < problemSize; i++) {
					list.add(Long.valueOf(i));
				}
			}
		};
	}

	public static Runner createLinkedListGetAll() {
		return new AbstractRunner() {
			private List<Long> list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = new LinkedList<>();
				for (int i = 0; i < problemSize; i++) {
					list.add(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use(list.get(i));
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

	public static Runner createLinkedListDeconstruct() {
		return new AbstractRunner() {
			private LinkedList<Long> list;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				list = new LinkedList<>();
				for (int i = 0; i < problemSize; i++) {
					list.add(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use(list.remove(0));
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
	
	public static Runner createArrayDequeAdd() {
		return new AbstractRunner() {

			@Override
			public void run() {
				Deque<Long> deque = new ArrayDeque<>();
				for (int i = 0; i < problemSize; i++) {
					deque.add(Long.valueOf(i));
				}
			}
		};
	}

	public static Runner createArrayDequeDeconstruct() {
		return new AbstractRunner() {
			private Deque<Long> deque;
			private Sink<Long> sink;

			@Override
			public void prepare(int problemSize) {
				super.prepare(problemSize);
				deque = new ArrayDeque<>();
				for (int i = 0; i < problemSize; i++) {
					deque.add(Long.valueOf(i));
				}
				sink = new Sink<>();
			}

			@Override
			public void run() {
				for (int i = 0; i < problemSize; i++) {
					sink.use(deque.pop());
				}
			}
			
			@Override
			public void cleanup() {
				super.cleanup();
				deque = null;
				sink = null;
			}
		};
	}	
}
