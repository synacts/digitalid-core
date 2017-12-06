/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.signature.client;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
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
        @Impure
        public @Nonnull ClientSignatureCreator.SignerClientSignatureCreator<OBJECT> about(@Nonnull InternalIdentifier subject);
        
    }
    
    public interface SignerClientSignatureCreator<OBJECT> {
    
        /**
         * Signs the object with a specific secret commitment.
         */
        @Pure
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
        @Impure
        @Override
        public @Nonnull ClientSignatureCreator.InnerClientSignatureCreator about(@Nonnull InternalIdentifier subject) {
            this.subject = subject;
            return this;
        }
    
        /**
         * {@inheritDoc}
         */
        @Pure
        @Override
        public @Nonnull ClientSignature<OBJECT> with(@Nonnull SecretCommitment commitment) {
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull BigInteger hash = ClientSignature.getContentHash(time, subject, objectConverter, object);

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
