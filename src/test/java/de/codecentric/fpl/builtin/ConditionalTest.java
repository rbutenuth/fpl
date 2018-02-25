package de.codecentric.fpl.builtin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.codecentric.fpl.AbstractFplTest;
import de.codecentric.fpl.datatypes.FplInteger;

public class ConditionalTest extends AbstractFplTest {

    @Test
    public void testIfElse() throws Exception {
        assertEquals(2, ((FplInteger)evaluate("if1", "(if 1 2 3)")).getValue());
        assertEquals(3, ((FplInteger)evaluate("if1", "(if 0 2 3)")).getValue());
        assertEquals(3, ((FplInteger)evaluate("if1", "(if nil 2 3)")).getValue());
    }
}
