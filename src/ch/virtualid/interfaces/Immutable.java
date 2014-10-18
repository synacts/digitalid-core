package ch.virtualid.interfaces;

import ch.virtualid.identity.Person;

/**
 * Classes that implement this interface guarantee that their objects are immutable.
 * Since this interface does not specify any methods, it is for indication purposes only.
 * <p>
 * An object is considered immutable, if its representation (usually the data that is included in its {@link Blockable block}) is fixed.
 * Other objects that are not fully part of its representation but can nonetheless be reached through its fields may still be mutable.
 * <p>
 * It should always be safe to share immutable objects between various instances and threads.
 * However, it is in general not guaranteed that the hash of immutable objects stays the same.
 * In other words, an immutable object is only conceptually immutable but its values may change.
 * (This is the case with references to persons, which remain constant but can still be merged.)
 * 
 * @see Person
 * @see Freezable
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface Immutable {}
