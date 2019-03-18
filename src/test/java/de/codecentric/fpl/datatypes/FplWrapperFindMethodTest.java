package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;

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
		
		public ConstructorTestClass(boolean bool) {
			args= "boolean bool";
		}
		
		public ConstructorTestClass(int i, Boolean bool) {
			args= "int i, Boolean bool";
		}
		
		public ConstructorTestClass(List<?> list) {
			args= "List<?> list";
		}
		
		public String getArgs() {
			return args;
		}
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
}
