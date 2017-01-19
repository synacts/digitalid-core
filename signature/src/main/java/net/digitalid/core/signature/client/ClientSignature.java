package net.digitalid.core.signature.client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.UncheckedException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.ExpiredClientSignatureException;
import net.digitalid.core.signature.exceptions.InvalidClientSignatureException;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class ClientSignature<T> extends Signature<T> {
    
    @Pure
    public abstract @Nonnull SecretCommitment getSecretCommitment();
    
    @Pure
    public @Nonnull BigInteger getHash(@Nonnull Element value) {
        try {
            final @Nonnull MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(value.getValue().toByteArray());
            return new BigInteger(1, messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw UncheckedException.with(e);
        }
    }
    
    @Pure
    public void verifySignature(@Nonnull BigInteger t, @Nonnull Exponent s, @Nonnull BigInteger hash) throws InvalidClientSignatureException, ExpiredClientSignatureException {
        if (getTime().isLessThan(Time.TROPICAL_YEAR.ago())) {
            throw ExpiredClientSignatureException.get(this);
        }
    
        final @Nonnull BigInteger h = t.xor(hash);
        final @Nonnull Element value = getSecretCommitment().getPublicKey().getAu().pow(s).multiply(getSecretCommitment().getElement().pow(h));
        
        // TODO: if (!t.equals(getHash(value)) || s.getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
        if (!t.equals(getHash(value))) { 
            throw InvalidClientSignatureException.get(this);
        }
    }
    
}
