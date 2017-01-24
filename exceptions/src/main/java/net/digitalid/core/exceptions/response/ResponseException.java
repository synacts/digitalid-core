package net.digitalid.core.exceptions.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identity.InternalIdentity;

/**
 * A response exception indicates that a response is semantically invalid.
 * (Syntactic problems are indicated by the {@link RecoveryException}.
 */
@Immutable
public abstract class ResponseException extends ExternalException {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    /**
     * Returns the identity whose response was invalid.
     */
    @Pure
    public abstract @Nonnull InternalIdentity getIdentity();
    
    /* -------------------------------------------------- Cause -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable Throwable getCause() {
        return null;
    }
    
}
