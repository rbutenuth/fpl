package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.list.FplList;

public class StringFunctionsTest extends AbstractFplTest {
	private static final String nl = System.lineSeparator();

	@Test
	public void noDescription() throws Exception {
		// cover constructor
		new StringFunctions();
		
		FplString fplMarkdown = (FplString) evaluate("describe", "(describe 42)");
		assertEquals("string", fplMarkdown.typeName());
		String markdown = fplMarkdown.getContent();
		assertEquals("There is no documentation for 42", markdown);
	}
	
	@Test
	public void describe() throws Exception {
		evaluate("function", "" + //
				"; Multiply three parameters." + nl + //
				"; A second line without sense." + nl + //
				"(def-function multiply (" + nl + "; comment for a" + nl + //
				"a" + nl + //
				"; comment for b" + nl + //
				"; with a second line" + nl + //
				"b c)" + nl + //
				"  (* a b c)" + nl + //
				")" + nl);
		FplString fplMarkdown = (FplString) evaluate("describe", "(describe multiply)");
		String markdown = fplMarkdown.getContent();
		String[] split = markdown.split(nl);
		String[] expected = new String[] {
			"Function multiply",
			"Multiply three parameters.",
			"A second line without sense.",
			"* a comment for a",
			"* b comment for b with a second line",
			"* c"
		};
		assertEquals(expected.length, split.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], split[i]);
		}
	}
	
	@Test
	public void joinNoParameters() throws Exception {
		FplString str = (FplString) evaluate("join", "(join)");
		assertEquals("", str.getContent());
	}
	
	@Test
	public void joinOneParameter() throws Exception {
		FplString str = (FplString) evaluate("join", "(join \"first\")");
		assertEquals("first", str.getContent());
	}
	
	@Test
	public void joinWithNil() throws Exception {
		FplString str = (FplString) evaluate("join", "(join \"first\" nil 42 null-symbol)");
		assertEquals("firstnil42nil", str.getContent());
	}

	@Test
	public void formatInteger() throws Exception {
		FplString str = (FplString) evaluate("integer", "(format-number \"#\" \"US\" 42)");
		assertEquals("42", str.getContent());
	}
	
	@Test
	public void formatDouble() throws Exception {
		FplString str = (FplString) evaluate("double", "(format-number \"#.#\" \"US\" 4.2)");
		assertEquals("4.2", str.getContent());
	}
	
	@Test
	public void formatNotANumber() throws Exception {
		try {
			evaluate("bad", "(format-number \"#.#\" \"US\" \"foo\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not a number: \"foo\"", e.getMessage());
		}
	}

	@Test
	public void parseInteger() throws Exception {
		FplInteger i = (FplInteger) evaluate("integer", "(parse-number \"#\" \"US\" \"42\")");
		assertEquals(42L, i.getValue());
	}
	
	@Test
	public void parseDouble() throws Exception {
		FplDouble d = (FplDouble)evaluate("double", "(parse-number \"#.#\" \"US\" \"4.2\")");
		assertEquals(4.2, d.getValue(), 0.00001);
	}
	
	@Test
	public void parseNotANumber() throws Exception {
		try {
			evaluate("bad", "(parse-number \"#.#\" \"US\" \"foo\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Unparseable number: \"foo\"", e.getMessage());
		}
	}
	
	@Test
	public void length() throws Exception {
		FplInteger i = (FplInteger) evaluate("length", "(length \"1234\")");
		assertEquals(4L, i.getValue());
	}
	
	@Test
	public void charAt() throws Exception {
		FplInteger i = (FplInteger) evaluate("char-at", "(char-at \"1234\" 1)");
		assertEquals('2', i.getValue());
	}
	
	@Test
	public void fromChars() throws Exception {
		FplString str = (FplString) evaluate("from chars", "(from-chars '(97 98 99))");
		assertEquals("abc", str.getContent());
	}

	@Test
	public void fromCharsWithException() throws Exception {
		try {
		evaluate("from chars", "(from-chars '(97 \"huhu\" 99))");
		fail("missing exception");
		} catch (EvaluationException e) {
			assertEquals("Not an integer at list pos 1: \"huhu\"", e.getMessage());
		}
	}

	@Test
	public void indexOf() throws Exception {
		FplInteger i = (FplInteger) evaluate("index-of", "(index-of \"abcd\" \"b\")");
		assertEquals(1L, i.getValue());
	}
	
	@Test
	public void lastIndexOf() throws Exception {
		FplInteger i = (FplInteger) evaluate("last-index-of", "(last-index-of \"abbd\" \"b\")");
		assertEquals(2L, i.getValue());
	}
	
	@Test
	public void substring() throws Exception {
		FplString str = (FplString) evaluate("substring", "(substring \"abcdef\" 2 4)");
		assertEquals("cd", str.getContent());
	}
	
	@Test
	public void matchFound() throws Exception {
		FplList list = (FplList) evaluate("match", "(match \"abcdefghij\" \"d(ef)g\")");
		assertEquals(3, ((FplInteger)list.get(0)).getValue());
		assertEquals("defg", ((FplString)list.get(1)).getContent());
		assertEquals("ef", ((FplString)list.get(2)).getContent());
		assertEquals(3, list.size());
	}
	
	@Test
	public void matchNotFound() throws Exception {
		FplList list = (FplList) evaluate("match", "(match \"abcdefghij\" \"xx\")");
		assertEquals(0, list.size());
	}
	
	@Test
	public void replaceAll() throws Exception {
		FplString str = (FplString) evaluate("replace-all", "(replace-all \"abcdefcd\" \"cd\" \"xy\")");
		assertEquals("abxyefxy", str.getContent());
	}
	
	@Test
	public void toLowerCase() throws Exception {
		FplString str = (FplString) evaluate("to-lower-case", "(to-lower-case \"ABCDEF\")");
		assertEquals("abcdef", str.getContent());
	}
	
	@Test
	public void toUpperCase() throws Exception {
		FplString str = (FplString) evaluate("to-upper-case", "(to-upper-case \"abcdef\")");
		assertEquals("ABCDEF", str.getContent());
	}	
}
