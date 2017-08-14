package net.digitalid.core.signature.host;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.parameters.Parameters;

/**
 *
 */
@Utility
public abstract class HostSignatureSigner {
    
    public interface SubjectHostSignatureSigner<OBJECT> {
    
        public @Nonnull HostSignatureSigner.SignerHostSignatureSigner<OBJECT> to(@Nonnull InternalIdentifier subject);
        
    }
    
    public interface SignerHostSignatureSigner<OBJECT> {
        
        public @Nonnull HostSignature<OBJECT> as(@Nonnull InternalIdentifier signer);
        
    }
    
    public static class InnerHostSignatureSigner<OBJECT> implements SubjectHostSignatureSigner<OBJECT>, SignerHostSignatureSigner<OBJECT> {
        
        private final @Nonnull OBJECT object;
        
        private final @Nonnull Converter<OBJECT, Void> objectConverter;
        
        InnerHostSignatureSigner(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
            this.object = object;
            this.objectConverter = objectConverter;
        }
        
        private @Nonnull InternalIdentifier subject;
        
        @Override
        public @Nonnull InnerHostSignatureSigner to(@Nonnull InternalIdentifier subject) {
            this.subject = subject;
            return this;
        }
        
        @Override
        public @Nonnull HostSignature<OBJECT> as(@Nonnull InternalIdentifier signer) {
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull PrivateKey privateKey;
            try {
                privateKey = PrivateKeyRetriever.retrieve(signer.getHostIdentifier(), time);
            } catch (@Nonnull RequestException exception) {
                throw UncheckedExceptionBuilder.withCause(exception).build();
            }
    
            final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
            final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
                encoder.startHashing(messageDigest);
                encoder.encodeObject(TimeConverter.INSTANCE, time);
                encoder.encodeObject(InternalIdentifierConverter.INSTANCE, subject);
                encoder.encodeObject(InternalIdentifierConverter.INSTANCE, signer);
                encoder.encodeObject(objectConverter, object);
                final @Nonnull BigInteger hash = new BigInteger(1, encoder.stopHashing());
                final @Nonnull BigInteger value = privateKey.powD(hash).getValue();
        
                Log.debugging("$ signed the hash $ about $.", signer, hash, subject);
    
                return HostSignatureBuilder.withObjectConverter(objectConverter).withObject(object).withSubject(subject).withSigner(signer).withSignatureValue(value).withTime(time).build();
            } catch (@Nonnull MemoryException exception) {
                throw UncheckedExceptionBuilder.withCause(exception).build();
            }
        }
        
    }
    
    @Pure
    public static <OBJECT> @Nonnull SubjectHostSignatureSigner<OBJECT> sign(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
        return new InnerHostSignatureSigner<>(object, objectConverter);
    }
    
}
