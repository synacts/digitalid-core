package ch.xdf.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown when a block has an invalid encoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class InvalidEncodingException extends Exception {
    
    /**
     * Creates a new invalid encoding exception with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public InvalidEncodingException(@Nonnull String problem) {
        this(problem, null);
    }
    
    /**
     * Creates a new invalid encoding exception with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     */
    public InvalidEncodingException(@Nonnull String problem, @Nullable Exception cause) {
        super(problem, cause);
    }
    
}
