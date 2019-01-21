package de.codecentric.fpl.data;

import java.util.SortedSet;

/**
 * A {@link Scope} where you can list all keys.
 */
public interface ListableScope extends Scope {

    /**
     * @return All keys from this scope, ordered by natural {@link String} order.
     */
    public SortedSet<String> allKeys();
}
