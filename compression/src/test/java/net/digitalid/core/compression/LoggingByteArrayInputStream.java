package net.digitalid.core.compression;

import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.type.Mutable;

@Mutable
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class LoggingByteArrayInputStream extends ByteArrayInputStream {
    
    private final boolean logging;
    
    public LoggingByteArrayInputStream(byte[] buffer, boolean logging) {
        super(buffer);
        
        this.logging = logging;
    }
    
    @Impure
    @Override
    public synchronized int read() {
        final int read = super.read();
        
        final @Nonnull byte[] bytes = new byte[1];
        bytes[0] = (byte) read;
        if (logging) { System.out.println("Read single " + Strings.hex(bytes)); }
        
        return read;
    }
    
    @Impure
    @Override
    public synchronized int read(byte b[], int off, int len) {
        final int length = super.read(b, off, len);
        
        if (length > 0) { 
            final @Nonnull byte[] bytes = new byte[length];
            System.arraycopy(b, off, bytes, 0, length);
            if (logging) { System.out.println("Read array  " + Strings.hexWithSpaces(bytes)); }
        } else if (length == 0) {
            if (logging) { System.out.println("Read nothing"); }
        } else if (length == -1) {
            if (logging) { System.out.println("End of input"); }
        }
        
        return length;
    }
    
    @Pure
    public int getPosition() {
        return pos;
    }
    
}
