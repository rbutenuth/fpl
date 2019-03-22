package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ListResultCallback;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;

/**
 * Tests for the functions "set", "set-global", "let" etc.
 */
public class AssignmentTest extends AbstractFplTest {

    @Test
    public void testPut() throws Exception {
    	new Assignment(); // cover the constructor...
    	Scope local = new Scope(scope);
        assertNull(scope.get("local"));
        assertNull(local.get("local"));
        assertNull(evaluate(local, "put", "(put local 20)"));
        // Value should be visible in local scope, but not in global scope
        assertEquals(FplInteger.valueOf(20), ((FplInteger)local.get("local")));
        assertNull(scope.get("local"));

        // Clear value
        assertEquals(FplInteger.valueOf(20), evaluate(local, "put", "(put local nil)"));
        assertNull(local.get("local"));
    }

    @Test
    public void testPutNullKey() throws Exception {
    	try {
    		evaluate(scope, "put", "(put nil 20)");
    		fail("exception missing");
    	} catch (EvaluationException e) {
    		assertEquals("nil or \"\" is not a valid name", e.getMessage());
    	}
    }

    @Test
    public void testPutGlobal() throws Exception {
    	Scope local = new Scope(scope);
        assertNull(scope.get("global"));
        assertNull(local.get("global"));
        assertNull(evaluate(local, "put-global", "(put-global global 20)"));

        // Value should be visible in local scope (via recursive get), and in global scope
        assertEquals(FplInteger.valueOf(20), local.get("global"));
        assertEquals(FplInteger.valueOf(20), scope.get("global"));

        // Changing it in local should only affect the local scope
        assertEquals(FplInteger.valueOf(20), evaluate(local, "set", "(set global 30)"));
        assertEquals(FplInteger.valueOf(30), local.get("global"));
        assertEquals(FplInteger.valueOf(30), scope.get("global"));

        assertEquals(FplInteger.valueOf(30), evaluate(local, "get", "global"));
        assertEquals(FplInteger.valueOf(30), evaluate(scope, "get", "global"));
    }
    
    @Test
    public void testPutGlobalNullKey() throws Exception {
    	try {
    		evaluate(scope, "put-global", "(put-global nil 20)");
    		fail("exception missing");
    	} catch (EvaluationException e) {
    		assertEquals("nil or \"\" is not a valid name", e.getMessage());
    	}
    }

    @Test
    public void testPutWithQuotedTarget() throws Exception {
    	Scope local = new Scope(scope);
        assertNull(evaluate(local, "put", "(put (quote local) 20)"));
        assertEquals(FplInteger.valueOf(20), ((FplInteger)local.get("local")));

        // Clear value
        assertEquals(FplInteger.valueOf(20), evaluate(local, "put", "(put local nil)"));
        assertNull(local.get("local"));
    }

    @Test(expected = EvaluationException.class)
    public void testPutWithTargetNotSymbolFails() throws Exception {
    	Scope local = new Scope(scope);
        assertEquals(20, ((FplInteger)evaluate(local, "put", "(put 10 20)")).getValue());
    }

    @Test
    public void testSet() throws Exception {
        assertNull(evaluate("put", "(put key 10)"));
        assertEquals(FplInteger.valueOf(10), evaluate("set", "(set key 20)"));
        assertEquals(FplInteger.valueOf(20), scope.get("key"));
    }
    
    @Test
    public void testSetOnUndefinedFails() throws Exception {
    	try {
    		evaluate("set", "(set foo 20)");
    		fail("exception missing");
    	} catch (EvaluationException e) {
    		assertEquals("No value with key foo found", e.getMessage());
    	}
    }

    @Test
    public void testDef() throws Exception {
        assertEquals(10, ((FplInteger)evaluate("def", "(def key 10)")).getValue());
        assertEquals(10, ((FplInteger)scope.get("key")).getValue());
    }
    
	@Test
	public void testDefField() throws Exception {
		ListResultCallback callback = evaluateResource("def-field.fpl");
		List<FplValue> values = callback.getResults();
		FplObject object = (FplObject) values.get(0);
		assertEquals(new FplString("bar"), object.get("foo"));
		assertEquals(new FplString("value"), object.get("key"));
	}
	
	@Test
	public void testDefGlobal() throws Exception {
		evaluateResource("def-global.fpl");
		assertEquals(new FplString("bar"), scope.get("foo"));
	}
	
	@Test(expected = EvaluationException.class)
	public void testDefGlobalFail() throws Exception {
		scope.put("foo", FplInteger.valueOf(1));
		evaluate("def-global-fail", "(def-global foo \"baz\")\n");
	}
	
    @Test
    public void testDefFieldNoObject() throws Exception {
    	try {
    		evaluate("def-field", "(def-field key 10)");
    		fail("missing exception");
    	} catch (EvaluationException e) {
    		assertEquals("No object found", e.getMessage());
    	}
    }
    
    @Test
    public void testDefFieldNil() throws Exception {
    	try {
    		evaluate("def-field", "(def object {\n" + 
    				"	(def-field foo nil)\n" + 
    				"})");
    		fail("missing exception");
    	} catch (EvaluationException e) {
    		assertEquals("value is nil", e.getMessage());
    	}
    }
    
    @Test
    public void testDefOnDefinedFails() throws Exception {
    	try {
    		evaluate("def-1", "(def foo 20)");
    		evaluate("def-2", "(def foo 30)");
    		fail("exception missing");
    	} catch (EvaluationException e) {
    		assertEquals("Duplicate key: foo", e.getMessage());
    	}
    }
    
    @Test
    public void testInstanceNilKey() throws Exception {
    	try {
    		evaluate("instance", "(instance nil 42)");
    		fail("missing exception");
    	} catch (EvaluationException e) {
    		assertEquals("nil or \"\" is not a valid name", e.getMessage());
    	}
    }   
}
