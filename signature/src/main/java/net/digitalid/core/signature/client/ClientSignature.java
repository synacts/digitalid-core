package net.digitalid.core.signature.client;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.ExpiredSignatureException;
import net.digitalid.core.signature.exceptions.ExpiredSignatureExceptionBuilder;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureExceptionBuilder;

/**
 * This class signs the wrapped object as a client.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class ClientSignature<@Unspecifiable OBJECT> extends Signature<OBJECT> {
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns the commitment of this client signature.
     */
    @Pure
    public abstract @Nonnull Commitment getCommitment();
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    @Pure
    public static @Nonnull BigInteger getHash(@Nonnull Element value) {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
        messageDigest.update(value.getValue().toByteArray());
        return new BigInteger(1, messageDigest.digest());
    }
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    public void verifySignature(@Nonnull BigInteger t, @Nonnull Exponent s, @Nonnull BigInteger hash) throws InvalidSignatureException, ExpiredSignatureException {
        if (getTime().isLessThan(Time.TROPICAL_YEAR.ago())) {
            throw ExpiredSignatureExceptionBuilder.withSignature(this).build();
        }
        
        final @Nonnull BigInteger h = t.xor(hash);
        final @Nonnull Element value = getCommitment().getPublicKey().getAu().pow(s).multiply(getCommitment().getElement().pow(h));
        
        // TODO: if (!t.equals(getHash(value)) || s.getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
        if (!t.equals(getHash(value))) { 
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
}
