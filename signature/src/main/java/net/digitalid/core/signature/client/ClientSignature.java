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
import java.security.MessageDigest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.exceptions.ExpiredSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.exceptions.InvalidSignatureExceptionBuilder;

/**
 * This class signs the wrapped object as a client.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class ClientSignature<@Unspecifiable OBJECT> extends Signature<OBJECT> {
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns the commitment of this client signature.
     */
    @Pure
    public abstract @Nonnull Commitment getCommitment();
    
    /**
     * Returns the hash of the temporary value computed as t = h(au^r).
     */
    @Pure
    public abstract @Nonnull BigInteger getT();
    
    /**
     * Returns the solution to the challenge, s = r - (t xor h(content)) * u
     */
    @Pure
    public abstract @Nonnull Exponent getS();
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    /**
     * Calculates the hash of the client signature content.
     */
    @Pure
    protected @Nullable BigInteger deriveClientSignatureContentHash() {
        return Signature.getContentHash(getTime(), getSubject(), getObjectConverter(), getObject());
    }
    
    /**
     * The client signature content hash, which is set if the client signature is recovered.
     */
    @Pure
    @Derive("deriveClientSignatureContentHash()")
    protected abstract @Nonnull BigInteger getClientSignatureContentHash();
    
    /**
     * Returns the hash of a specific element.
     */
    @Pure
    public static @Nonnull BigInteger getHash(@Nonnull Element value) {
        final @Nonnull MessageDigest messageDigest = Parameters.HASH_FUNCTION.get().produce();
        messageDigest.update(value.getValue().toByteArray());
        return new BigInteger(1, messageDigest.digest());
    }
    
    /* -------------------------------------------------- Verification -------------------------------------------------- */
    
    /**
     * Verifies the client signature by checking whether the received t = h(au^s * f^(t xor h(content))).
     */
    @Pure
    @Override
    public void verifySignature() throws InvalidSignatureException, ExpiredSignatureException {
        checkExpiration();
        
        final @Nonnull BigInteger h = getT().xor(getClientSignatureContentHash());
        final @Nonnull Element value = getCommitment().getPublicKey().getAu().pow(getS()).multiply(getCommitment().getElement().pow(h));
        
        // TODO: if (!t.equals(getHash(value)) || s.getBitLength() > Parameters.RANDOM_EXPONENT.get()) {
        if (!getT().equals(getHash(value))) { 
            throw InvalidSignatureExceptionBuilder.withSignature(this).build();
        }
    }
    
}
