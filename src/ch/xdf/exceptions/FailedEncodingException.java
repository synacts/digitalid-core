package ch.xdf.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown when a block could not be encoded.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class FailedEncodingException extends Exception {
    
    /**
     * Creates a new failed encoding exception with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public FailedEncodingException(@Nonnull String problem) {
        this(problem, null);
    }
    
    /**
     * Creates a new failed encoding exception with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     */
    public FailedEncodingException(@Nonnull String problem, @Nullable Exception cause) {
        super(problem, cause);
    }
    
}
