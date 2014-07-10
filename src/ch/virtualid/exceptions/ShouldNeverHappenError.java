package ch.virtualid.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This error is thrown when an error occurs which should never happen.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ShouldNeverHappenError extends FatalError {
    
    /**
     * Creates a new error which should never happen with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public ShouldNeverHappenError(@Nonnull String problem) {
        this(problem, null);
    }
    
    /**
     * Creates a new error which should never happen with the given cause.
     * 
     * @param cause a reference to the cause of the problem.
     */
    public ShouldNeverHappenError(@Nonnull Exception cause) {
        this(null, cause);
    }
    
    /**
     * Creates a new error which should never happen with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     * 
     * @require problem != null || cause != null : "The problem and the cause may not both be null.";
     */
    public ShouldNeverHappenError(@Nullable String problem, @Nullable Exception cause) {
        super(problem, cause);
        
        assert problem != null || cause != null : "The problem and the cause may not both be null.";
    }
    
}
