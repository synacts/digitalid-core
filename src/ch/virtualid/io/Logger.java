package ch.virtualid.io;

import ch.virtualid.exceptions.ShouldNeverHappenError;
import static ch.virtualid.io.Level.INFORMATION;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nonnull;

/**
 * The logger logs messages with the current time.
 * <p>
 * <em>Warning:</em> Logging from different processes to the same file may fail!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Logger {
    
    /**
     * Stores the level above which messages are logged.
     */
    public static final @Nonnull Level LEVEL = INFORMATION;
    
    
    /**
     * Stores the reference to the print stream.
     */
    private final @Nonnull PrintStream out;
    
    /**
     * Stores the date formatter for the output.
     */
    private static final @Nonnull ThreadLocal<DateFormat> formatter = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
        }
    };
    
    /**
     * Creates a new logger that writes to the standard output.
     */
    public Logger() {
        this(System.out);
    }
    
    /**
     * Creates a new logger that writes to the given file.
     * 
     * @param file the file to write the log messages to.
     */
    public Logger(@Nonnull File file) throws FileNotFoundException {
        this(new PrintStream(new FileOutputStream(file, true)));
    }
    
    /**
     * Creates a new logger that writes to the given print stream.
     * 
     * @param out the print stream to write the log messages to.
     */
    public Logger(@Nonnull PrintStream out) {
        this.out = out;
    }
    
    /**
     * Creates a new logger that writes to the file with the given name.
     * 
     * @param name the name of the file to write the log messages to.
     */
    public Logger(@Nonnull String name) {
        try {
            this.out = new PrintStream(new FileOutputStream(Directory.LOGS.getPath() +  Directory.SEPARATOR + name, true));
        } catch (FileNotFoundException exception) {
            throw new ShouldNeverHappenError("Could not open the log file '" + name + "'.", exception);
        }
    }
    
    /**
     * Logs the given message with the current time.
     * 
     * @param level the log level of the message.
     * @param message the message to log.
     */
    public synchronized void log(@Nonnull Level level, @Nonnull String message) {
        if (level.getValue() >= LEVEL.getValue()) {
            out.println(formatter.get().format(new Date()) + " " + level + ": " + message);
            out.flush();
        }
    }
    
    /**
     * Logs the given message and exception with the current time.
     * 
     * @param level the log level of the message.
     * @param message the message to log.
     * @param exception the exception to log.
     */
    public void log(@Nonnull Level level, @Nonnull String message, @Nonnull Exception exception) {
        log(level, message + " [" + getMessage(exception) + "]");
    }
    
    /**
     * Returns recursively the message of the exception and its cause.
     * 
     * @param exception the exception whose message is to be returned.
     * 
     * @return recursively the message of the exception and its cause.
     */
    private @Nonnull String getMessage(@Nonnull Throwable exception) {
        return exception.getClass().getSimpleName() + ": " + exception.getMessage() + (exception.getCause() != null ? " [" + getMessage(exception.getCause()) + "]" : "");
    }
    
}
