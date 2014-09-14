package ch.virtualid.packet;

import static ch.virtualid.io.Level.WARNING;
import ch.virtualid.io.Logger;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when a request could not be completed.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class FailedRequestException extends Exception {
    
    /**
     * Stores the logger for failed requests.
     */
    private static final @Nonnull Logger logger = new Logger("Requests.log");
    
    /**
     * Creates a new failed request exception with the given problem.
     * 
     * @param problem a string indicating the kind of problem.
     */
    public FailedRequestException(@Nonnull String problem) {
        super(problem);
        
        logger.log(WARNING, problem);
    }
    
    /**
     * Creates a new failed request exception with the given problem and cause.
     * 
     * @param problem a string indicating the kind of problem.
     * @param cause a reference to the cause of the problem.
     */
    public FailedRequestException(@Nonnull String problem, @Nonnull Exception cause) {
        super(problem, cause);
        
        logger.log(WARNING, problem, cause);
    }
    
}
