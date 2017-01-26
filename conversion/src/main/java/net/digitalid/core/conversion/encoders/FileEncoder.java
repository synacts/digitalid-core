package net.digitalid.core.conversion.encoders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.conversion.exceptions.FileExceptionBuilder;

/**
 * A file encoder encodes values as XDF to a file.
 */
@Mutable
@GenerateSubclass
public abstract class FileEncoder extends XDFEncoder<FileException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull FileException createException(@Nonnull IOException exception) {
        return FileExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected FileEncoder(@Nonnull OutputStream outputStream) {
        super(outputStream);
    }
    
    /**
     * Returns an encoder for the given file.
     */
    @Pure
    public static @Nonnull FileEncoder of(@Nonnull File file) throws FileException {
        try {
            return new FileEncoderSubclass(new FileOutputStream(file));
        } catch (@Nonnull FileNotFoundException exception) {
            throw FileExceptionBuilder.withCause(exception).build();
        }
    }
    
}
