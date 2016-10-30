package net.digitalid.core.signature;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.exceptions.InvalidHostSignatureException;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class HostSignature<T> extends Signature<T> {
    
    @Pure
    public abstract @Nonnull InternalIdentifier getSigner();
    
    @Pure
    public void verifySignature(@Nonnull PublicKey publicKey, @Nonnull BigInteger value, @Nonnull BigInteger hash) throws InvalidHostSignatureException {
        if (!publicKey.getCompositeGroup().getElement(value).pow(publicKey.getE()).getValue().equals(hash)) {
            throw InvalidHostSignatureException.get(this);
        }
    }
    
}

