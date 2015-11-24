package net.digitalid.service.core.exceptions.network;

import java.io.File;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception indicates that a file was not found.
 */
@Immutable
public class FileNotFoundException extends NetworkException {
    
    /* -------------------------------------------------- File -------------------------------------------------- */
    
    /**
     * Stores the file that was not found.
     */
    private final @Nonnull File file;
    
    /**
     * Returns the file that was not found.
     * 
     * @return the file that was not found.
     */
    @Pure
    public final @Nonnull File getFile() {
        return file;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new file not found exception.
     * 
     * @param exception the original exception.
     * @param file the file that was not found.
     */
    protected FileNotFoundException(@Nonnull java.io.FileNotFoundException exception, @Nonnull File file) {
        super(exception, null);
        
        this.file = file;
    }
    
    /**
     * Returns a new file not found exception.
     * 
     * @param exception the original exception.
     * @param file the file that was not found.
     * 
     * @return a new file not found exception.
     */
    @Pure
    public static final @Nonnull FileNotFoundException get(@Nonnull java.io.FileNotFoundException exception, @Nonnull File file) {
        return new FileNotFoundException(exception, file);
    }
    
}
