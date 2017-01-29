package net.digitalid.core.signature.host;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureExceptionBuilder;

/**
 * This class signs the wrapped object as a host.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class HostSignature<@Unspecifiable OBJECT> extends Signature<OBJECT> {
    
    /* -------------------------------------------------- Signer -------------------------------------------------- */
    
    /**
     * Returns the signer of this host signature.
     */
    @Pure
    public abstract @Nonnull InternalIdentifier getSigner();
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    public void verifySignature(@Nonnull PublicKey publicKey, @Nonnull BigInteger value, @Nonnull BigInteger hash) throws InvalidSignatureException {
        final @Nonnull BigInteger computedHash = publicKey.getCompositeGroup().getElement(value).pow(publicKey.getE()).getValue();
        if (!computedHash.equals(hash)) {
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
}

