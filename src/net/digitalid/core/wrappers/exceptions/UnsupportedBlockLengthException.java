package net.digitalid.core.wrappers.exceptions;

import java.io.IOException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;

/**
 * Blocks that are larger than the maximum integer are not supported by this library due to the array limitations of Java.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class UnsupportedBlockLengthException extends IOException {
    
    /**
     * Creates a new unsupported block length exception.
     */
    private UnsupportedBlockLengthException() {}
    
    /**
     * Returns a new unsupported block length exception.
     * 
     * @return a new unsupported block length exception.
     */
    @Pure
    public static @Nonnull UnsupportedBlockLengthException get() {
        return new UnsupportedBlockLengthException();
    }
    
}
