package de.codecentric.fpl.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplSortedDictionary;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.AbstractListTest;
import de.codecentric.fpl.datatypes.list.FplList;

public class LoopTest extends AbstractFplTest {

	@Test
	public void coverConstructor() {
		new Loop();
	}

	@Test
	public void forEachOfList() throws Exception {
		evaluate("result", "(def result '())");
		evaluate("for-each", "(def-function fun (x) (set result (add-end result x)))");
		evaluate("for-each", "(for-each fun '(1 2 nil 4))");
		FplList result = (FplList) scope.get("result");
		assertEquals(4, result.size());
		assertEquals(1, ((FplInteger) result.get(0)).getValue());
		assertEquals(2, ((FplInteger) result.get(1)).getValue());
		assertNull(result.get(2));
		assertEquals(4, ((FplInteger) result.get(3)).getValue());
	}

	@Test
	public void forEachThrowsException() throws Exception {
		evaluate("fail", "(def-function fail (x) (/ 1 0))");
		try {
			evaluate("for-each", "(for-each fail '(1 2 3 4))");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("java.lang.ArithmeticException: / by zero", expected.getMessage());
		}
	}

	@Test
	public void fromTo() throws Exception {
		evaluate("values", "(def values '())");
		FplList result = (FplList) evaluate("from-to", "(from-to (lambda (x) (set values (add-end values x))) 0 10)");
		FplList values = (FplList) scope.get("values");
		AbstractListTest.check(result, 0, 9);
		AbstractListTest.check(values, 0, 10);
	}

	@Test
	public void fromToDown() throws Exception {
		evaluate("values", "(def values '())");
		FplList result = (FplList) evaluate("from-to", "(from-to (lambda (x) (set values (add-end values x))) 10 0)");
		FplList values = (FplList) scope.get("values");
		assertEquals(9, result.size());
		assertEquals(10, values.size());
		for (int i = 0; i < 9; i++) {
			assertEquals(10 - i, ((FplInteger) result.get(i)).getValue());
			assertEquals(10 - i, ((FplInteger) values.get(i)).getValue());
		}
		assertEquals(1, ((FplInteger) values.get(9)).getValue());
	}

	@Test
	public void fromToInclusive() throws Exception {
		evaluate("values", "(def values '())");
		FplList result = (FplList) evaluate("from-to-inclusive",
				"(from-to-inclusive (lambda (x) (set values (add-end values x))) 0 10)");
		FplList values = (FplList) scope.get("values");
		AbstractListTest.check(result, 0, 10);
		AbstractListTest.check(values, 0, 11);
	}

	@Test
	public void fromToDownInclusive() throws Exception {
		evaluate("values", "(def values '())");
		FplList result = (FplList) evaluate("from-to-inclusive",
				"(from-to-inclusive (lambda (x) (set values (add-end values x))) 10 0)");
		FplList values = (FplList) scope.get("values");
		assertEquals(10, result.size());
		assertEquals(11, values.size());
		for (int i = 0; i <= 9; i++) {
			assertEquals(10 - i, ((FplInteger) result.get(i)).getValue());
			assertEquals(10 - i, ((FplInteger) values.get(i)).getValue());
		}
		assertEquals(0, ((FplInteger) values.get(10)).getValue());
	}

	@Test
	public void mapEmptySequence() throws Exception {
		FplList values = (FplList) evaluate("map-sequence", "(map-sequence (lambda (x) x) 0 0)");
		assertTrue(values.isEmpty());
	}

	@Test
	public void mapSequence() throws Exception {
		FplList values = (FplList) evaluate("map-sequence", "(map-sequence (lambda (x) x) 0 10)");
		AbstractListTest.check(values, 0, 10);
	}

