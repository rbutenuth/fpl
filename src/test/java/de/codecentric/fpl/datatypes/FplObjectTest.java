package de.codecentric.fpl.datatypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.builtin.ClassAndObject;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.parser.Position;

public class FplObjectTest extends AbstractFplTest {
	private static String NL = System.lineSeparator();

	public FplObjectTest() {
		super(FplObjectTest.class);
	}

	@Test
	public void positionEmptyList() throws Exception {
		Position p = FplValue.position(evaluate("empty-list", "'()"));
		assertEquals(Position.UNKNOWN, p);
	}

	@Test
	public void positionEmptyDictionary() throws Exception {
		Position p = FplValue.position(evaluate("empty-dict", "(dict)"));
		assertEquals(Position.UNKNOWN, p);
	}

	@Test
	public void positionListWithSymbol() throws Exception {
		Position p = FplValue.position(evaluate("list", "'( foo )"));
		assertEquals(new Position("list", 1, 4), p);
	}

	@Test
	public void positionListWithEmptyList() throws Exception {
		Position p = FplValue.position(evaluate("list", "'( () )"));
		assertEquals(Position.UNKNOWN, p);
	}

	@Test
	public void instanciateClassAndObject() {
		new ClassAndObject(); // cover constuctor
	}

	@Test
	public void thisOutsideObjectIsNull() throws Exception {
		assertNull(evaluate("nil", "(this)"));
	}

	@Test
	public void thisInObjectReturnsObject() throws Exception {
		FplObject clazz = (FplObject) evaluate("class", "(class (def-field a0 (this)))");
		FplValue a = clazz.get("a0");
		assertEquals(clazz, a);
		assertEquals("object", clazz.typeName());
	}

	@Test
	public void createEmptyObject() throws Exception {
		FplDictionary dict = (FplDictionary) evaluate("empty", "(dict)");
		assertEquals("{" + NL + "}" + NL, dict.toString());
		assertEquals("dictionary", dict.typeName());
	}

	@Test
	public void createClassWithTwoMembers() throws Exception {
		FplObject object = (FplObject) evaluate("class", "(class (def-field a0 1) (def-field a1 2))");
		checkDictionary(object, 2);
		assertEquals(engine.getScope(), object.getNext());
		checkDoesNotContainAx(engine.getScope(), 2);
	}

	@Test
	public void createClassFromFunction() throws Exception {
		evaluate("def-fun", "(def-function make-class () (class (def-field a0 1) (def-field a1 2)))");
		FplObject object = (FplObject) evaluate("call", "(make-class)");
		checkDictionary(object, 2);
		assertEquals(engine.getScope(), object.getNext());
		checkDoesNotContainAx(engine.getScope(), 2);
	}

	@Test
	public void defClassWithTwoMembers() throws Exception {
		FplObject object = (FplObject) evaluate("def-class", "(def-class my-class (def-field a0 1) (def-field a1 2))");
		assertEquals(object, engine.getScope().get("my-class"));
		checkDictionary(object, 2);
		assertEquals(engine.getScope(), object.getNext());
		checkDoesNotContainAx(engine.getScope(), 2);
	}

	@Test
	public void defClassFailsWhenSymbolExistsInScope() throws Exception {
		evaluate("def-class", "(def-class my-class (def-field a0 1))");
		try {
			evaluate("def-class", "(def-class my-class (def-field a0 1))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Duplicate key: my-class", e.getMessage());
		}
	}

	@Test
	public void defClassAndSubClass() throws Exception {
		FplObject myClass = (FplObject) evaluate("def-class", "(def-class my-class (def-field a0 1) (def-field a1 2))");
		FplObject subClass = (FplObject) evaluate("sub-class",
				"(sub-class my-class (def-field a2 3) (def-field a3 4))");
		assertEquals(myClass, subClass.getNext());
		checkDictionary(myClass, 2);
		checkDictionary(subClass, 2);
	}

	@Test
	public void subClassFailsWhenParentIsNotObject() throws Exception {
		evaluate("def-class", "(def my-class 42)");
		try {
			evaluate("sub-class", "(sub-class my-class (def-field a2 3) (def-field a3 4))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not an object: 42", e.getMessage());
		}
	}

