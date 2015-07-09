package net.digitalid.core.client;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.wrappers.ClientSignatureWrapper;

/**
 * This class extends the {@link Commitment commitment} of a {@link Client client} with its {@link Client#getSecret() secret}.
 * 
 * @invariant getPublicKey().getAu().pow(getSecret()).equals(getValue()) : "The secret matches the commitment.";
 * 
 * @see ClientSignatureWrapper
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class SecretCommitment extends Commitment implements Blockable {
    
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
        
        if (!publicKey.getAu().pow(secret).equals(value)) throw new PacketException(PacketError.INTERNAL, "The secret does not match the commitment.");
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
