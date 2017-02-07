package net.digitalid.core.conversion.streams.input;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.Review;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.validation.annotations.type.Mutable;

@Mutable
@Deprecated // TODO: Do we really need this wrapper? Also the mark and reset seems to be implemented wrongly according to the Javadoc.
@Review(date = "2017-01-27", author = Author.KASPAR_ETTER, comment = "Especially the finish method, which does nothing at the moment.")
public class BufferedInflaterInputStream extends InflaterInputStream {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream, @Nonnull Inflater inflater, int size) {
        super(new BufferedInputStream(inputStream), inflater, size);
    }
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream, @Nonnull Inflater inflater) {
        this(inputStream, inflater, 512);
    }
    
    public BufferedInflaterInputStream(@Nonnull InputStream inputStream) {
        this(inputStream, new Inflater());
    }
    
    /* -------------------------------------------------- Reading -------------------------------------------------- */
    
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
