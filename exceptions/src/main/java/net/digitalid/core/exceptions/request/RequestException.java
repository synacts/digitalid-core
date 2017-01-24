package net.digitalid.core.exceptions.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Normalize;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A request exception indicates an error in the encoding or content of a request.
 * 
 * @see RequestErrorCode
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class RequestException extends ExternalException {
    
    /* -------------------------------------------------- Code -------------------------------------------------- */
    
    /**
     * Returns the error code of this request exception.
     */
    @Pure
    public abstract @Nonnull RequestErrorCode getCode();
    
    /* -------------------------------------------------- Message -------------------------------------------------- */
    
    @Pure
    @Override
    @Normalize("\"(\" + code + \") \" + message")
    public abstract @Nonnull String getMessage();
    
    /* -------------------------------------------------- Cause -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public abstract @Nullable Throwable getCause();
    
    /* -------------------------------------------------- Decoded -------------------------------------------------- */
    
    /**
     * Returns whether this exception was decoded from a block.
     */
    @Pure
    @NonRepresentative
    public abstract @Default("true") boolean isDecoded();
    
}
