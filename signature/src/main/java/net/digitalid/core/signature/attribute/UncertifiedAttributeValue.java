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
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;

/**
 * This class facilitates the encoding and decoding of uncertified attribute values.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class UncertifiedAttributeValue extends AttributeValue {
    
    // TODO: Check somewhere that:
    // @invariant signature.isNotSigned() : "The signature is not signed.";
    
    // TODO: The signature needs to be created somewhere:
//        this.signature = SignatureWrapper.encodeWithoutSigning(AttributeValue.TYPE, SelfcontainedWrapper.encodeNonNullable(AttributeValue.CONTENT, content), null);
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    @Pure
    @Override
    public void verify() {}
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull UncertifiedAttributeValue with(@Nonnull @Invariant(condition = "signature.getObject().getType().isAttributeType()", message = "The type of the packed value denotes an attribute.") Signature<Pack> signature) {
        return new UncertifiedAttributeValueSubclass(signature);
    }
    
}
