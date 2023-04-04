package de.codecentric.fpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.io.BomAwareReader;
import de.codecentric.fpl.parser.ParseException;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Scanner;

public class AbstractFplTest {
	private Class<?> clazz;
	protected FplEngine engine;
    protected Scope scope;

	public AbstractFplTest() {
		this(null);
	}
	
	/**
	 * @param clazz Class as base for package relative resource loading.
	 */
	public AbstractFplTest(Class<?> clazz) {
		this.clazz = clazz == null ? this.getClass() : clazz;
	}

    @BeforeEach
    public void setUp() throws EvaluationException, ScopeException {
    	engine = new DefaultFplEngine();
        scope = engine.getScope();
    }

    @AfterEach
    public void tearDown() {
    	engine = null;
        scope = null;
    }

    protected FplValue evaluate(String name, String input) throws ParseException, EvaluationException {
    	return evaluate(scope, name, input);
    }

    protected FplValue evaluate(Scope s, String name, String input) throws ParseException, EvaluationException {
        Parser p = parser(name, input);
        assertTrue(p.hasNext());
        FplValue e = p.next();
        assertFalse(p.hasNext());
        return e.evaluate(s);
    }

	protected Parser parser(String name, String input) throws ParseException {
		return new Parser(new Scanner(name, new StringReader(input)));
	}
	
	protected ListResultCallback evaluateResource(String resource) throws Exception {
		ListResultCallback callback = new ListResultCallback();

		try (Reader rd = new BomAwareReader(clazz.getResourceAsStream(resource))) {
			engine.evaluate(resource, rd, callback);
		}
		if (callback.hasException()) {
			throw callback.getException();
		}
		return callback;
	}
	
	protected File writeToTempFile(String s) throws IOException {
		File f = File.createTempFile("code", ".fpl");
		try (FileOutputStream fos = new FileOutputStream(f);
				OutputStreamWriter os = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			os.write(s);
			return f;
		}
	}
}
