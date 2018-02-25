package de.codecentric.fpl.builtin;

import java.util.List;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplDouble;
import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

/**
 * Basic comparison functions.
 */
public class Comparison extends Function {
    private static FplInteger TRUE = FplInteger.valueOf(1);

    /**
     * @param scope Scope to which functions should be added.
     * @throws EvaluationException Should not happen on initialization.
     */
    public static void put(Scope scope) throws EvaluationException {

    	scope.put(new Comparison("eq", comment("Compare for equal.")));
    	scope.put(new Comparison("ne", comment("Compare for not equal.")));
    	scope.put(new Comparison("lt", comment("Compare left less than right.")));
    	scope.put(new Comparison("le", comment("Compare lest less or equal than right.")));
    	scope.put(new Comparison("gt", comment("Compare left greater than right.")));
    	scope.put(new Comparison("ge", comment("Compare left greater or equal than right.")));
    }

    private Comparison(String name, List<String> comment) {
        super(name, comment, false, "left", "right");
    }

    /**
     * @see lang.data.Function#callInternal(lang.data.Scope, FplValue.data.LObject[])
     */
    @Override
    public FplValue callInternal(Scope scope, FplValue[] parameters) throws EvaluationException {
        FplValue left = parameters[0] == null ? null : parameters[0].evaluate(scope);
        FplValue right = parameters[1] == null ? null : parameters[1].evaluate(scope);

        if (left == null) {
            if (right == null) {
                switch (getName()) {
                case "eq":
                    return TRUE;
                case "ne":
                    return null;
                default:
                    throw new EvaluationException("Comparison with null");
                }
            } else { // left == null, right != null
                switch (getName()) {
                case "eq":
                    return null;
                case "ne":
                    return TRUE;
                default:
                    throw new EvaluationException("Comparison with null");
                }
            }
        } else { // left != null
            if (right == null) {
                switch (getName()) {
                case "eq":
                    return null;
                case "ne":
                    return TRUE;
                default:
                    throw new EvaluationException("Comparison with null");
                }
            } else { // left != null, right != null
                return compareValues(left, right);
            }
        }
    }

    private FplValue compareValues(FplValue left, FplValue right) {
        // Precondition: left != null && right != null
        if (left instanceof FplInteger) {
            if (right instanceof FplInteger) {
                switch (getName()) {
                case "eq":
                    return ((FplInteger) left).getValue() == ((FplInteger) right).getValue() ? TRUE : null;
                case "ne":
                    return ((FplInteger) left).getValue() != ((FplInteger) right).getValue() ? TRUE : null;
                case "lt":
                    return ((FplInteger) left).getValue() < ((FplInteger) right).getValue() ? TRUE : null;
                case "le":
                    return ((FplInteger) left).getValue() <= ((FplInteger) right).getValue() ? TRUE : null;
                case "gt":
                    return ((FplInteger) left).getValue() > ((FplInteger) right).getValue() ? TRUE : null;
                case "ge":
                    return ((FplInteger) left).getValue() >= ((FplInteger) right).getValue() ? TRUE : null;
                }
            } else if (right instanceof FplDouble) {
                switch (getName()) {
                case "eq":
                    return ((FplInteger) left).getValue() == ((FplDouble) right).getValue() ? TRUE : null;
                case "ne":
                    return ((FplInteger) left).getValue() != ((FplDouble) right).getValue() ? TRUE : null;
                case "lt":
                    return ((FplInteger) left).getValue() < ((FplDouble) right).getValue() ? TRUE : null;
                case "le":
                    return ((FplInteger) left).getValue() <= ((FplDouble) right).getValue() ? TRUE : null;
                case "gt":
                    return ((FplInteger) left).getValue() > ((FplDouble) right).getValue() ? TRUE : null;
                case "ge":
                    return ((FplInteger) left).getValue() >= ((FplDouble) right).getValue() ? TRUE : null;
                }
            } else if (right instanceof FplString) {
                switch (getName()) {
                case "eq":
                    return null;
                case "ne":
                    return TRUE;
                default:
                    return null;
                }
            } else {
                return null;
            }
        } else if (left instanceof FplDouble) {
            if (right instanceof FplInteger) {
                switch (getName()) {
                case "eq":
                    return ((FplDouble) left).getValue() == ((FplInteger) right).getValue() ? TRUE : null;
                case "ne":
                    return ((FplDouble) left).getValue() != ((FplInteger) right).getValue() ? TRUE : null;
                case "lt":
                    return ((FplDouble) left).getValue() < ((FplInteger) right).getValue() ? TRUE : null;
                case "le":
                    return ((FplDouble) left).getValue() <= ((FplInteger) right).getValue() ? TRUE : null;
                case "gt":
                    return ((FplDouble) left).getValue() > ((FplInteger) right).getValue() ? TRUE : null;
                case "ge":
                    return ((FplDouble) left).getValue() >= ((FplInteger) right).getValue() ? TRUE : null;
                }
            } else if (right instanceof FplDouble) {
                switch (getName()) {
                case "eq":
                    return ((FplDouble) left).getValue() == ((FplDouble) right).getValue() ? TRUE : null;
                case "ne":
                    return ((FplDouble) left).getValue() != ((FplDouble) right).getValue() ? TRUE : null;
                case "lt":
                    return ((FplDouble) left).getValue() < ((FplDouble) right).getValue() ? TRUE : null;
                case "le":
                    return ((FplDouble) left).getValue() <= ((FplDouble) right).getValue() ? TRUE : null;
                case "gt":
                    return ((FplDouble) left).getValue() > ((FplDouble) right).getValue() ? TRUE : null;
                case "ge":
                    return ((FplDouble) left).getValue() >= ((FplDouble) right).getValue() ? TRUE : null;
                }
            } else if (right instanceof FplString) {
                switch (getName()) {
                case "eq":
                    return null;
                case "ne":
                    return TRUE;
                default:
                    return null;
                }
            } else {
                return null;
            }
        } else if (left instanceof FplString) {
            if (right instanceof FplInteger || right instanceof FplDouble) {
                switch (getName()) {
                case "eq":
                    return null;
                case "ne":
                    return TRUE;
                default:
                    return null;
                }
            } else if (right instanceof FplString) {
                switch (getName()) {
                case "eq":
                    return ((FplString) left).getContent().equals(((FplString) right).getContent()) ? TRUE : null;
                case "ne":
                    return ((FplString) left).getContent().equals(((FplString) right).getContent()) ? null : TRUE;
                case "lt":
                    return ((FplString) left).getContent().compareTo(((FplString) right).getContent()) < 0 ? TRUE : null;
                case "le":
                    return ((FplString) left).getContent().compareTo(((FplString) right).getContent()) <= 0 ? TRUE : null;
                case "gt":
                    return ((FplString) left).getContent().compareTo(((FplString) right).getContent()) > 0 ? TRUE : null;
                case "ge":
                    return ((FplString) left).getContent().compareTo(((FplString) right).getContent()) >= 0 ? TRUE : null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        // Not reached, but compiler does not recoginzes all switch statements are completely covered
        return null;
    }
}
