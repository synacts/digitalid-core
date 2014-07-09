package ch.virtualid.exception;

import static ch.virtualid.io.Level.ERROR;
import ch.virtualid.io.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown when a fatal error occurs.
 * 
 * @see InitializationError
 * @see ShouldNeverHappenError
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class FatalError extends Error {
    
    /**
     * Stores the logger for fatal errors.
     */
    private static final @Nonnull Logger logger = new Logger("Errors.log");
    
    /**
     * Creates a new fatal error with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public FatalError(@Nonnull String problem) {
        this(problem, null);
    }
    
    /**
     * Creates a new fatal error with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     */
    public FatalError(@Nonnull String problem, @Nullable Exception cause) {
        super(problem, cause);
        
        if (cause == null) {
            logger.log(ERROR, problem);
        } else {
            logger.log(ERROR, problem, cause);
        }
    }
    
}
