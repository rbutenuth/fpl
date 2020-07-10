package de.codecentric.fpl.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class InterpreterTest {
	private static final String sep = System.lineSeparator();

	@Test
	public void noArgs() throws Exception {
		assertEquals("", execute(new String[0]));
	}

	@Test
	public void oneArg() throws Exception {
		assertEquals("42" + sep + sep, execute(new String[] { "(* 6 7)" }));
	}

	@Test
	public void twoArg() throws Exception {
		assertEquals("42" + sep + sep + "(1 2 3)" + sep + sep, execute(new String[] { "(* 6 7)", "'(1 2 3)" }));
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

	private File writeToTempFile(String s) throws IOException {
		File f = File.createTempFile("code", ".fpl");
		try (FileOutputStream fos = new FileOutputStream(f);
				OutputStreamWriter os = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			os.write(s);
			return f;
		}
	}
}
