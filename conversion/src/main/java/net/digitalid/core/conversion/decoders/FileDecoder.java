package net.digitalid.core.conversion.decoders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
public abstract class FileDecoder extends XDFDecoder<FileException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull FileException createException(@Nonnull IOException exception) {
        return FileExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected FileDecoder(@Nonnull InputStream inputStream) {
        super(inputStream);
    }
    
    /**
     * Returns a decoder for the given file.
     */
    @Pure
    public static @Nonnull FileDecoder of(@Nonnull File file) throws FileException {
        try {
            return new FileDecoderSubclass(new FileInputStream(file));
        } catch (@Nonnull FileNotFoundException exception) {
            throw FileExceptionBuilder.withCause(exception).build();
        }
    }
    
}
