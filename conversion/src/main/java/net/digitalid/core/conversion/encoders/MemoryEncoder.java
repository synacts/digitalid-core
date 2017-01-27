package net.digitalid.core.conversion.encoders;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.conversion.exceptions.MemoryExceptionBuilder;

/**
 * A memory encoder encodes values as XDF in memory.
 */
@Mutable
@GenerateSubclass
public abstract class MemoryEncoder extends XDFEncoder<MemoryException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull MemoryException createException(@Nonnull IOException exception) {
        return MemoryExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected MemoryEncoder(@Nonnull OutputStream outputStream) {
        super(outputStream);
    }
    
    /**
     * Returns an encoder for the given output stream.
     */
    @Pure
    public static @Nonnull MemoryEncoder of(@Nonnull OutputStream outputStream) {
        return new MemoryEncoderSubclass(outputStream);
    }
    
}
