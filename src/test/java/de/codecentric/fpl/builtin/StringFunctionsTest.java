package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplString;

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
}
