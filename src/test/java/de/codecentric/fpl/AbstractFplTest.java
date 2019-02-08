package de.codecentric.fpl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Scanner;

public class AbstractFplTest {
	protected FplEngine engine;
    protected Scope scope;

    @Before
    public void setUp() throws EvaluationException, ScopeException {
    	engine = new FplEngine();
        scope = engine.getScope();
    }

    @After
    public void tearDown() {
    	engine = null;
        scope = null;
    }

    protected FplValue evaluate(String name, String input) throws ParseException, IOException, EvaluationException {
    	return evaluate(scope, name, input);
    }

    protected FplValue evaluate(Scope s, String name, String input) throws ParseException, IOException, EvaluationException {
        Parser p = parser(name, input);
        assertTrue(p.hasNext());
        FplValue e = p.next();
        assertFalse(p.hasNext());
        return e.evaluate(s);
    }

	protected Parser parser(String name, String input) throws ParseException, IOException {
		return new Parser(new Scanner(name, new StringReader(input)));
	}
}
