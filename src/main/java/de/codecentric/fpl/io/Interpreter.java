package de.codecentric.fpl.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;

import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.StringResultCallback;

/**
 * Reads and evaluates one or several files.
 */
public class Interpreter {

	public static void main(String[] args) throws Exception {
		FplEngine engine = new FplEngine();
		for (String arg : args) {
			try (InputStream is = new FileInputStream(arg); Reader rd = new BomAwareReader(is)) {
				StringResultCallback callback = new StringResultCallback(true);
				try (PrintStream ps = new PrintStream(callback.getOutputStream())) {
					engine.setSystemOut(ps);
					engine.evaluate(arg, rd, callback);
					System.out.println(callback.toString());
				}
			}
		}
	}
}
