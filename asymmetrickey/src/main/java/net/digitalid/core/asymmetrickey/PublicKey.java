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
package net.digitalid.core.asymmetrickey;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.generation.Provide;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.annotations.group.InGroup;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.GroupWithUnknownOrder;

/**
 * This class stores the groups, elements and exponents of a host's public key.
 * 
 * @invariant verifySubgroupProof() : "The elements au, ai, av and ao are in the subgroup of ab.";
 * 
 * @see PrivateKey
 * @see KeyPair
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PublicKey extends AsymmetricKey {
    
    /* -------------------------------------------------- Composite Group -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull GroupWithUnknownOrder getCompositeGroup();
    
    /**
     * Returns the encryption and verification exponent.
     */
    @Pure
    public abstract @Nonnull Exponent getE();
    
    /**
     * Returns the base for blinding.
     */
    @Pure
    @Provide("compositeGroup")
    public abstract @Nonnull @InGroup("compositeGroup") Element getAb();
    
    /**
     * Returns the base of the client's secret.
     */
    @Pure
    @Provide("compositeGroup")
    public abstract @Nonnull @InGroup("compositeGroup") Element getAu();
    
    /**
     * Returns the base of the serial number.
     */
    @Pure
    @Provide("compositeGroup")
    public abstract @Nonnull @InGroup("compositeGroup") Element getAi();
    
    /**
     * Returns the base of the hashed identifier.
     */
    @Pure
    @Provide("compositeGroup")
    public abstract @Nonnull @InGroup("compositeGroup") Element getAv();
    
    /**
     * Returns the base of the exposed arguments.
     */
    @Pure
    @Provide("compositeGroup")
    public abstract @Nonnull @InGroup("compositeGroup") Element getAo();
    
    /* -------------------------------------------------- Subgroup Proof -------------------------------------------------- */
    
    /**
     * Returns the hash of the temporary commitments in the subgroup proof.
     */
    @Pure
    public abstract @Nonnull Exponent getT();
    
    /**
     * Returns the solution for the proof that au is in the subgroup of ab.
     */
    @Pure
    public abstract @Nonnull Exponent getSu();
    
    /**
     * Returns the solution for the proof that ai is in the subgroup of ab.
     */
    @Pure
    public abstract @Nonnull Exponent getSi();
    
    /**
     * Returns the solution for the proof that av is in the subgroup of ab.
     */
    @Pure
    public abstract @Nonnull Exponent getSv();
    
    /**
     * Returns the solution for the proof that ao is in the subgroup of ab.
     */
    @Pure
    public abstract @Nonnull Exponent getSo();
    
    /**
     * Returns whether the proof that au, ai, av and ao are in the subgroup of ab is correct.
     */
    @Pure
    public boolean verifySubgroupProof() {
        final @Nonnull Element tu = getAb().pow(getSu()).multiply(getAu().pow(getT()));
        final @Nonnull Element ti = getAb().pow(getSi()).multiply(getAi().pow(getT()));
        final @Nonnull Element tv = getAb().pow(getSv()).multiply(getAv().pow(getT()));
        final @Nonnull Element to = getAb().pow(getSo()).multiply(getAo().pow(getT()));
        
        return getT().getValue().equals(HashGenerator.generateHash(tu, ti, tv, to));
    }
    
    /* -------------------------------------------------- Square Group -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull GroupWithUnknownOrder getSquareGroup();
    
    /**
     * Returns the generator of the square group.
     */
    @Pure
    @Provide("squareGroup")
    public abstract @Nonnull @InGroup("squareGroup") Element getG();
    
    /**
     * Returns the encryption element of the square group.
     */
    @Pure
    @Provide("squareGroup")
    public abstract @Nonnull @InGroup("squareGroup") Element getY();
    
    /**
     * Returns the encryption base of the square group.
     */
    @Pure
    @Provide("squareGroup")
    public abstract @Nonnull @InGroup("squareGroup") Element getZPlus1();
    
    /* -------------------------------------------------- Verifiable Encryption -------------------------------------------------- */
    
    /**
     * Returns the verifiable encryption of the given value m with the random value r.
     */
    @Pure
    @TODO(task = "Move this method to where it is used.", date = "2016-04-19", author = Author.KASPAR_ETTER, priority = Priority.LOW)
    public @Nonnull Pair<@Nonnull Element, @Nonnull Element> getVerifiableEncryption(@Nonnull Exponent m, @Nonnull Exponent r) {
        return Pair.of(getY().pow(r).multiply(getZPlus1().pow(m)), getG().pow(r));
    }
    
    /* -------------------------------------------------- Validate -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Require.that(verifySubgroupProof()).orThrow("The elements au, ai, av and ao have to be in the subgroup of ab.");
    }
    
}
