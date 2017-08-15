package net.digitalid.core.signature.client;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.parameters.Parameters;

/**
 * Creates a client signature by signing an object of generic type OBJECT.
 */
@Utility
public abstract class ClientSignatureCreator {
    
    public interface SubjectClientSignatureCreator<OBJECT> {
    
        /**
         * Addresses the signature to a certain subject.
         */
        public @Nonnull ClientSignatureCreator.SignerClientSignatureCreator<OBJECT> to(@Nonnull InternalIdentifier subject);
        
    }
    
    public interface SignerClientSignatureCreator<OBJECT> {
    
        /**
         * Signs the object with a specific secret commitment.
         */
        public @Nonnull ClientSignature<OBJECT> with(@Nonnull SecretCommitment commitment);
        
    }
    
    /**
     * Inner class for the host signature creator which structures the parameters required for signing.
     */
    public static class InnerClientSignatureCreator<OBJECT> implements SubjectClientSignatureCreator<OBJECT>, SignerClientSignatureCreator<OBJECT> {
        
        private final @Nonnull OBJECT object;
        
        private final @Nonnull Converter<OBJECT, Void> objectConverter;
    
        /**
         * Creates a new InnerHostSignatureSigner.
         */
        private InnerClientSignatureCreator(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
            this.object = object;
            this.objectConverter = objectConverter;
        }
        
        private @Nonnull InternalIdentifier subject;
    
        /**
         * {@inheritDoc}
         */
        @Override
        public @Nonnull ClientSignatureCreator.InnerClientSignatureCreator to(@Nonnull InternalIdentifier subject) {
            this.subject = subject;
            return this;
        }
    
        /**
         * {@inheritDoc}
         */
        @Override
        public @Nonnull ClientSignature<OBJECT> with(@Nonnull SecretCommitment commitment) {
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull BigInteger hash = ClientSignature.getContentHash(time, subject, object, objectConverter);

            final @Nonnull Exponent r = commitment.getPublicKey().getCompositeGroup().getRandomExponent(Parameters.RANDOM_EXPONENT.get());
            final @Nonnull BigInteger t = ClientSignature.getHash(commitment.getPublicKey().getAu().pow(r));

            final @Nonnull Exponent h = ExponentBuilder.withValue(t.xor(hash)).build();
            final @Nonnull Exponent s = r.subtract(commitment.getSecret().multiply(h));
            return ClientSignatureBuilder.withObjectConverter(objectConverter).withObject(object).withSubject(subject).withCommitment(commitment).withT(t).withS(s).withTime(time).build();
        }
        
    }
    
    /**
     * Initializes the signing of a given object with a host signature.
     */
    @Pure
    public static <OBJECT> @Nonnull SubjectClientSignatureCreator<OBJECT> sign(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
        return new InnerClientSignatureCreator<>(object, objectConverter);
    }
    
}
