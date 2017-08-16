package net.digitalid.core.signature.client;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
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
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * The object converter is used to calculate the client signature content hash.
     */
    private final @Nonnull Converter<OBJECT, Void> objectConverter;
    
    /**
     * Constructs a host signature instance with the given object converter. The object converter
     * is used to calculate the client signature content hash.
     */
    protected ClientSignature(@Nonnull Converter<OBJECT, Void> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns the commitment of this client signature.
     */
    @Pure
    public abstract @Nonnull Commitment getCommitment();
    
    /**
     * Returns the hash of the temporary value computed as t = h(au^r).
     */
    @Pure
    public abstract @Nonnull BigInteger getT();
    
    /**
     * Returns the solution to the challenge, s = r - (t xor h(content)) * u
     */
    @Pure
    public abstract @Nonnull Exponent getS();
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    /**
     * Calculates the hash of the client signature content.
     */
    @Pure
    protected @Nullable BigInteger deriveClientSignatureContentHash() {
        return Signature.getContentHash(getTime(), getSubject(), objectConverter, getObject());
    }
    
    /**
     * The client signature content hash, which is set if the client signature is recovered.
     */
    @Pure
    @Derive("deriveClientSignatureContentHash()")
    protected abstract @Nonnull BigInteger getClientSignatureContentHash();
    
    /**
     * Returns the hash of a specific element.
     */
    @Pure
    public static @Nonnull BigInteger getHash(@Nonnull Element value) {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
        messageDigest.update(value.getValue().toByteArray());
        return new BigInteger(1, messageDigest.digest());
    }
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Verifies the client signature by checking whether the received t = h(au^s * f^(t xor h(content))).
     */
    @Pure
    public void verifySignature() throws InvalidSignatureException, ExpiredSignatureException {
        if (getTime().isLessThan(Time.TROPICAL_YEAR.ago())) {
            throw ExpiredSignatureExceptionBuilder.withSignature(this).build();
        }
        
        final @Nonnull BigInteger h = getT().xor(getClientSignatureContentHash());
        final @Nonnull Element value = getCommitment().getPublicKey().getAu().pow(getS()).multiply(getCommitment().getElement().pow(h));
        
        // TODO: if (!t.equals(getHash(value)) || s.getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
        if (!getT().equals(getHash(value))) { 
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
}
