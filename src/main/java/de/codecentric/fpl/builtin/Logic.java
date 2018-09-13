package de.codecentric.fpl.builtin;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;
import de.codecentric.fpl.datatypes.Symbol;
import de.codecentric.fpl.datatypes.list.FplList;

/**
 * Basic logic functions. <code>LInteger(0)</code> and <code>null</code> are false, everything else is true.
 */
public class Logic extends Function {
    private final static FplInteger L_TRUE = FplInteger.valueOf(1);

    /**
     * @param scope Scope to which functions should be added.
     * @throws EvaluationException Should not happen on initialization.
     */
    public static void put(Scope scope) throws EvaluationException {
    	
    	scope.put(new Logic("and", comment("Logic and of parameters.")));
    	scope.put(new Logic("or", comment("Logic or of parameters.")));
    	scope.put(new Logic("xor", comment("Logic xor of parameters.")));
    	scope.put(new Logic("not", comment("Logic not of parameter.")));

        scope.put(new Function("is-symbol", comment("Is expression a symbol?"), false, "expression") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                return parameters[0].evaluate(scope) instanceof Symbol ? L_TRUE : null;
            }
        });

        scope.put(new Function("is-integer", comment("Is expression an integer?"), false, "expression") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                return parameters[0].evaluate(scope) instanceof FplInteger ? L_TRUE : null;
            }
        });

        scope.put(new Function("is-double", comment("Is expression a double?"), false, "expression") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                return parameters[0].evaluate(scope) instanceof FplDouble ? L_TRUE : null;
            }
        });

        scope.put(new Function("is-list", comment("Is expression a list?"), false, "expression") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                return parameters[0].evaluate(scope) instanceof FplList ? L_TRUE : null;
            }
        });

        scope.put(new Function("is-function", comment("Is expression a function?"), false, "expression") {

            @Override
            public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
                return parameters[0].evaluate(scope) instanceof Function ? L_TRUE : null;
            }
        });
    }

    /**
     * @param op Operator: "and", "or", etc.
     */
    private Logic(String op, List<String> comment) {
        super(op, comment, !op.equals("not"), "expression");
    }

    /**
     * @see lang.data.Function#callInternal(lang.data.Scope, FplValue.data.LObject[])
     */
    @Override
    public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
        if (getName().equals("not")) {
            return booleanValue(scope, parameters[0]) ? null : L_TRUE;
        }
        boolean current = getName().equals("and");
        for (FplValue parameter : parameters) {
            boolean next = booleanValue(scope, parameter);
            switch (getName()) {
            case "and":
                current &= next;
                if (!current) {
                    return null;
                }
                break;
            case "or":
                current |= next;
                if (current) {
                    return L_TRUE;
                }
                break;
            case "xor":
                current ^= next;
                break;
            }
        }
        return current ? L_TRUE : null;
    }

    private boolean booleanValue(Scope scope, FplValue expression) throws EvaluationException {
        FplValue value = expression.evaluate(scope);
        if (value == null) {
            return false;
        }
        if (value instanceof FplInteger) {
            return ((FplInteger) value).getValue() != 0;
        }
        if (value instanceof FplList) {
            return ((FplList) value).size() != 0;
        }
        return true;
    }
}
