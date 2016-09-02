package net.digitalid.core.cryptography.signature;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.cryptography.Parameters;
import net.digitalid.utility.exceptions.UnexpectedFailureException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.math.Element;
import net.digitalid.utility.math.Exponent;
import net.digitalid.utility.time.Time;

import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.cryptography.signature.exceptions.ExpiredClientSignatureException;
import net.digitalid.core.cryptography.signature.exceptions.InvalidClientSignatureException;

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
            throw UnexpectedFailureException.with(e);
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
