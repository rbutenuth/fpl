package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
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
	public void describeBuiltinPlus() throws Exception {
		FplString fplMarkdown = (FplString) evaluate("describe", "(describe +)");
		String markdown = fplMarkdown.getContent();
		String[] split = markdown.split(nl);
		String[] expected = new String[] { //
				"Function +", //
				"Add values.", "* op1", "* op2", "* ops..." };
		assertEquals(expected.length, split.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], split[i]);
		}
	}

	@Test
	public void describeFPLFunction() throws Exception {
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
		String[] expected = new String[] { //
				"Function multiply", //
				"Multiply three parameters.",
				"A second line without sense.", //
				"* a comment for a", //
				"* b comment for b", //
				"with a second line", // 
				"* c" };
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
		FplDouble d = (FplDouble) evaluate("double", "(parse-number \"#.#\" \"US\" \"4.2\")");
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
	public void charAtNullAsIndex() throws Exception {
		FplInteger i = (FplInteger) evaluate("char-at", "(char-at \"1234\" symbol-with-null-value)");
		assertEquals('1', i.getValue());
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
		assertEquals(3, list.size());
		assertEquals(3, ((FplInteger) list.get(0)).getValue());
		assertEquals("defg", ((FplString) list.get(1)).getContent());
		assertEquals("ef", ((FplString) list.get(2)).getContent());
	}

	@Test
	public void matchNotFound() throws Exception {
		FplList list = (FplList) evaluate("match", "(match \"abcdefghij\" \"xx\")");
		assertEquals(0, list.size());
	}

	@Test
	public void matchEmptyGroup() throws Exception {
		FplList list = (FplList) evaluate("match", "(match \"/api/bmi/80/1.88\" \"^/api/bmi/([0-9]+(\\.[0-9]*)?)/([0-9]+(\\.[0-9]*)?)$\")");
		assertEquals(6, list.size());
		assertEquals(0, ((FplInteger) list.get(0)).getValue());
		assertEquals("/api/bmi/80/1.88", ((FplString) list.get(1)).getContent());
		assertEquals("80", ((FplString) list.get(2)).getContent());
		assertEquals("", ((FplString) list.get(3)).getContent());
		assertEquals("1.88", ((FplString) list.get(4)).getContent());
		assertEquals(".88", ((FplString) list.get(5)).getContent());
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

	@Test
	public void split() throws Exception {
		FplList list = (FplList) evaluate("split", "(split \"foo bar baz \" \" \" -1)");
		assertEquals(4, list.size());
		assertEquals("foo", ((FplString) list.get(0)).getContent());		
		assertEquals("bar", ((FplString) list.get(1)).getContent());		
		assertEquals("baz", ((FplString) list.get(2)).getContent());		
		assertEquals("", ((FplString) list.get(3)).getContent());		
	}

	@Test
	public void symbol() throws Exception {
		Symbol s = (Symbol) evaluate("symbol", "(symbol \"abcdef\")");
		assertEquals("abcdef", s.getName());
	}

	@Test
	public void nameOfSymbol() throws Exception {
		FplString s = (FplString) evaluate("name-of-symbol", "(name-of-symbol 'abcdef)");
		assertEquals("abcdef", s.getContent());
	}

	@Test
	public void toStringofBuiltin() throws Exception {
		assertEquals("(quote (expression) <code>)", scope.get("quote").toString());
		assertEquals("(list (element...) <code>)", scope.get("list").toString());
		//String name, String comment, boolean varArg, String... parameterNames
		assertEquals("(test-function () <code>)", new AbstractFunction("test-function", "") {
			
			@Override
			public FplValue evaluate(Scope scope) throws EvaluationException {
				return null;
			}
			
			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return null;
			}
		}.toString());
	}

	@Test
	public void serializeToJson() throws Exception {
		FplString s = (FplString) evaluate("serialize-to-json",
				"(serialize-to-json (list \"abcdef\" 42 3.14 (dict \"key\" 44 \"other\" \"foo\") nil))");
		assertEquals("[\"abcdef\",42,3.14,{\"other\":\"foo\",\"key\":44},null]", s.getContent());
	}

	@Test
	public void serializeSymbolValueToJson() throws Exception {
		FplString s = (FplString) evaluate("serialize-to-json",
				"(serialize-to-json (dict \"key\" 'symbol))");
		assertEquals("{\"key\":\"symbol\"}", s.getContent());
	}

	@Test
	public void serializeSymbolValueTrueToJson() throws Exception {
		FplString s = (FplString) evaluate("serialize-to-json",
				"(serialize-to-json (dict \"key\" 'true))");
		assertEquals("{\"key\":true}", s.getContent());
	}

	@Test
	public void serializeSymbolValueFalseToJson() throws Exception {
		FplString s = (FplString) evaluate("serialize-to-json",
				"(serialize-to-json (dict \"key\" 'false))");
		assertEquals("{\"key\":false}", s.getContent());
	}

	@Test
	public void serializeObjectWithSeveralKeysToJson() throws Exception {
		FplString s = (FplString) evaluate("serialize-to-json",
				"(serialize-to-json (dict \"key1\" 44 \"key2\" 45))");
		assertEquals("{\"key1\":44,\"key2\":45}", s.getContent());
	}

	@Test
	public void serializeToJsonOfFunctionThrowsException() throws Exception {
		try {
			evaluate("serialize-to-json", "(serialize-to-json (lambda (x) (* x x)))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Can't serialize function to json", e.getMessage());
		}
	}

	@Test
	public void serializeToJsonOfJavaWrapperThrossException() throws Exception {
		try {
			evaluate("serialize-to-json", "(serialize-to-json (java-instance \"java.util.ArrayList\" 10))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Can't serialize wrapper(java.util.ArrayList) to json", e.getMessage());
		}
	}

	@Test
	public void parseJsonListOfNumbers() throws Exception {
		FplList list = (FplList)evaluate("parse-json", "(parse-json \"[ 1, 2, 3.14]\")");
		assertEquals(3, list.size());
		assertEquals(1, ((FplInteger)list.get(0)).getValue());
		assertEquals(2, ((FplInteger)list.get(1)).getValue());
		assertEquals(3.14, ((FplDouble)list.get(2)).getValue(), 0.00001);
	}
	
	@Test
	public void parseJsonListStrings() throws Exception {
		FplList list = (FplList)evaluate("parse-json", "(parse-json \"[\\\"abc\\\", \\\"def\\\"]\")");
		assertEquals(2, list.size());
		assertEquals("abc", ((FplString)list.get(0)).getContent());
		assertEquals("def", ((FplString)list.get(1)).getContent());
	}
	
	@Test
	public void parseJsonListBooleanWithNull() throws Exception {
		FplList list = (FplList)evaluate("parse-json", "(parse-json \"[true, false, null]\")");
		assertEquals(3, list.size());
		assertEquals(1, ((FplInteger)list.get(0)).getValue());
		assertEquals(0, ((FplInteger)list.get(1)).getValue());
		assertNull(list.get(2));
	}
	
	@Test
	public void parseJsonObject() throws Exception {
		FplObject obj = (FplObject)evaluate("parse-json", "(parse-json \"{\\\"key\\\": 1, \\\"another\\\": [0]}\")");
		int count = 0;
		for (Iterator<Entry<String, FplValue>> iterator = obj.iterator(); iterator.hasNext();) {
			Entry<String, FplValue> entry = iterator.next();
			if (entry.getKey().equals("key")) {
				assertEquals(1, ((FplInteger)entry.getValue()).getValue());
			} else if (entry.getKey().equals("another")) {
				assertTrue(entry.getValue() instanceof FplList);
			} else {
				fail("unknown key: " + entry.getKey());
			}
			count++;
		}
		assertEquals(2, count);
	}
	
	@Test
	public void parseJsonObjectNilKey() throws Exception {
		FplObject obj = (FplObject)evaluate("parse-json", "(parse-json \"{\\\"nil\\\": 1}\")");
		int count = 0;
		for (Iterator<Entry<String, FplValue>> iterator = obj.iterator(); iterator.hasNext();) {
			Entry<String, FplValue> entry = iterator.next();
			if (entry.getKey().equals("nil")) {
				assertEquals(1, ((FplInteger)entry.getValue()).getValue());
			} else {
				fail("unknown key: " + entry.getKey());
			}
			count++;
		}
		assertEquals(1, count);
	}
	
	@Test
	public void parseJsonObjectInvalidKey() throws Exception {
		try {
			evaluate("parse-json", "(parse-json \"{\\\"\\\": 1}\")");
			fail("execption missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}
}
