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
    public void testSet() throws Exception {
    	Scope local = new Scope(scope);
        assertNull(scope.get("local"));
        assertNull(local.get("local"));
        assertEquals(20, ((FplInteger)evaluate(local, "set", "(set local 20)")).getValue());
        // Value should be visible in local scope, but not in global scope
        assertEquals(20, ((FplInteger)local.get("local")).getValue());
        assertNull(scope.get("local"));

        // Clear value
        assertNull(evaluate(local, "set", "(set local nil)"));
        assertNull(local.get("local"));
    }

    @Test
    public void testSetGlobal() throws Exception {
    	Scope local = new Scope(scope);
        assertNull(scope.get("global"));
        assertNull(local.get("global"));
        assertEquals(20, ((FplInteger)evaluate(local, "set-global", "(set-global global 20)")).getValue());

        // Value should be visible in local scope (via recursive get), and in global scope
        assertEquals(20, ((FplInteger)local.get("global")).getValue());
        assertEquals(20, ((FplInteger)scope.get("global")).getValue());

        // Changing it in local should ony affect the local scope
        assertEquals(30, ((FplInteger)evaluate(local, "set", "(set global 30)")).getValue());
        assertEquals(30, ((FplInteger)local.get("global")).getValue());
        assertEquals(20, ((FplInteger)scope.get("global")).getValue());

        assertEquals(30, ((FplInteger)evaluate(local, "get", "global")).getValue());
        assertEquals(20, ((FplInteger)evaluate(scope, "get", "global")).getValue());
    }
    
    @Test
    public void testSetWithQuotedTarget() throws Exception {
    	Scope local = new Scope(scope);
        assertEquals(20, ((FplInteger)evaluate(local, "set", "(set (quote local) 20)")).getValue());
        assertEquals(20, ((FplInteger)local.get("local")).getValue());

        // Clear value
        assertNull(evaluate(local, "set", "(set local nil)"));
        assertNull(local.get("local"));
    }

    @Test(expected = EvaluationException.class)
    public void testSetWithTargetNotSymbolFails() throws Exception {
    	Scope local = new Scope(scope);
        assertEquals(20, ((FplInteger)evaluate(local, "set", "(set 10 20)")).getValue());
    }

}
