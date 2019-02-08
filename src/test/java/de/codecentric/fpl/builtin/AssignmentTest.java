package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplInteger;

/**
 * Tests for the functions "set", "set-global", "let" etc.
 */
public class AssignmentTest extends AbstractFplTest {

    @Test
    public void testPut() throws Exception {
    	Scope local = new Scope(scope);
        assertNull(scope.get("local"));
        assertNull(local.get("local"));
        assertEquals(20, ((FplInteger)evaluate(local, "put", "(put local 20)")).getValue());
        // Value should be visible in local scope, but not in global scope
        assertEquals(20, ((FplInteger)local.get("local")).getValue());
        assertNull(scope.get("local"));

        // Clear value
        assertNull(evaluate(local, "put", "(put local nil)"));
        assertNull(local.get("local"));
    }

    @Test
    public void testPutGlobal() throws Exception {
    	Scope local = new Scope(scope);
        assertNull(scope.get("global"));
        assertNull(local.get("global"));
        assertEquals(20, ((FplInteger)evaluate(local, "put-global", "(put-global global 20)")).getValue());

        // Value should be visible in local scope (via recursive get), and in global scope
        assertEquals(20, ((FplInteger)local.get("global")).getValue());
        assertEquals(20, ((FplInteger)scope.get("global")).getValue());

        // Changing it in local should only affect the local scope
        assertEquals(30, ((FplInteger)evaluate(local, "set", "(put global 30)")).getValue());
        assertEquals(30, ((FplInteger)local.get("global")).getValue());
        assertEquals(20, ((FplInteger)scope.get("global")).getValue());

        assertEquals(30, ((FplInteger)evaluate(local, "get", "global")).getValue());
        assertEquals(20, ((FplInteger)evaluate(scope, "get", "global")).getValue());
    }
    
    @Test
    public void testPutWithQuotedTarget() throws Exception {
    	Scope local = new Scope(scope);
        assertEquals(20, ((FplInteger)evaluate(local, "put", "(put (quote local) 20)")).getValue());
        assertEquals(20, ((FplInteger)local.get("local")).getValue());

        // Clear value
        assertNull(evaluate(local, "put", "(put local nil)"));
        assertNull(local.get("local"));
    }

    @Test(expected = EvaluationException.class)
    public void testPutWithTargetNotSymbolFails() throws Exception {
    	Scope local = new Scope(scope);
        assertEquals(20, ((FplInteger)evaluate(local, "put", "(put 10 20)")).getValue());
    }

}
