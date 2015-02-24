package net.digitalid.core.io;

import java.io.IOException;
import java.io.Writer;
import javax.annotation.Nonnull;

/**
 * This class extends the {@link java.io.BufferedWriter} with a {@link #writeLine(java.lang.String)} method.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class BufferedWriter extends java.io.BufferedWriter {
    
    /**
     * Creates a buffered character-output stream with a default-sized buffer.
     *
     * @param writer the writer to write to.
     */
    public BufferedWriter(@Nonnull Writer writer) {
        super(writer);
    }
    
    /**
     * Writes the given line, makes a new line and flushes the buffer.
     * 
     * @param line the line to write.
     */
    public void writeLine(@Nonnull String line) throws IOException {
        write(line);
        newLine();
        flush();
    }
    
}
