package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

public class InputOutputTest extends AbstractFplTest {

	@Test
	public void loadOnlyOne() throws Exception {
		File file = writeToTempFile("(* 6 7)");
		try {
			FplList list = (FplList) evaluate("load", "(parse-resource \"" + file.toURI() + "\" 0)");
			assertEquals(1, list.size());
			FplList expression = (FplList) list.get(0);
			assertEquals(3, expression.size());
			assertEquals(new Symbol("*"), expression.get(0));
			assertEquals(6L, ((FplInteger) expression.get(1)).getValue());
			assertEquals(7L, ((FplInteger) expression.get(2)).getValue());
		} finally {
			file.delete();
		}
	}

	@Test
	public void loadAndEvaluateOnlyOne() throws Exception {
		File file = writeToTempFile("(* 6 7)");
		try {
			FplList list = (FplList) evaluate("evaluate", "(parse-resource \"" + file.toURI() + "\" 1)");
			assertEquals(1, list.size());
			FplInteger value = (FplInteger) list.get(0);
			assertEquals(42L, ((FplInteger) value).getValue());
		} finally {
			file.delete();
		}
	}

	@Test
	public void syntaxError() throws Exception {
		// missing closing parenthesis
		File file = writeToTempFile("(* 6 7");
		try {
			evaluate("evaluate", "(parse-resource \"" + file.toURI() + "\" 1)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("Unexpected end of source in list", e.getMessage());
			StackTraceElement top = e.getStackTrace()[0];
			assertEquals(file.toURI().toString(), top.getFileName());
			assertEquals(1, top.getLineNumber());
		} finally {
			file.delete();
		}
	}

	@Test
	public void badURI() throws Exception {
		try {
			evaluate("evaluate", "(parse-resource \"htsonstwas://foo.fpl\" 1)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("unknown protocol: htsonstwas", e.getMessage());
			StackTraceElement top = e.getStackTrace()[0];
			assertEquals("htsonstwas://foo.fpl", top.getFileName());
			assertEquals(0, top.getLineNumber());
		}
	}

	@Test
	public void writeToFile() throws Exception {
		File file = File.createTempFile("test", ".txt");
		try {
			FplString content = (FplString) evaluate("write-string-to-file",
					"(write-string-to-file \"" + file.getAbsolutePath().replace('\\', '/') + "\" \"Hello world!\")");
			assertEquals("Hello world!", content.getContent());
			try (InputStream is = new FileInputStream(file);
					InputStreamReader rd = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				StringBuilder sb = new StringBuilder();
				int ch = rd.read();
				while (ch != -1) {
					sb.append((char) ch);
					ch = rd.read();
				}
				assertEquals("Hello world!", sb.toString());
			}
		} finally {
			file.delete();
		}
	}

	@Test
	public void writeToFileWithException() throws Exception {
		File file = File.createTempFile("test", ".txt");
		file.setWritable(false);
		try {
			evaluate("write-string-to-file",
					"(write-string-to-file \"" + file.getAbsolutePath().replace('\\', '/') + "\" \"Hello world!\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertTrue(e.getCause() instanceof IOException);
		} finally {
			file.setWritable(true);
			file.delete();
		}
	}
}