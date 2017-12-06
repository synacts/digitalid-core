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
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.Packable;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.host.HostSignature;

/**
 * This class facilitates the encoding and decoding of attribute values.
 * 
 * @see CertifiedAttributeValue
 * @see UncertifiedAttributeValue
 */
@Immutable
@GenerateConverter
public abstract class AttributeValue extends RootClass implements Packable {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Returns the signature with the packed attribute value.
     */
    @Pure
    public abstract @Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") Signature<Pack> getSignature();
    
    /**
     * Returns whether this attribute value is certified.
     */
    @Pure
    public boolean isCertified() {
        return this instanceof CertifiedAttributeValue;
    }
    
    /* -------------------------------------------------- Content -------------------------------------------------- */
    
    /**
     * Returns the packed content of this attribute value.
     */
    @Pure
    public @Nonnull Pack getContent() {
        return getSignature().getObject();
    }
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Verifies the signature of this attribute value.
     * 
     * @throws InvalidSignatureException if the signature is not valid.
     * 
     * @require !isVerified() : "The signature of this attribute value has not been verified.";
     * 
     * @ensure isVerified() : "The signature of this attribute value has been verified.";
     */
    @Pure
    @NonCommitting
    public abstract void verify() throws ExternalException;
    
    /**
     * Returns whether the signature of this attribute value has been verified.
     */
    @Pure
    public boolean isVerified() {
        return getSignature().isVerified();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull AttributeValue with(@Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") Signature<Pack> signature) {
        if (signature instanceof HostSignature<?>) { return new CertifiedAttributeValueSubclass((HostSignature<Pack>) signature); }
        else { return new UncertifiedAttributeValueSubclass(signature); }
    }
    
}
