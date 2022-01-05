package de.codecentric.fpl.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.parser.Parser;
import de.codecentric.fpl.parser.Position;
import de.codecentric.fpl.parser.Scanner;
import static de.codecentric.fpl.datatypes.AbstractFunction.evaluateToBoolean;

/**
 * Reads and evaluates one or several files.
 */
public class Interpreter {

	public static void main(String[] args) throws Exception {
		Symbol silent = new Symbol("silent");
		boolean firstExpression = true;
		FplEngine engine = new FplEngine();
		FplValue expression = null;

		for (String arg : args) {
			try (InputStream is = new FileInputStream(arg);
					Reader rd = new BomAwareReader(is);
					Parser parser = new Parser(new Scanner(arg, rd))) {
				try {
					while (parser.hasNext()) {
						expression = parser.next();
						if (expression != null) {
							expression = engine.evaluate(expression);
						}
						if (!evaluateToBoolean(engine.getScope(), silent)) {
							if (!firstExpression) {
								System.out.println();
							}
							if (expression == null) {
								System.out.println("nil");
							} else {
								System.out.println(expression.toString());
							}
						}
						firstExpression = false;
					}
				} catch (EvaluationException e) {
					Position p = FplEngine.findPosition(expression);
					e.add(new StackTraceElement(AbstractFunction.FPL, "top-level", p.getName(), p.getLine()));
					System.out.println(e.getMessage());
					System.out.println(e.stackTraceAsString());
				} catch (Throwable e) {
					System.out.println(e.getMessage());
					e.printStackTrace(System.out);
				}
			}
		}
	}
}
