package de.codecentric.fpl;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;

/**
 * Should be implemented by classes which provide default methods.
 */
public interface ScopePopulator {
	/**
	 * Add functionality to the default scope of a {@link FplEngine}. 
	 * Should use {@link Scope#define(de.codecentric.fpl.datatypes.Symbol, de.codecentric.fpl.datatypes.FplValue)} 
	 * @param engine The engine to be used. 
	 */
	public void populate(FplEngine engine) throws ScopeException, EvaluationException;
}
