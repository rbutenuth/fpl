package de.codecentric.fpl.builtin;

import java.util.SortedSet;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.FplEngine;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

public class ScopeFactoryDump {

    /**
     * @param args Unused
     * @throws EvaluationException Should not happen
     * @throws ScopeException Should not happen
     */
    public static void main(String[] args) throws EvaluationException, ScopeException {
        Scope scope = new FplEngine().getScope();
        SortedSet<String> keys = scope.allKeys();
        for (String key : keys) {
            FplValue value = scope.get(key);
            if (value instanceof Function) {
                Function f = (Function)value;
                System.out.print("(" + f.getName());
                String[] pns = f.getParameterNames();
                for (String pn : pns) {
                    System.out.print(" " + pn);
                }
                System.out.println(")");
            }
        }
    }

}
