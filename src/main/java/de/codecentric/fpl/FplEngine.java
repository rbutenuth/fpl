package de.codecentric.fpl;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.codecentric.fpl.builtin.Arithmetric;
import de.codecentric.fpl.builtin.Assignment;
import de.codecentric.fpl.builtin.ClassAndObject;
import de.codecentric.fpl.builtin.Comparison;
import de.codecentric.fpl.builtin.Conditional;
import de.codecentric.fpl.builtin.Lambda;
import de.codecentric.fpl.builtin.ListFunctions;
import de.codecentric.fpl.builtin.Logic;
import de.codecentric.fpl.builtin.Loop;
import de.codecentric.fpl.builtin.Print;
import de.codecentric.fpl.builtin.StringFunctions;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Scanner;

/**
 * Builds the initial {@link Scope} and contains helpers for execution.
 */
public class FplEngine {
	private PrintStream systemOut;
	private Scope scope;

	public FplEngine() throws ScopeException {
		systemOut = System.out;
		scope = createDefaultScope();
	}
	
	/**
	 * @return A new {@link Scope} with no outer scope and all built in functions.
	 * @throws ScopeException
	 *             Should not happen on initialization.
	 */
	public Scope createDefaultScope() throws ScopeException {
		Scope scope = new Scope();

		new Print(this).populate(scope);
		new Assignment().populate(scope);
		new Arithmetric().populate(scope);
		new Logic().populate(scope);
		new ListFunctions().populate(scope);
		new StringFunctions().populate(scope);
		new Comparison().populate(scope);
		new Conditional().populate(scope);
		new Loop().populate(scope);
		new Lambda().populate(scope);
		new ClassAndObject().populate(scope);

		return scope;
	}

	public PrintStream getSystemOut() {
		return systemOut;
	}

	public void setSystemOut(PrintStream systemOut) {
		this.systemOut = systemOut;
	}

	public Scope getScope() {
		return scope;
	}
	
	public List<FplValue> evaluate(String sourceName, Reader rd, ResultCallback callback) throws IOException {
		List<FplValue> results = new ArrayList<>();
		boolean continueEvaluation = true;
		try (Parser parser = new Parser(new Scanner(sourceName, rd))) {
			do {
				try {
					if (parser.hasNext()) {
						FplValue expression = parser.next();
						if (expression != null) {
							expression = expression.evaluate(scope);
						}
						continueEvaluation = callback.handleSuccess(expression);
					} else {
						break;
					}
				} catch (Exception e) {
					continueEvaluation = callback.handleException(e);
				}
			} while (continueEvaluation);
		}
		return results;
	}
}
