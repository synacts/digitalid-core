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
package net.digitalid.core.signature.host;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identifier.InternalIdentifier;

/**
 * Creates a host signature by signing an object of generic type OBJECT.
 */
@Utility
public abstract class HostSignatureCreator {
    
    public interface SubjectHostSignatureCreator<OBJECT> {
    
        /**
         * Addresses the signature to a certain subject.
         */
        public @Nonnull HostSignatureCreator.SignerHostSignatureCreator<OBJECT> about(@Nonnull InternalIdentifier subject);
        
    }
    
    public interface SignerHostSignatureCreator<OBJECT> {
    
        /**
         * Signs the object as a specific signer.
         */
        public @Nonnull HostSignature<OBJECT> as(@Nonnull InternalIdentifier signer);
        
    }
    
    /**
     * Inner class for the host signature creator which structures the parameters required for signing.
     */
    public static class InnerHostSignatureCreator<OBJECT> implements SubjectHostSignatureCreator<OBJECT>, SignerHostSignatureCreator<OBJECT> {
        
        private final @Nonnull OBJECT object;
        
        private final @Nonnull Converter<OBJECT, Void> objectConverter;
    
        /**
         * Creates a new InnerHostSignatureSigner.
         */
        private InnerHostSignatureCreator(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
            this.object = object;
            this.objectConverter = objectConverter;
        }
        
        private @Nonnull InternalIdentifier subject;
    
        @Override
        public @Nonnull HostSignatureCreator.InnerHostSignatureCreator about(@Nonnull InternalIdentifier subject) {
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
    
            final @Nonnull BigInteger hash = HostSignature.getContentHash(time, subject, objectConverter, object);
            final @Nonnull BigInteger value = privateKey.powD(hash).getValue();
    
            Log.debugging("$ signed the hash $ about $.", signer, hash, subject);

            return HostSignatureBuilder.withObjectConverter(objectConverter).withObject(object).withSubject(subject).withSigner(signer).withSignatureValue(value).withTime(time).build();
        }
        
    }
    
    /**
     * Initializes the signing of a given object with a host signature.
     */
    @Pure
    public static <OBJECT> @Nonnull SubjectHostSignatureCreator<OBJECT> sign(@Nonnull OBJECT object, @Nonnull Converter<OBJECT, Void> objectConverter) {
        return new InnerHostSignatureCreator<>(object, objectConverter);
    }
    
}
