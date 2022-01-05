package de.codecentric.fpl.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;

public class InterpreterTest extends AbstractFplTest {
	private static final String sep = System.lineSeparator();

	@Test
	public void coverConstructor() throws Exception {
		new Interpreter();
	}

	@Test
	public void noArgs() throws Exception {
		assertEquals("", execute(new String[0]));
	}

	@Test
	public void oneArg() throws Exception {
		assertEquals("42" + sep, execute("(* 6 7)"));
	}

	@Test
	public void twoExpressions() throws Exception {
		assertEquals("42" + sep + sep + "7" + sep, execute("(* 6 7) (+ 3 4)"));
	}

	@Test
	public void twoArg() throws Exception {
		assertEquals("42" + sep + sep + "(1 2 3)" + sep, execute(new String[] { "(* 6 7)", "'(1 2 3)" }));
	}

	@Test
	public void nil() throws Exception {
		assertEquals("nil" + sep, execute("nil"));
	}

	@Test
	public void nilResult() throws Exception {
		assertEquals("(lambda () nil)\r\n" + sep + "nil" + sep,
				execute("(def-function returns-nil () nil) (returns-nil)"));
	}

	@Test
	public void evaluationException() throws Exception {
		String str = execute("(throw \"foo\")");
		// str should look like this:
		// foo
		// at throw(C:\Users\butenuth\AppData\Local\Temp\code16478938231109279970.fpl:1)
		// at
		// top-level(C:\Users\butenuth\AppData\Local\Temp\code16478938231109279970.fpl:1)
		assertTrue(str.startsWith("foo"));
		assertTrue(str.contains("at throw("));
		assertTrue(str.contains("at top-level("));
	}

	@Test
	public void javaException() throws Exception {
		assertTrue(execute("(").startsWith("Unexpected end of source in list"));
	}

	private String execute(String... code) throws Exception {
		File[] files = new File[code.length];
		String[] paths = new String[code.length];
		try {
			for (int i = 0; i < code.length; i++) {
				files[i] = writeToTempFile(code[i]);
				paths[i] = files[i].getAbsolutePath();
			}
			PrintStream originalOut = System.out;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(out, true, "UTF-8");
			System.setOut(ps);
			Interpreter.main(paths);
			System.setOut(originalOut);
			return new String(out.toByteArray(), "UTF-8");
		} finally {
			for (File f : files) {
				f.delete();
			}
		}
	}
}
