package net.digitalid.core.signature.host;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.math.Positive;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.Signature;
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
    @Override
    public abstract @Nonnull @Positive Time getTime();
    
    @Pure
    public void verifySignature(@Nonnull PublicKey publicKey, @Nonnull BigInteger value, @Nonnull BigInteger hash) throws InvalidHostSignatureException {
        final @Nonnull BigInteger computedHash = publicKey.getCompositeGroup().getElement(value).pow(publicKey.getE()).getValue();
        if (!computedHash.equals(hash)) {
            throw InvalidHostSignatureException.get(this);
        }
    }
    
}

