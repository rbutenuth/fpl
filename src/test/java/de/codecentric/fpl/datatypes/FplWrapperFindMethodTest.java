package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;

public class FplWrapperFindMethodTest extends AbstractFplTest {

	public FplWrapperFindMethodTest() {
		super(FplWrapperFindMethodTest.class);
	}

	// number like primitives are: Byte, Short, Integer, Long, Float, Double,
	// Boolean (1, 0)
	// non number like primitives are: Void, Character

	public static class ConstructorTestClass {
		private String args;
		
		public ConstructorTestClass(byte b, short s, int i, long l, float f, double d, boolean bool) {
			args = "byte b, short s, int i, long l, float f, double d, boolean bool";
		}
		
		public ConstructorTestClass(Byte b, Short s, Integer i, Long l, Float f, Double d, Boolean bool) {
			args = "Byte b, Short s, Integer i, Long l, Float f, Double d, Boolean bool";
		}
		
		public ConstructorTestClass(byte b, short s, int i, long l, float f, double d) {
			args = "byte b, short s, int i, long l, float f, double d";
		}
		
		public ConstructorTestClass(int i) {
			args= "int i";
		}
		
		public ConstructorTestClass(boolean bool) {
			args= "boolean bool";
		}
		
		public ConstructorTestClass(int i, Boolean bool) {
			args= "int i, Boolean bool";
		}
		
		public ConstructorTestClass(List<?> list) {
			args= "List<?> list";
		}
		
		public ConstructorTestClass() {
			args= "";
		}
		
		public String getArgs() {
			return args;
		}
		
		public String testMethod(String arg) {
			return "String arg";
		}
		
		public String testMethod(char arg) {
			return "char arg";
		}
	}
	
	@Test
	public void testConstructorNotFound() throws Exception {
		try {
			evaluate("noarg-cons", "(java-instance \"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" \"foo\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No matching method with name de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass found", e.getMessage());
		}
	}
	
	@Test
	public void testNoArgs() throws Exception {
		FplString args = (FplString) evaluate("noarg-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\") getArgs)");
		assertEquals("", args.getContent());
	}
	
	@Test
	public void testListByNull() throws Exception {
		FplString args = (FplString) evaluate("list-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" nil) getArgs)");
		assertEquals("List<?> list", args.getContent());
	}
	
	@Test
	public void testBooleanTrue() throws Exception {
		FplString args = (FplString) evaluate("boolean-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" 1) getArgs)");
		assertEquals("boolean bool", args.getContent());
	}
	
	@Test
	public void testBooleanFalse() throws Exception {
		FplString args = (FplString) evaluate("boolean-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" 0) getArgs)");
		assertEquals("boolean bool", args.getContent());
	}
	
	@Test
	public void testBooleanNullIsFalse() throws Exception {
		FplString args = (FplString) evaluate("boolean-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" 1 nil) getArgs)");
		assertEquals("int i, Boolean bool", args.getContent());
	}
	
	@Test
	public void testWrappedPrimitives() throws Exception {
		FplString args = (FplString) evaluate("wrapped-primitive-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" " 
				+"1 2 3 4 5.0 6.0 1) getArgs)");
		assertEquals("Byte b, Short s, Integer i, Long l, Float f, Double d, Boolean bool", args.getContent());
	}
	
	@Test
	public void testPrimitives() throws Exception {
		FplString args = (FplString) evaluate("primitive-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" " 
				+"1 2 3 4 5.0 6.0) getArgs)");
		assertEquals("byte b, short s, int i, long l, float f, double d", args.getContent());
	}
	
	@Test
	public void testCallTestMethodWithStringArg() throws Exception {
		FplString tm = (FplString) evaluate("primitive-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\") testMethod \"foo\")");
		assertEquals("String arg", tm.getContent());
	}
	
	private static class MethodTestClass {
		
	}
}
