package net.digitalid.core.conversion.streams.output;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This class wraps an output stream and does not close the stream with the normal method call.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class NonClosingOutputStream extends OutputStream {
    
    @Pure
    protected abstract @Nonnull OutputStream getOutputStream();
    
    @Impure
    @Override
    public void write(int i) throws IOException {
        getOutputStream().write(i);
    }
    
    @Pure
    @Override
    public void close() throws IOException {
        // intentionally left blank
    }
    
    @Impure
    public void actualClose() throws IOException {
        getOutputStream().close();
    }
    
}
