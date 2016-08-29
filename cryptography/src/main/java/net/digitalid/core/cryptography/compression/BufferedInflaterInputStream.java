package net.digitalid.core.cryptography.compression;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;

/**
 *
 */
public class BufferedInflaterInputStream extends InflaterInputStream {
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream, @Nonnull Inflater inflater, int size) {
        super(new BufferedInputStream(inputStream), inflater, size);
    }
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream, @Nonnull Inflater inflater) {
        this(inputStream, inflater, 512);
    }
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream) {
        this(inputStream, new Inflater());
    }
    
    @Impure
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        super.in.mark(length - offset);
        int inflatedBytes = super.read(bytes, offset, length);
        super.in.reset();
        long compressedBytesRead = super.inf.getBytesRead();
        super.in.skip(compressedBytesRead);
        return inflatedBytes;
    }
    
    @Pure
    public @Nonnull InputStream finish() {
        return super.in;
    }
}
