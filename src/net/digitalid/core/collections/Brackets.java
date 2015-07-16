package net.digitalid.core.collections;

import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;

/**
 * This class enumerates the various brackets that can be used.
 * 
 * @see IterableUtility
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public enum Brackets {
    
    /**
     * The round brackets '()'.
     */
    ROUND('(', ')'),
    
    /**
     * The square brackets '[]'.
     */
    SQUARE('[', ']'),
    
    /**
     * The curly brackets '{}'.
     */
    CURLY('{', '}'),
    
    /**
     * The pointy brackets '<>'.
     */
    POINTY('<', '>');
    
    
    /**
     * Stores the opening bracket.
     */
    private final char opening;
    
    /**
     * Stores the closing bracket.
     */
    private final char closing;
    
    /**
     * Creates new brackets with the given chars.
     * 
     * @param opening the opening bracket.
     * @param closing the closing bracket.
     */
    private Brackets(char opening, char closing) {
        this.opening = opening;
        this.closing = closing;
    }
    
    /**
     * Returns the opening bracket.
     * 
     * @return the opening bracket.
     */
    @Pure
    public char getOpening() {
        return opening;
    }
    
    /**
     * Returns the closing bracket.
     * 
     * @return the closing bracket.
     */
    @Pure
    public char getClosing() {
        return closing;
    }
    
}
