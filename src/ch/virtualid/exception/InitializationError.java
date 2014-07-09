package ch.virtualid.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This error is thrown when an error occurs during initialization.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class InitializationError extends FatalError {
    
    /**
     * Creates a new initialization error with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public InitializationError(@Nonnull String problem) {
        this(problem, null);
    }
    
    /**
     * Creates a new initialization error with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     */
    public InitializationError(@Nonnull String problem, @Nullable Exception cause) {
        super(problem, cause);
    }
    
}
