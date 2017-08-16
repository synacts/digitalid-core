package net.digitalid.core.signature;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class signs the wrapped object for encoding.
 * 
 * @see HostSignature
 * @see ClientSignature
 * TODO: CredentialsSignature
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class Signature<@Unspecifiable OBJECT> extends RootClass {
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    /**
     * Returns the wrapped object that has been or will be signed.
     */
    @Pure
    public abstract @Nonnull OBJECT getObject();
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * Returns the time at which the object has been or will be signed.
     */
    @Pure
    @Default("net.digitalid.utility.time.TimeBuilder.build()")
    public abstract @Nonnull @Positive Time getTime();
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Returns the subject about which a statement is made.
     */
    @Pure
    public abstract @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Returns whether this signature has been verified.
     */
    @Pure
    public boolean isVerified() {
        // TODO.
        return true;
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    /**
     * Creates a content hash with a given time, subject, signer, object converter and object.
     */
    @Pure
    public static <OBJECT> @Nonnull BigInteger getContentHash(@Nonnull Time time, @Nonnull InternalIdentifier subject, @Nonnull Converter<OBJECT, Void> objectConverter, @Nonnull OBJECT object) {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
            final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
                encoder.startHashing(messageDigest);
                encoder.encodeObject(TimeConverter.INSTANCE, time);
                encoder.encodeObject(InternalIdentifierConverter.INSTANCE, subject);
                encoder.encodeObject(objectConverter, object);
                return new BigInteger(1, encoder.stopHashing());
            } catch (@Nonnull MemoryException exception) {
                throw UncheckedExceptionBuilder.withCause(exception).build();
            }
    }
    
}
