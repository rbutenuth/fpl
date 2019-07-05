package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.parser.Position;

/**
 * Class and Object related functions.
 */
public class ClassAndObject implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException {

		scope.define(new AbstractFunction("class", comment("Create a new scope and execute the given code within it."),
				true, "code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Position position = determinePosition(parameters[0]);
				return makeClass(position, skipParameterScopes(scope), parameters, 0);
			}

		});

		scope.define(new AbstractFunction("def-class", comment(
				"Create a new scope and execude the given code within it. Assign the resulting class to \"name\""),
				true, "name", "code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Symbol target = Assignment.targetSymbol(scope, parameters[0]);
				FplObject obj = makeClass(target.getPosition(), skipParameterScopes(scope), parameters, 1);
				try {
					scope.define(target, obj);
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
				return obj;
			}

		});

		scope.define(new AbstractFunction("sub-class",
				comment("Create a new scope and execute the given code within it, set parent to parameter."), true,
				"parent", "code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Position position = determinePosition(parameters[0]);
				FplValue parent = evaluateToObject(scope, parameters[0]);
				return makeClass(position, (FplObject)parent, parameters, 1);
			}

		});

		// (def-sub-class name parent [one or more code blocks to define it])
		scope.define(new AbstractFunction("def-sub-class", comment("TODO"), true, "name", "parent", "code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				Symbol target = Assignment.targetSymbol(scope, parameters[0]);
				FplValue parent = evaluateToObject(scope, parameters[1]);
				FplObject obj = makeClass(target.getPosition(), (FplObject)parent, parameters, 2);
				try {
					scope.define(target, obj);
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
				return obj;
			}

		});

		scope.define(new AbstractFunction("new-instance", comment("Create an instce of an object."), true,
				"key-value-pair...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				if (parameters.length % 2 != 0) {
					throw new EvaluationException("Number of parameters must be even.");
				}
				Scope objectScope = scope;
				while (objectScope instanceof ParameterScope) {
					objectScope = objectScope.getNext();
				}
				FplObject object = new FplObject(Position.UNKNOWN, objectScope);

				initializeObject(scope, parameters, object);
				return object;
			}

		});

		scope.define(new AbstractFunction("this",
				comment("The next object in the scope chain, can be nil when not within an object context."), false) {

			@Override
			public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
				return thisFromScope(scope);
			}
		});
	}

	static private FplObject makeClass(Position position, Scope next, FplValue[] parameters, int first)
			throws EvaluationException {
		FplObject obj = new FplObject(position, next);
		for (int i = first; i < parameters.length; i++) {
			parameters[i].evaluate(obj);
		}
		return obj;
	}

	static private FplObject thisFromScope(Scope scope) {
		Scope result = scope;
		while (result != null && !(result instanceof FplObject)) {
			result = result.getNext();
		}
		return (FplObject) result;
	}

	static private Scope skipParameterScopes(Scope scope) {
		Scope result = scope;
		while (result instanceof ParameterScope) {
			result = result.getNext();
		}
		return result;
	}

	static private void initializeObject(Scope scope, FplValue[] parameters, FplObject object)
			throws EvaluationException {
		int keyValueCount = parameters.length / 2;
		String[] keys = new String[keyValueCount];
		FplValue[] values = new FplValue[keyValueCount];
		for (int i = 0; i < keyValueCount; i++) {
			keys[i] = Assignment.targetName(scope, parameters[i * 2]);
			values[i] = Assignment.value(scope, parameters[i * 2 + 1]);
		}
		for (int i = 0; i < keyValueCount; i++) {
			try {
				object.put(keys[i], values[i]);
			} catch (ScopeException e) {
				throw new EvaluationException(e.getMessage());
			}
		}
	}

	private static Position determinePosition(FplValue value) {
		return Position.UNKNOWN; // // TODO try to determine better value (search for Symbol)
	}
}
