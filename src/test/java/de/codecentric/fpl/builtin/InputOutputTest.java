package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

public class InputOutputTest extends AbstractFplTest {

	@Test
	public void loadOnlyOne() throws Exception {
		File file = writeToTempFile("(* 6 7)");
		try {
			FplList list = (FplList) evaluate("load", "(read-resource \"" + file.toURI() + "\" 0)");
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
			FplList list = (FplList) evaluate("evaluate", "(read-resource \"" + file.toURI() + "\" 1)");
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
			evaluate("evaluate", "(read-resource \"" + file.toURI() + "\" 1)");
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
			evaluate("evaluate", "(read-resource \"htsonstwas://foo.fpl\" 1)");
			fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("unknown protocol: htsonstwas", e.getMessage());
			StackTraceElement top = e.getStackTrace()[0];
			assertEquals("htsonstwas://foo.fpl", top.getFileName());
			assertEquals(0, top.getLineNumber());
		}
	}
}