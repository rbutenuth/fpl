package de.codecentric.fpl;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;

/**
 * Should be implemented by classes which provide default methods.
 */
public interface ScopePopulator {
	/**
	 * Add functionality to a {@link Scope}. 
	 * Should use {@link Scope#define(de.codecentric.fpl.datatypes.Symbol, de.codecentric.fpl.datatypes.FplValue)} 
	 * overwriting already existing bindings. 
	 * @param scope Target scope for the functionality.
	 */
	public void populate(Scope scope) throws ScopeException, EvaluationException;
}
