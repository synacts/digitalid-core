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
package net.digitalid.core.commitment;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.group.Exponent;

/**
 * This class extends the {@link Commitment commitment} of a client with its secret.
 * 
 * @invariant getPublicKey().getAu().pow(getSecret()).equals(getElement()) : "The secret has to match the commitment.";
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public interface SecretCommitment extends Commitment {
    
    /* -------------------------------------------------- Secret -------------------------------------------------- */
    
    /**
     * Returns the secret of this commitment.
     */
    @Pure
    public @Nonnull Exponent getSecret();
    
    @Pure
    @Override
    @OrderOfAssignment(1)
    @Derive("getPublicKey().getAu().pow(getSecret()).getValue()")
    public @Nonnull BigInteger getValue();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public default void validate() {
        Commitment.super.validate();
        Validate.that(getPublicKey().getAu().pow(getSecret()).equals(getElement())).orThrow("The secret has to match the commitment.");
    }
    
}
