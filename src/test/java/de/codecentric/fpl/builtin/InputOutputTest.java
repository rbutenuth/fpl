package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

public class InputOutputTest extends AbstractFplTest {

	@Test
	public void loadOnlyOneFromFile() throws Exception {
		File file = writeToTempFile("(* 6 7)");
		try {
			FplList list = (FplList) evaluate("parse-resource", "(parse-resource \"" + file.toURI() + "\" 0)");
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
	public void loadAndEvaluateOnlyOneFromFile() throws Exception {
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
	public void syntaxErrorFromFile() throws Exception {
		// missing closing parenthesis
		File file = writeToTempFile("(* 6 7");
		try {
			evaluate("evaluate", "(parse-resource \"" + file.toURI() + "\" 1)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("Unexpected end of source in list", e.getMessage());
			StackTraceElement top = e.getStackTrace()[0];
			assertEquals(file.toURI().toString(), top.getFileName());
			assertEquals(0, top.getLineNumber());
		} finally {
			file.delete();
		}
	}

	@Test
	public void syntaxErrorInSecondExpression() throws Exception {
		File file = writeToTempFile("(* 6 7)\n[");
		try {
			evaluate("evaluate", "(parse-resource \"" + file.toURI() + "\" 1)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("Illegal character for symbol: [", e.getMessage());
			StackTraceElement top = e.getStackTrace()[0];
			assertEquals(file.toURI().toString(), top.getFileName());
			assertEquals(0, top.getLineNumber());
		} finally {
			file.delete();
		}
	}

	@Test
	public void loadOnlyOne() throws Exception {
		FplList list = (FplList) evaluate("parse-string", "(parse-string \"(* 6 7)\" 0)");
		assertEquals(1, list.size());
		FplList expression = (FplList) list.get(0);
		assertEquals(3, expression.size());
		assertEquals(new Symbol("*"), expression.get(0));
		assertEquals(6L, ((FplInteger) expression.get(1)).getValue());
		assertEquals(7L, ((FplInteger) expression.get(2)).getValue());
	}

	@Test
	public void loadAndEvaluateOnlyOne() throws Exception {
		FplList list = (FplList) evaluate("evaluate", "(parse-string \"(* 6 7)\" 1)");
		assertEquals(1, list.size());
		FplInteger value = (FplInteger) list.get(0);
		assertEquals(42L, ((FplInteger) value).getValue());
	}

	@Test
	public void syntaxError() throws Exception {
		try {
			// missing closing parenthesis
			evaluate("evaluate", "(parse-string \"(* 6 7\" 1)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("Unexpected end of source in list", e.getMessage());
			StackTraceElement top = e.getStackTrace()[0];
			assertEquals("(* 6 7", top.getFileName());
			assertEquals(1, top.getLineNumber());
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
	public void readFromFileByUrl() throws Exception {
		final int length = 10_000;
		File file = File.createTempFile("test", ".txt");
		StringBuilder content = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			content.append((char) ('a' + i));
		}
		try (Writer w = new FileWriter(file, StandardCharsets.UTF_8)) {
			w.write(content.toString());
		}
		try {
			FplString str = (FplString) evaluate("read-string-from-resource",
					"(read-string-from-resource \"" + file.toURI().toString().replace('\\', '/') + "\")");
			String readContent = str.getContent();
			for (int i = 0; i < length; i++) {
				assertEquals(content.charAt(i), readContent.charAt(i));
			}
		} finally {
			file.delete();
		}
	}

	@Test
	public void readFromFile() throws Exception {
		final int length = 10_000;
		File file = File.createTempFile("test", ".txt");
		StringBuilder content = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			content.append((char) ('a' + i));
		}
		try (Writer w = new FileWriter(file, StandardCharsets.UTF_8)) {
			w.write(content.toString());
		}
		try {
			FplString str = (FplString) evaluate("read-string-from-file",
					"(read-string-from-file \"" + file.getAbsolutePath().replace('\\', '/') + "\")");
			String readContent = str.getContent();
			for (int i = 0; i < length; i++) {
				assertEquals(content.charAt(i), readContent.charAt(i));
			}
		} finally {
			file.delete();
		}
	}

	@Test
	public void readFromFileWithBadURI() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("evaluate", "(read-string-from-resource \"htsonstwas://foo.fpl\")");
		});
	}

	@Test
	public void readFromFileWithBadName() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			evaluate("evaluate", "(read-string-from-file \"foo.fpl\")");
		});
	}

	@Test
	public void toStringOfNil() throws Exception {
		FplString str = (FplString) evaluate("nil", "(to-string nil)");
		assertEquals("nil", str.getContent());
	}

	@Test
	public void toStringOfValueNil() throws Exception {
		FplString str = (FplString) evaluate("nil", "(to-string symbol-with-value-nil)");
		assertEquals("nil", str.getContent());
	}

	@Test
	public void toStringOfSymbol() throws Exception {
		FplString str = (FplString) evaluate("nil", "(to-string 'some-symbol)");
		assertEquals("some-symbol", str.getContent());
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