	@Test
	public void mapSequenceException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("map-sequence", "(map-sequence (lambda (x) x) 10 0)");
		});
		assertEquals("start > end", e.getMessage());
	}

	@Test
	public void mapSequenceLambdaThrowsException() throws Exception {
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("map-sequence", "(map-sequence (lambda (x) (/ 1 0)) 0 10)");
		});
		assertEquals("java.lang.ArithmeticException: / by zero", e.getMessage());
	}

	@Test
	public void mapToDict() throws Exception {
		evaluate("pattern", "(def rule-pattern \"([A-Z]+) -> ([A-Z]+)\")");
		evaluate("def-dict",
				"(def rule-dict\r\n"
				+ "	(map-to-dict \r\n"
				+ "		(lambda (rule)\r\n"
				+ "			(get-element (match rule rule-pattern) 2)\r\n"
				+ "		) \r\n"
				+ "		(lambda (old rule)\r\n"
				+ "			(get-element (match rule rule-pattern) 3)\r\n"
				+ "		) \r\n"
				+ "		'(\"CH -> B\" \"HH -> N\" ) \r\n"
				+ "	)\r\n"
				+ ")\r\n"
				+ "");
		FplObject dict = (FplObject) scope.get("rule-dict");
		assertEquals(FplString.make("B"), dict.get("CH"));
	}
	
	@Test
	public void mapToSortedDict() throws Exception {
		evaluate("pattern", "(def rule-pattern \"([A-Z]+) -> ([A-Z]+)\")");
		evaluate("def-dict",
				"(def rule-dict\r\n"
				+ "	(map-to-sorted-dict \r\n"
				+ "		(lambda (rule)\r\n"
				+ "			(get-element (match rule rule-pattern) 2)\r\n"
				+ "		) \r\n"
				+ "		(lambda (old rule)\r\n"
				+ "			(get-element (match rule rule-pattern) 3)\r\n"
				+ "		) \r\n"
				+ "     nil\r\n"
				+ "		'(\"HH -> N\" \"CH -> B\" ) \r\n"
				+ "	)\r\n"
				+ ")\r\n"
				+ "");
		FplSortedDictionary dict = (FplSortedDictionary) scope.get("rule-dict");
		assertEquals(FplString.make("B"), dict.get("CH"));
	}
	
	@Test
	public void mapToDictCheckOldValue() throws Exception {
		evaluate("pattern", "(def rule-pattern \"([A-Z]+) -> ([A-Z]+)\")");
		evaluate("def-dict",
				"(def rule-dict\r\n"
				+ "	(map-to-dict \r\n"
				+ "		(lambda (rule)\r\n"
				+ "			(get-element (match rule rule-pattern) 2)\r\n"
				+ "		) \r\n"
				+ "		(lambda (old rule)\r\n"
				+ "			(join (if-else old old \"\") (get-element (match rule rule-pattern) 3))\r\n"
				+ "		) \r\n"
				+ "		'(\"CH -> X\" \"CH -> Y\" ) \r\n"
				+ "	)\r\n"
				+ ")\r\n"
				+ "");
		FplObject dict = (FplObject) scope.get("rule-dict");
		assertEquals(FplString.make("XY"), dict.get("CH"));
	}
	
	@Test
	public void mapToDictEmptyKey() throws Exception {
		evaluate("def-dict",
				"(def rule-dict\r\n"
				+ "	(map-to-dict \r\n"
				+ "		(lambda (rule)\r\n"
				+ "			\"\"\r\n" // empty key
				+ "		) \r\n"
				+ "		(lambda (old rule)\r\n"
				+ "			42\r\n"
				+ "		) \r\n"
				+ "		'(1) \r\n"
				+ "	)\r\n"
				+ ")\r\n"
				+ "");
		FplObject dict = (FplObject) scope.get("rule-dict");
		assertEquals(0, dict.entrieSet().size());
	}
	
	@Test
	public void mapToDictNullKey() throws Exception {
		evaluate("def-dict",
				"(def rule-dict\r\n"
				+ "	(map-to-dict \r\n"
				+ "		(lambda (rule)\r\n"
				+ "			nil\r\n" // nil, same as empty key
				+ "		) \r\n"
				+ "		(lambda (old rule)\r\n"
				+ "			42\r\n"
				+ "		) \r\n"
				+ "		'(1) \r\n"
				+ "	)\r\n"
				+ ")\r\n"
				+ "");
		FplObject dict = (FplObject) scope.get("rule-dict");
		assertEquals(0, dict.entrieSet().size());
	}
	
	@Test
	public void mapToDictKeyIsNumber() throws Exception {
		evaluate("def-dict",
				"(def rule-dict\r\n"
				+ "	(map-to-dict \r\n"
				+ "		(lambda (rule)\r\n"
				+ "			42\r\n" // key is number
				+ "		) \r\n"
				+ "		(lambda (old rule)\r\n"
				+ "			42\r\n"
				+ "		) \r\n"
				+ "		'(1) \r\n"
				+ "	)\r\n"
				+ ")\r\n"
				+ "");
		FplObject dict = (FplObject) scope.get("rule-dict");
		assertEquals(FplInteger.valueOf(42), dict.get("42"));
	}
	
	@Test
	public void filterOfList() throws Exception {
		evaluate("filter", "(def-function my-filter (x) (lt x 3))");
		FplList filtered = (FplList) evaluate("filter", "(filter my-filter '(1 2 3 4))");
		assertEquals(2, filtered.size());
		for (int i = 1; i <= 2; i++) {
			assertEquals(FplInteger.valueOf(i), filtered.get(i - 1));
		}
	}

	@Test
	public void sort() throws Exception {
		evaluate("values", "(def values '(4 5 9 8 7 8 6))");
		FplList sorted = (FplList) evaluate("sort",
				"(sort (lambda (a b) (if-else (lt a b) -1 (if-else (gt a b) 1 0))) values)");
		FplList expected = (FplList) evaluate("expected", "'(4 5 6 7 8 8 9)");
		assertEquals(7, sorted.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), sorted.get(i));
		}
	}

	@Test
	public void sortWithExceptionInComparator() throws Exception {
		evaluate("values", "(def values '(4 5 9 8 7 8 6))");
		EvaluationException e = assertThrows(EvaluationException.class, () -> {
			evaluate("sort", "(sort (lambda (a b) (/ 1 0)) values)");
		});
		assertEquals("java.lang.ArithmeticException: / by zero", e.getMessage());
	}

	@Test
	public void filterWithLayzSymbol() throws Exception {
		evaluate("filter", "(def-function ge-filter (t src)\r\n" + //
				"	(filter (lambda (x) (ge x t)) src)\r\n" + //
				")");
		FplList filtered = (FplList) evaluate("filter", "(ge-filter (+ 3 2) '( 1 2 3 4 5 6 7 8 9 10))");
		assertEquals(6, filtered.size());
		for (int i = 5; i <= 10; i++) {
			assertEquals(FplInteger.valueOf(i), filtered.get(i - 5));
		}
	}

	@Test
	public void mapOfSmallList() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");
		FplList squares = (FplList) evaluate("map", "(map square '(1 2 3 4))");
		assertEquals(4, squares.size());
		for (int i = 1; i <= 4; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

	@Test
	public void mapOfLargeList() throws Exception {
		evaluate("square", "(def-function square (x) (* x x))");
		FplList squares = (FplList) evaluate("map", "(map square '(1 2 3 4 5 6 7 8 9 10))");
		assertEquals(10, squares.size());
		for (int i = 1; i <= 10; i++) {
			assertEquals(FplInteger.valueOf(i * i), squares.get(i - 1));
		}
	}

	@Test
	public void mapNotAList() throws Exception {
		evaluate("input", "(def input 4)");
		evaluate("square", "(def-function square (x) (* x x))");
		try {
			evaluate("map-test", "(map square input)");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("Not a list: 4", expected.getMessage());
		}
	}

	@Test
	public void mapNotALambda() throws Exception {
		evaluate("input", "(def input '(1 2 3))");
		evaluate("square", "(put square 4)");
		try {
			evaluate("map-test", "(map square input)");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("Not a function: 4", expected.getMessage());
		}
	}

	@Test
	public void mapLambdaThrowsException() throws Exception {
		evaluate("fail", "(def-function square (x) (/ 1 0))");
		try {
			evaluate("map", "(map square '(1 2 3 4))");
			fail("should not be reached.");
		} catch (EvaluationException expected) {
			assertEquals("java.lang.ArithmeticException: / by zero", expected.getMessage());
		}
	}

	@Test
	public void flatMap() throws Exception {
		evaluate("some-list", "(def-function some-list (x) (list x x))");
		FplList flatList = (FplList) evaluate("map", "(flat-map some-list '(0 1 2 3))");
		assertEquals(8, flatList.size());
		for (int i = 0; i < 4; i++) {
			assertEquals(FplInteger.valueOf(i), flatList.get(2 * i));
			assertEquals(FplInteger.valueOf(i), flatList.get(2 * i + 1));
		}
	}

	@Test
	public void flatMapNotAList() throws Exception {
		evaluate("some-list", "(def-function some-list (x) x)");
		assertThrows(EvaluationException.class, () -> {
			evaluate("map", "(flat-map some-list '(0 1 2 3))");
		});
	}

	@Test
	public void whileLoop() throws Exception {
		evaluate("empty", "(def li '())");
		evaluate("count", "(def count 0)");
		evaluate("while", "(while (lt count 10) (set li (add-front count li)) (set count (+ count 1)))");
		FplValue count = scope.get("count");
		assertEquals(FplInteger.valueOf(10), count);
		FplList list = (FplList) scope.get("li");
		assertEquals(10, list.size());
		for (int i = 0; i < 9; i++) {
			assertEquals(FplInteger.valueOf(i), list.get(9 - i));
		}
	}

	@Test
	public void reduce() throws Exception {
		FplValue sum = evaluate("reduce", "(reduce (lambda (acc value) (+ acc value)) 0 '(1 2 3 4 5 6))");
		assertEquals(FplInteger.valueOf(21), sum);
	}
}
