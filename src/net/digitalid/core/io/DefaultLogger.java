package net.digitalid.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.errors.InitializationError;
import net.digitalid.core.server.Server;

/**
 * This class implements a logger that logs the messages to a file.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class DefaultLogger extends Logger {
    
    /**
     * Stores the date formatter for the file name.
     */
    private static final @Nonnull ThreadLocal<DateFormat> day = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    
    /**
     * Stores the date formatter for the message time.
     */
    private static final @Nonnull ThreadLocal<DateFormat> time = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS");
        }
    };
    
    
    /**
     * Stores the level of this logger.
     */
    private final @Nonnull Level level;
    
    /**
     * Stores the name of this logger.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the date of the currently open log file.
     */
    private @Nonnull Date date;
    
    /**
     * Stores the print stream to which the messages are written.
     */
    private @Nonnull PrintStream out;
    
    /**
     * Rotates the log file.
     */
    private void rotate() {
        this.date = new Date();
        try {
            this.out = new PrintStream(new FileOutputStream(Directory.getLogsDirectory().getPath() +  File.separator + day.get().format(new Date()) + " " + name + ".log", true));
        } catch (@Nonnull FileNotFoundException exception) {
            throw new InitializationError("Could not open the log file '" + name + "'.", exception);
        }
    }
    
    /**
     * Creates a new logger that writes to the file with the given name.
     * 
     * @param level the level above which messages should be logged.
     * @param name the name of the file to write the log messages to.
     */
    public DefaultLogger(@Nonnull Level level, @Nonnull String name) {
        this.level = level;
        this.name = name;
        rotate();
    }
    
    @Override
    @SuppressWarnings("deprecation")
    protected synchronized void protectedLog(@Nonnull Level level, @Nonnull String tag, @Nonnull String message, @Nullable Throwable throwable) {
        final @Nonnull Date date = new Date();
        if (date.getDate() != this.date.getDate()) rotate();
        if (level.getValue() >= this.level.getValue()) {
            out.println(time.get().format(date) + " in " + Server.VERSION + " (" + level + ") [" + tag + "]: " + message);
            if (throwable != null) throwable.printStackTrace(out);
            out.flush();
        }
    }
    
}
