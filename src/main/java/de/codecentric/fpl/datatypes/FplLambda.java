package de.codecentric.fpl.datatypes;

import java.util.Iterator;
import java.util.Map;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.ParameterScope;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.list.FplList;
import de.codecentric.fpl.parser.Position;

public class FplLambda extends AbstractFunction {
	private final Scope definitionScope;
	private final FplValue[] code;

	/**
	 * @param position   Position in source code.
	 * @param comment    A list of lines with comment in markdown syntax
	 * @param name       Name of the function.
	 * @param paramNames Names of parameters, if last one ends with "...", this is a
	 *                   variable list function.
	 * @param code       The Lisp code.
	 * @param scope      The scope in which this function is defined. This is the
	 *                   <code>next</code> when the function is called.
	 */
	public FplLambda(Position position, String name, String comment, String[] paramNames, FplValue[] code, Scope scope)
			throws EvaluationException {
		super(position, name, comment, paramNames);
		Map<String, Integer> parameterMap = getParameterNameToIndex();
		this.code = compile(code, parameterMap);
		definitionScope = scope;
	}

	/**
	 * @see AbstractFunction.data.Function#callInternal(lang.data.Scope,
	 *      FplValue.data.LObject[])
	 */
	@Override
	public FplValue callInternal(final Scope scope, final FplValue... parameters) throws EvaluationException {
		FplValue[] scopeParameters = new FplValue[getNumberOfParameterNames()];
		int lastNamedIndex = getNumberOfParameterNames() - 1;
		if (isVarArg()) {
			for (int i = 0; i < getMinimumNumberOfParameters(); i++) {
				scopeParameters[i] = FplLazy.make(scope, parameters[i]);
			}
			int count = parameters.length - getMinimumNumberOfParameters();
			scopeParameters[lastNamedIndex] = FplList.fromIterator(new Iterator<FplValue>() {
				int i = 0;
				int j = lastNamedIndex;
				
				@Override
				public boolean hasNext() {
					return i < count;
				}

				@Override
				public FplValue next() {
					i++;
					return FplLazy.make(scope, parameters[j++]);
				}
			}, count);
		} else {
			for (int i = 0; i < parameters.length; i++) {
				scopeParameters[i] = FplLazy.make(scope, parameters[i]);
			}
		}
		ParameterScope callScope = new ParameterScope(getName(), definitionScope, getParameterNameToIndex(),
				scopeParameters);
		FplValue result = null;
		for (int i = 0; i < code.length; i++) {
			result = code[i] == null ? null : code[i].evaluate(callScope);
		}
		return result;
	}

	private FplValue[] compile(FplValue[] code, Map<String, Integer> parameterMap) {
		FplValue[] compiled = new FplValue[code.length];
		for (int i = 0; i < code.length; i++) {
			compiled[i] = compile(code[i], parameterMap);
		}
		return compiled;
	}

	private FplValue compile(FplValue code, Map<String, Integer> parameterMap) {
		if (code instanceof FplList) {
			FplList list = (FplList) code;
			Iterator<FplValue> iter = list.iterator();
			return FplList.fromIterator(new Iterator<FplValue>() {
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Override
				public FplValue next() {
					return compile(iter.next(), parameterMap);
				}
			}, list.size());
		} else {
			if (code instanceof Symbol) {
				Symbol s = (Symbol) code;
				Integer index = parameterMap.get(s.getName());
				if (index == null) {
					return s;
				} else {
					return new Parameter(s, index);
				}
			} else if (code instanceof Parameter) {
				Parameter p = (Parameter) code;
				String name = p.getName();
				Integer index = parameterMap.get(name);
				if (index == null) {
					return p.getSymbol();
				} else {
					return new Parameter(p.getSymbol(), index);
				}
			} else {
				return code;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(lambda (");
		int i = 0;
		for (String name : getParameterNameToIndex().keySet()) {
			sb.append(name);
			if (i < getNumberOfParameterNames() - 1) {
				sb.append(' ');
			} else {
				if (isVararg()) {
					sb.append("...");
				}
			}
			i++;
		}
		sb.append(") ");
		for (i = 0; i < code.length; i++) {
			sb.append(code[i] == null ? "nil" : code[i].toString());
			if (i < code.length - 1) {
				sb.append(' ');
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
