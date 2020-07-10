package de.codecentric.fpl.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;

import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.ResultCallback;
import de.codecentric.fpl.StringResultCallback;

/**
 * Reads and evaluates one or several files.
 */
public class Interpreter {

	public static void main(String[] args) throws Exception {
		FplEngine engine = new FplEngine();
		for (String arg : args) {
			try (InputStream is = new FileInputStream(arg); Reader rd = new BomAwareReader(is)) {
				ResultCallback callback = new StringResultCallback(true);
				engine.evaluate(arg, rd, callback);
				System.out.println(callback.toString());
			}
		}
	}
}
