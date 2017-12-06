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
package net.digitalid.core.signature.attribute;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.exceptions.InactiveSignatureException;
import net.digitalid.core.signature.exceptions.InactiveSignatureExceptionBuilder;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class facilitates the encoding and decoding of certified attribute values.
 * 
 * @invariant getContent().getType().isAttributeFor(getSubject().getCategory()) : "The content is an attribute for the subject.";
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class CertifiedAttributeValue extends AttributeValue {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") HostSignature<Pack> getSignature();
    
    // TODO: Check somewhere that:
//        Require.that(content.getType().isAttributeFor(subject.getCategory())).orThrow("The content is an attribute for the subject.");
        
    // TODO: The signature needs to be created somewhere:
//     * @require Server.hasHost(issuer.getAddress().getHostIdentifier()) : "The host of the issuer is running on this server.";
//        this.signature = HostSignatureWrapper.sign(AttributeValue.TYPE, SelfcontainedWrapper.encodeNonNullable(AttributeValue.CONTENT, content), subject.getAddress(), null, issuer.getAddress());
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public void verify() throws ExternalException {
        // TODO:
//        getSignature().verify();
//        Certificate.isAuthorized(getIssuer(), getContent());
    }
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the certificate of this attribute value is valid at the given time.
     */
    @Pure
    public boolean isValid(@Nonnull Time time) {
        return getSignature().getTime().add(getSignature().getObject().getType().getCachingPeriod()).isGreaterThan(time);
    }
    
    /**
     * Checks that the certificate of this attribute value is valid at the given time.
     * 
     * @throws InactiveSignatureException if the certificate is not valid at the given time.
     */
    @Pure
    public void checkIsValid(@Nonnull Time time) throws InactiveSignatureException {
        if (!isValid(time)) { throw InactiveSignatureExceptionBuilder.withSignature(getSignature()).build(); }
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull CertifiedAttributeValue with(@Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") HostSignature<Pack> signature) {
        return new CertifiedAttributeValueSubclass(signature);
    }
    
}
