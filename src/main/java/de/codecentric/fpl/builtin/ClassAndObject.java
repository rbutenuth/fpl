package de.codecentric.fpl.builtin;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.ScopePopulator;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.AbstractFunction;
import de.codecentric.fpl.datatypes.FplObject;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.parser.Position;

/**
 * Class and Object related functions.
 */
public class ClassAndObject implements ScopePopulator {
	@Override
	public void populate(Scope scope) throws ScopeException, EvaluationException {

		scope.define(new AbstractFunction("class", "Create a new scope and execute the given code within it.",
				"code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Position position = FplValue.position(parameters[0]);
				return makeClass("class", position, skipParameterScopes(scope), parameters, 0);
			}

		});

		scope.define(new AbstractFunction("def-class", 
				"Create a new scope and execute the given code within it. Assign the resulting class to \"name\"",
				"name", "code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String target = Assignment.targetName(scope, parameters[0]);
				FplObject obj = makeClass(target, FplValue.position(parameters[0]), skipParameterScopes(scope), parameters, 1);
				try {
					scope.define(target, obj);
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
				return obj;
			}

		});

		scope.define(new AbstractFunction("sub-class",
				"Create a new scope and execute the given code within it, set parent to parameter.", "parent",
				"code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				Position position = FplValue.position(parameters[0]);
				FplObject parent = evaluateToObject(scope, parameters[0]);
				return makeClass("sub-class-of-" + parent.getName(), position, parent, parameters, 1);
			}

		});

		scope.define(new AbstractFunction("def-sub-class", "Define a class and set the parent of the class.",
				"name", "parent", "code...") {

			@Override
			protected FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				String target = Assignment.targetName(scope, parameters[0]);
				FplObject parent = evaluateToObject(scope, parameters[1]);
				FplObject obj = makeClass(target, FplValue.position(parameters[0]), parent, parameters, 2);
				try {
					scope.define(target, obj);
				} catch (ScopeException e) {
					throw new EvaluationException(e.getMessage());
				}
				return obj;
			}

		});

		scope.define(new AbstractFunction("new-instance", "Create an instance of an object.", "key-value-pair...") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				if (parameters.length % 2 != 0) {
					throw new EvaluationException("Number of parameters must be even.");
				}
				Scope objectScope = scope;
				while (objectScope instanceof ParameterScope) {
					objectScope = objectScope.getNext();
				}
				FplObject object = new FplObject("instance-of-" + getName(), Position.UNKNOWN, objectScope);

				initializeObject(scope, parameters, object);
				return object;
			}

		});

		scope.define(new AbstractFunction("this",
				"The next object in the scope chain, can be nil when not within an object context.") {

			@Override
			public FplValue callInternal(Scope scope, FplValue... parameters) throws EvaluationException {
				return thisFromScope(scope);
			}
		});
	}

	static private FplObject makeClass(String name, Position position, Scope next, FplValue[] parameters, int first)
			throws EvaluationException {
		FplObject obj = new FplObject(name, position, next);
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
}
