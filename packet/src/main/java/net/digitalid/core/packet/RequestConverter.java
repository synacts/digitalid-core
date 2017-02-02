package net.digitalid.core.packet;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;


/**
 * This class converts and recovers a {@link Request request}.
 */
@Immutable
public class RequestConverter extends PacketConverter<Request> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull RequestConverter INSTANCE = new RequestConverter();
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<Request> getType() {
        return Request.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "Request";
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull Request recover(@Nonnull Encryption<Signature<Compression<Pack>>> encryption) {
        return RequestBuilder.withEncryption(encryption).build();
    }
    
}
