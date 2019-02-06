package de.codecentric.fpl;

import java.io.Reader;

import de.codecentric.fpl.io.BomAwareReader;

public class AbstractClasspathResourceTest {
	public static class TestResults {
		private final FplEngine engine;
		private final ListResultCallback callback;

		private TestResults(FplEngine engine, ListResultCallback callback) {
			this.engine = engine;
			this.callback = callback;
		}

		public FplEngine getEngine() {
			return engine;
		}

		public ListResultCallback getCallback() {
			return callback;
		}
	}

	private Class<?> clazz;

	/**
	 * @param clazz Class as base for package relative resource loading.
	 */
	public AbstractClasspathResourceTest(Class<?> clazz) {
		this.clazz = clazz;
	}

	protected TestResults evaluate(String resource) throws Exception {
		ListResultCallback callback = new ListResultCallback();

		FplEngine engine = new FplEngine();
		try (Reader rd = new BomAwareReader(clazz.getResourceAsStream(resource))) {
			engine.evaluate(resource, rd, callback);
		}
		if (callback.hasException()) {
			throw callback.getException();
		}
		return new TestResults(engine, callback);
	}
}
