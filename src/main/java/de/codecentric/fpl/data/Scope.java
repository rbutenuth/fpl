package de.codecentric.fpl.data;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

/**
 * For parameters or key value mappings.
 */
public interface Scope {
    /**
     * @return Next outer scope, may be null.
     */
    public Scope getNext();

    /**
     * Lookup a symbol, if not found in this scope, walk chain of scopes.
     * @param key Name of value to lookup
     * @return The found expression, may be null.
     */
    public FplValue get(String key);

    /**
     * Put a key value mapping in the scope, may overwrite an existing mapping.
     * @param key Name of value to lookup, not null, not empty
     * @param value The value of the symbol, null values are allowed.
     * @throws EvaluationException If scope is sealed (see {@link #isSealed()}).
     */
    public void put(String key, FplValue value) throws EvaluationException;

    /**
     * Put a key value mapping in the scope, may overwrite an existing mapping.
     * The name is taken from the value.
     * @param value The value of the symbol, null values are allowed.
     * @throws EvaluationException If scope is sealed (see {@link #isSealed()}).
     */
    public void put(Named value) throws EvaluationException;

    /**
     * Search scopes starting from this one via {@link #getNext()}. If a {@link #isSealed()} scope is found, put the value in
     * the one before (can be this scope).
     * @param key Name of value to lookup, not null, not empty
     * @param value The value of the symbol, null values are allowed.
     * @throws EvaluationException If this scope is sealed.
     */
    public void putGlobal(String key, FplValue value) throws EvaluationException;
    
    /**
     * @return Is this scope read only?
     */
    public boolean isSealed();
}
