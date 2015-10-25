package net.digitalid.service.core.client;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.cryptography.Element;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.wrappers.ClientSignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class extends the {@link Commitment commitment} of a {@link Client client} with its {@link Client#getSecret() secret}.
 * 
 * @invariant getPublicKey().getAu().pow(getSecret()).equals(getValue()) : "The secret matches the commitment.";
 * 
 * @see ClientSignatureWrapper
 */
@Immutable
public final class SecretCommitment extends Commitment {
    
    /**
     * Stores the secret of this commitment.
     */
    private final @Nonnull Exponent secret;
    
    /**
     * Creates a new commitment with the given host, time, value, public key and secret.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     * @param publicKey the public key of this commitment.
     * @param secret the secret of this commitment.
     */
    SecretCommitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull Element value, @Nonnull PublicKey publicKey, @Nonnull Exponent secret) throws PacketException {
        super(host, time, value, publicKey);
        
        if (!publicKey.getAu().pow(secret).equals(value)) throw new PacketException(PacketErrorCode.INTERNAL, "The secret does not match the commitment.");
        this.secret = secret;
    }
    
    /**
     * Returns the secret of this commitment.
     * 
     * @return the secret of this commitment.
     */
    @Pure
    public @Nonnull Exponent getSecret() {
        return secret;
    }
    
}
