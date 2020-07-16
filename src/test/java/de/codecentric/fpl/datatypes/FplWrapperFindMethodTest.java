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
		
		public String testMethod(float arg) {
			return "float arg";
		}
		
		public String testMethod(double arg) {
			return "double arg";
		}
		
		public String testMethod(boolean arg) {
			return "boolean arg";
		}
		
		public String testMethod(Integer i, String arg) {
			return "Integer i, String arg";
		}
		
		public String testMethod(Double d, String arg) {
			return "Double d, String arg";
		}
		
		public String booleanMethod(boolean arg) {
			return "booleanMethod(boolean arg)->" + arg;
		}
		
		public String fractionalMethod(Double d) {
			return "Double d";
		}
		
		public String fractionalMethod(Float t) {
			return "Float t";
		}
	}
	
	@Test
	public void constructorNotFound() throws Exception {
		try {
			evaluate("noarg-cons", "(java-instance \"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" \"foo\")");
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("No matching method with name de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass found", e.getMessage());
		}
	}
	
	@Test
	public void noArgs() throws Exception {
		FplString args = (FplString) evaluate("noarg-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\") getArgs)");
		assertEquals("", args.getContent());
	}
	
	@Test
	public void listByNull() throws Exception {
		FplString args = (FplString) evaluate("list-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" nil) getArgs)");
		assertEquals("List<?> list", args.getContent());
	}
	
	@Test
	public void booleanFalse() throws Exception {
		FplString args = (FplString) evaluate("boolean-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" 0) getArgs)");
		assertEquals("int i", args.getContent());
	}
	
	@Test
	public void booleanNullIsFalse() throws Exception {
		FplString args = (FplString) evaluate("boolean-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" 1 nil) getArgs)");
		assertEquals("int i, Boolean bool", args.getContent());
	}
	
	@Test
	public void wrappedPrimitives() throws Exception {
		FplString args = (FplString) evaluate("wrapped-primitive-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" " 
				+"1 2 3 4 5.0 6.0 1) getArgs)");
		assertEquals("Byte b, Short s, Integer i, Long l, Float f, Double d, Boolean bool", args.getContent());
	}
	
	@Test
	public void primitives() throws Exception {
		FplString args = (FplString) evaluate("primitive-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\" " 
				+"1 2 3 4 5.0 6.0) getArgs)");
		assertEquals("byte b, short s, int i, long l, float f, double d", args.getContent());
	}
	
	@Test
	public void callTestMethodWithDoubleArg() throws Exception {
		FplString tm = (FplString) evaluate("double", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\") testMethod 1.0)");
		assertEquals("double arg", tm.getContent());
	}
	
	@Test
	public void callTestMethodWithStringArg() throws Exception {
		FplString tm = (FplString) evaluate("primitive-cons", //
				"((java-instance\"de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass\") testMethod \"foo\")");
		assertEquals("String arg", tm.getContent());
	}
	
	@Test
	public void callTestMethodWithDoubleNullArg() throws Exception {
		FplString tm = (FplString) evaluate("primitive-cons", //
				"((java-instance de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass) testMethod 1.0 nil)");
		assertEquals("Double d, String arg", tm.getContent());
	}
	
	@Test
	public void floatCall() throws Exception {
		FplWrapper w = new FplWrapper(ConstructorTestClass.class.getName(), new FplValue[0]);
		FplValue[] parameters = new FplValue[2];
		parameters[0] = new FplString("testMethod"); 
		parameters[1] = new FplWrapper(Float.valueOf(3.14f));
		FplValue result = w.callInternal(null, parameters);
		assertEquals("float arg", ((FplString)result).getContent());
	}
	
	@Test
	public void booleanCall() throws Exception {
		FplWrapper w = new FplWrapper(ConstructorTestClass.class.getName(), new FplValue[0]);
		FplValue[] parameters = new FplValue[2];
		parameters[0] = new FplString("testMethod"); 
		parameters[1] = new FplWrapper(Boolean.TRUE);
		FplValue result = w.callInternal(null, parameters);
		assertEquals("boolean arg", ((FplString)result).getContent());
	}
	
	@Test
	public void callBooleanMethodWithIntArg() throws Exception {
		FplString tm = (FplString) evaluate("primitive-cons", //
				"((java-instance de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass) booleanMethod 1)");
		assertEquals("booleanMethod(boolean arg)->true", tm.getContent());
		tm = (FplString) evaluate("primitive-cons", //
				"((java-instance de.codecentric.fpl.datatypes.FplWrapperFindMethodTest$ConstructorTestClass) booleanMethod 0)");
		assertEquals("booleanMethod(boolean arg)->false", tm.getContent());
	}
	
	@Test
	public void fractionalFloatCall() throws Exception {
		FplWrapper w = new FplWrapper(ConstructorTestClass.class.getName(), new FplValue[0]);
		FplValue[] parameters = new FplValue[2];
		parameters[0] = new FplString("fractionalMethod"); 
		parameters[1] = new FplWrapper(Float.valueOf(3.14f));
		FplValue result = w.callInternal(null, parameters);
		assertEquals("Float t", ((FplString)result).getContent());
	}

	@Test
	public void fractionalDoubleCall() throws Exception {
		FplWrapper w = new FplWrapper(ConstructorTestClass.class.getName(), new FplValue[0]);
		FplValue[] parameters = new FplValue[2];
		parameters[0] = new FplString("fractionalMethod"); 
		parameters[1] = new FplWrapper(Double.valueOf(2.78));
		FplValue result = w.callInternal(null, parameters);
		assertEquals("Double d", ((FplString)result).getContent());
	}
	
	@Test
	public void parentChild() throws Exception {
		FplWrapper w = new FplWrapper(ConstructorTestClass.class.getName(), new FplValue[0]);
		FplValue[] parameters = new FplValue[2];
		parameters[0] = new FplString("fractionalMethod"); 
		parameters[1] = new FplWrapper(Double.valueOf(2.78));
		FplValue result = w.callInternal(null, parameters);
		assertEquals("Double d", ((FplString)result).getContent());
	}
}
