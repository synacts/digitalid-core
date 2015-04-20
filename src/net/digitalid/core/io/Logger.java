package net.digitalid.core.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Initialized;

/**
 * The logger logs messages of various {@link Level levels}.
 * <p>
 * <em>Warning:</em> Logging from different processes to the same file may fail!
 * 
 * @see DefaultLogger
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Logger {
    
    /**
     * Stores the concrete logger.
     */
    private static @Nullable Logger logger;
    
    /**
     * Initializes this class with the given concrete logger.
     * 
     * @param logger an implementation of this abstract class.
     */
    public static void initialize(@Nonnull Logger logger) {
        Logger.logger = logger;
    }
    
    /**
     * Returns whether this class is initialized.
     * 
     * @return whether this class is initialized.
     */
    public static boolean isInitialized() {
        return logger != null;
    }
    
    
    /**
     * Logs the given message with the given tag and exception.
     * 
     * @param level the log level of the message.
     * @param tag a tag to annotate the message.
     * @param message the message to be logged.
     * @param throwable the throwable to log.
     */
    protected abstract void protectedLog(@Nonnull Level level, @Nonnull String tag, @Nonnull String message, @Nullable Throwable throwable);
    
    /**
     * Logs the given message with the given tag and exception.
     * 
     * @param level the log level of the message.
     * @param tag a tag to annotate the message.
     * @param message the message to be logged.
     * @param throwable the throwable to log.
     */
    @Initialized
    public static void log(@Nonnull Level level, @Nonnull String tag, @Nonnull String message, @Nullable Throwable throwable) {
        assert logger != null : "This class is initialized.";
        
        logger.protectedLog(level, tag, message, throwable);
    }
    
    /**
     * Logs the given message with the given tag.
     * 
     * @param level the log level of the message.
     * @param tag a tag to annotate the message.
     * @param message the message to be logged.
     */
    @Initialized
    public static void log(@Nonnull Level level, @Nonnull String tag, @Nonnull String message) {
        log(level, tag, message, null);
    }
    
}
