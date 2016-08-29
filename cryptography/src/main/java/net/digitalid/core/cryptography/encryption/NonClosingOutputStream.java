package net.digitalid.core.cryptography.encryption;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;

/**
 *
 */
public class NonClosingOutputStream extends OutputStream {
    
    private final @Nonnull OutputStream outputStream;
    
    public NonClosingOutputStream(@Nonnull OutputStream outputStream) {
        this.outputStream = outputStream;
    }
    
    @Impure
    @Override
    public void write(int i) throws IOException {
        outputStream.write(i);
    }
    
    @Pure
    @Override
    public void close() throws IOException {
        // intentionally left blank
    }
    
    @Impure
    public void actualClose() throws IOException {
        outputStream.close();
    }
    
}
