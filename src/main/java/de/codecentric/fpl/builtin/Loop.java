package de.codecentric.fpl.builtin;

import static de.codecentric.fpl.datatypes.AbstractFunction.comment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Loop functions.
 */
public class Loop {

	/**
	 * @param scope Scope to which functions should be added.
	 * @throws ScopeException Should not happen on initialization.
	 */
	public static void put(Scope scope) throws ScopeException {

		// (while condition expression...)
//        scope.putFunction(new Function("while", "condition", "code...") {
//            @Override
//            public FplObject call(Scope scope, FplObject[] parameters) throws EvaluationException {
//                FplObject result = null;
//                while (evaluateToBoolean(scope, parameters[0])) {
//                    for (int i = 1; i < parameters.length; i++) {
//                        result = parameters[i].evaluate(scope);
//                    }
//                }
//                return result;
//            }
//        });

		// (for-each i (list 1 2 3) (print i))
//        scope.put("for-each", new Function("for-each", "element-name", "list", "code...") {
//            @Override
//            public FplObject call(Scope scope, FplObject[] parameters) throws EvaluationException {
//                String name = evaluateToSymbol(scope, parameters[0]).getName();
//                FplList list = evaluateToList(scope, parameters[1]);
//                Scope loopScope = new Scope(scope);
//                FplObject result = null;
//                for (FplObject value : list) {
//                    loopScope.put(name, value);
//                    for (int i = 2; i < parameters.length; i++) {
//                        result = parameters[i].evaluate(loopScope);
//                    }
//                }
//                return result;
//            }
//        });

		scope.put(new AbstractFunction("map", comment("Apply a lambda to all list elements."), false, "func", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				FplLambda function = evaluateToLambda(scope, parameters[0]);
				try {
					return FplList.fromIterator(list.lambdaIterator(scope, function), list.size());
				} catch (TunnelException e) {
					throw e.getTunnelledException();
				}
			}
		});

		scope.put(new AbstractFunction("filter", comment("Filter a list elements."), false, "func", "list") {
			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				FplList list = evaluateToList(scope, parameters[1]);
				FplLambda function = evaluateToLambda(scope, parameters[0]);
				Iterator<FplValue> iter = list.iterator();
				List<FplValue> results = new ArrayList<>();
				while (iter.hasNext()) {
					FplValue value = iter.next();
					if (evaluateToBoolean(scope, function.call(scope, new FplValue[] { value }))) {
						results.add(value);
					}
				}
				return new FplList(results);
			}
		});
	}

	// More example for Lisp loops:
	// - http://www.ai.sri.com/~pkarp/loop.html
	// - Tutorial:
	// http://www.cs.tut.fi/lintula/manual/elisp/emacs-lisp-intro-1.05/emacs-lisp-intro_4.html
	// - http://ergoemacs.org/emacs/elisp_hash_table.html
}
