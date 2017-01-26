package net.digitalid.core.conversion.exceptions;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A stream exception is thrown whenever a network or file stream is corrupted by external causes.
 * 
 * @see FileException
 * @see NetworkException
 */
@Immutable
public abstract class StreamException extends ConnectionException {
    
    /* -------------------------------------------------- Cause -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull IOException getCause();
    
}
