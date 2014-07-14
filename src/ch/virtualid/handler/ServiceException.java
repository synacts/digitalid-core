package ch.virtualid.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown when a service is not implemented correctly.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ServiceException extends Exception {
    
    /**
     * Creates a new service exception with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public ServiceException(@Nonnull String problem) {
        this(problem, null);
    }
    
    /**
     * Creates a new service exception with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     */
    public ServiceException(@Nonnull String problem, @Nullable Exception cause) {
        super(problem, cause);
    }
    
}
