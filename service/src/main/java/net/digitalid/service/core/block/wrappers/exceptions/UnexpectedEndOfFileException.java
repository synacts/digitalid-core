package net.digitalid.service.core.block.wrappers.exceptions;

import java.io.IOException;
import javax.annotation.Nonnull;
import net.digitalid.utility.validation.state.Pure;

/**
 * The end of the input stream has been reached before the indicated data could be read.
 */
public final class UnexpectedEndOfFileException extends IOException {
    
    /**
     * Creates a new unexpected end-of-file exception.
     */
    private UnexpectedEndOfFileException() {}
    
    /**
     * Returns a new unexpected end-of-file exception.
     * 
     * @return a new unexpected end-of-file exception.
     */
    @Pure
    public static @Nonnull UnexpectedEndOfFileException get() {
        return new UnexpectedEndOfFileException();
    }
    
}