	@Test
	public void defClassAndDefSubClass() throws Exception {
		FplObject myClass = (FplObject) evaluate("def-class", "(def-class my-class (def-field a0 1) (def-field a1 2))");
		FplObject subClass = (FplObject) evaluate("def-sub-class",
				"(def-sub-class my-sub-class my-class (def-field a2 3) (def-field a3 4))");
		assertEquals(myClass, subClass.getNext());
		assertEquals(subClass, engine.getScope().get("my-sub-class"));
		checkDictionary(myClass, 2);
		checkDictionary(subClass, 2);
	}

	@Test
	public void defSubClassFailsWhenSymbolExistsInScope() throws Exception {
		evaluate("def-class", "(def-class my-class (def-field a0 1) (def-field a1 2))");
		evaluate("def-sub-class", "(def-sub-class my-sub-class my-class (def-field a2 3) (def-field a3 4))");
		try {
			evaluate("def-sub-class", "(def-sub-class my-sub-class my-class (def-field a2 3) (def-field a3 4))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Duplicate key: my-sub-class", e.getMessage());
		}
	}

	@Test
	public void defSubClassFailsWhenParentIsNumber() throws Exception {
		evaluate("def-class", "(def my-class 42)");
		try {
			evaluate("sub-class", "(def-sub-class my-sub-class my-class (def-field a2 3) (def-field a3 4))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not an object: 42", e.getMessage());
		}
	}

	@Test
	public void defSubClassFailsWhenParentIsDictionary() throws Exception {
		evaluate("def-class", "(def my-class (dict) )");
		try {
			evaluate("sub-class", "(def-sub-class my-sub-class my-class (def-field a2 3) (def-field a3 4))");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals(String.format("Not an object: {%n}%n"), e.getMessage());
		}
	}

	private void checkDictionary(FplObject object, int count) {
		int counted = 0;
		for (Entry<String, FplValue> pair : object) {
			counted++;
			String key = pair.getKey();
			String letter = key.substring(0, 1);
			assertEquals("a", letter);
			int number = Integer.valueOf(key.substring(1));
			assertEquals(number + 1, ((FplInteger) pair.getValue()).getValue());
		}
		assertEquals(count, counted);
	}

	private void checkDoesNotContainAx(Scope scope, int count) {
		for (int i = 0; i < count; i++) {
			assertNull(scope.get("a" + i));
		}
	}

	@Test
	public void createNewInstance() throws Exception {
		ListResultCallback callback = evaluateResource("create-new-instance.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		FplObject instance = (FplObject) values.get(1);
		assertNull(object.get("a"));
		assertNull(object.get("b"));
		assertEquals(FplInteger.valueOf(6), instance.get("a"));
		assertEquals(FplInteger.valueOf(7), instance.get("b"));
	}

	@Test
	public void callWithParameterScope() throws Exception {
		evaluateResource("create-new-instance.fpl");
		FplObject instance = (FplObject) evaluate("call-method", "(make)");
		assertEquals(FplInteger.valueOf(8), instance.get("a"));
		assertEquals(FplInteger.valueOf(9), instance.get("b"));
	}

	@Test
	public void callingMemberFails() throws Exception {
		evaluateResource("create-new-instance.fpl");
		try {
			evaluate("not-a-function", "(my-class key)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Not a function: 42", e.getMessage());
		}
	}

	@Test
	public void newInstanceWithOddNumberOfParametersFails() throws Exception {
		evaluateResource("create-new-instance.fpl");
		try {
			evaluate("odd", "(my-class bad-new 1 2)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("Number of parameters must be even.", e.getMessage());
		}
	}

	@Test
	public void newInstanceWithEmptyKeyString() throws Exception {
		evaluateResource("create-new-instance.fpl");
		try {
			evaluate("odd", "(my-class bad-new-empty-key 1 2)");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("\"\" is not a valid name", e.getMessage());
		}
	}

	@Test
	public void nullMethodThrowsException() throws Exception {
		assertThrows(EvaluationException.class, () -> {
			assertNull(evaluate("not-a-function", "((instance method 1) foo)"));
		});
	}
}
