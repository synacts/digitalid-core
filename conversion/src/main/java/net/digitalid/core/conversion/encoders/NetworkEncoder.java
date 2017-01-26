package net.digitalid.core.conversion.encoders;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.NetworkException;
import net.digitalid.core.conversion.exceptions.NetworkExceptionBuilder;

/**
 * A network encoder encodes values as XDF to a socket.
 */
@Mutable
@GenerateSubclass
public abstract class NetworkEncoder extends XDFEncoder<NetworkException> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull NetworkException createException(@Nonnull IOException exception) {
        return NetworkExceptionBuilder.withCause(exception).build();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected NetworkEncoder(@Nonnull OutputStream outputStream) {
        super(outputStream);
    }
    
    /**
     * Returns an encoder for the given socket.
     */
    @Pure
    public static @Nonnull NetworkEncoder of(@Nonnull Socket socket) throws NetworkException {
        try {
            return new NetworkEncoderSubclass(socket.getOutputStream());
        } catch (@Nonnull IOException exception) {
            throw NetworkExceptionBuilder.withCause(exception).build();
        }
    }
    
}
