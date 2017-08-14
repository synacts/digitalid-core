package net.digitalid.core.signature.host;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureExceptionBuilder;

/**
 * This class signs the wrapped object as a host.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
@TODO(task = "I think the signing should not be part of the conversion. Otherwise, a signature (like a certificate) cannot be stored. Another solution might be to wrap signatures in packs so that their byte encoding can be accessed (or implement this here directly).", date = "2017-01-30", author = Author.KASPAR_ETTER)
public abstract class HostSignature<@Unspecifiable OBJECT> extends Signature<OBJECT> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * The converter for the generic object.
     */
    private final @Nonnull Converter<OBJECT, Void> objectConverter;
    
    /**
     * Constructs a host signature instance with the given object converter.
     */
    protected HostSignature(@Nonnull Converter<OBJECT, Void> objectConverter) {
        this.objectConverter = objectConverter;
    }
    
    /* -------------------------------------------------- Signer -------------------------------------------------- */
    
    /**
     * Returns the signer of this host signature.
     */
    @Pure
    public abstract @Nonnull InternalIdentifier getSigner();
    
    /* -------------------------------------------------- Signature Value -------------------------------------------------- */
    
    /**
     * Returns the signature value of the host signature, that is:
     * hash(time, subject, signer, object) ^ d % n;
     */
    @Pure
    public abstract @Nonnull BigInteger getSignatureValue();
    
    /**
     * Returns the hash of the host signature object.
     */
    @Pure
    public @Nonnull BigInteger getHash() {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
            encoder.startHashing(messageDigest);
            encoder.encodeObject(TimeConverter.INSTANCE, getTime());
            encoder.encodeObject(InternalIdentifierConverter.INSTANCE, getSubject());
            encoder.encodeObject(InternalIdentifierConverter.INSTANCE, getSigner());
            encoder.encodeObject(objectConverter, getObject());
            return new BigInteger(1, encoder.stopHashing());
        } catch (@Nonnull MemoryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Verifies the correctness of the host signature by using the given public key.
     * @throws InvalidSignatureException if the signature is not valid.
     */
    @Pure
    public void verifySignature(@Nonnull PublicKey publicKey) throws InvalidSignatureException {
        final @Nonnull BigInteger computedHash = publicKey.getCompositeGroup().getElement(getSignatureValue()).pow(publicKey.getE()).getValue();
        if (!computedHash.equals(getHash())) {
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
}

