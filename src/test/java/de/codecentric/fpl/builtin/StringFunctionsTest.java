package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplString;

public class StringFunctionsTest extends AbstractFplTest {
	private static final String nl = System.lineSeparator();

	@Test
	public void testNoDescription() throws Exception {
		// cover constructor
		new StringFunctions();
		
		FplString fplMarkdown = (FplString) evaluate("describe", "(describe 42)");
		assertEquals("string", fplMarkdown.typeName());
		String markdown = fplMarkdown.getContent();
		assertEquals("There is no documentation for 42", markdown);
	}
	
	@Test
	public void testDescribe() throws Exception {
		evaluate("function", "" + //
				"; Multiply three parameters." + nl + //
				"; A second line without sense." + nl + //
				"(defun multiply (" + nl + "; comment for a" + nl + //
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
}
