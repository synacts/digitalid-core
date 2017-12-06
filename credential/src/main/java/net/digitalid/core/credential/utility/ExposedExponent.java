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
package net.digitalid.core.credential.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.credential.Credential;
import net.digitalid.core.identification.annotations.RoleType;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;

/**
 * This class models the exposed exponent of {@link Credential credentials}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class ExposedExponent extends RootClass {
    
    /* -------------------------------------------------- Issuer -------------------------------------------------- */
    
    /**
     * Returns the internal non-host identity that issued this credential.
     */
    @Pure
    public abstract @Nonnull InternalNonHostIdentity getIssuer();
    
    /* -------------------------------------------------- Issuance Time -------------------------------------------------- */
    
    /**
     * Returns the issuance time rounded down to the last half-hour.
     */
    @Pure
    public abstract @Nonnull @Positive @MultipleOf(1_800_000l) Time getIssuance();
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */
    
    @Pure
    protected @Nonnull PublicKey derivePublicKey() throws ExternalException {
        return PublicKeyRetriever.retrieve(getIssuer().getAddress().getHostIdentifier(), getIssuance());
    }
    
    /**
     * Returns the public key of the host that issued this credential.
     */
    @Pure
    @Derive("derivePublicKey()")
    public abstract @Nonnull PublicKey getPublicKey();
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the client's salted permissions or just its hash.
     */
    @Pure
    public abstract @Nonnull HashedOrSaltedAgentPermissions getHashedOrSaltedPermissions();
    
    /* -------------------------------------------------- Role -------------------------------------------------- */
    
    /**
     * Returns the role that is assumed by the client or null in case no role is assumed.
     */
    @Pure
    public abstract @Nullable @RoleType SemanticType getRole();
    
    /* -------------------------------------------------- Attribute Content -------------------------------------------------- */
    
    /**
     * Returns the attribute content for attribute-based access control or null in case of identity-based authentication.
     */
    @Pure
    public abstract @Nullable Pack getAttributeContent();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    @TODO(task = "Remove as soon as derive statements can indicate exceptions.", date = "2017-01-29", author = Author.KASPAR_ETTER)
    protected ExposedExponent() throws ExternalException {}
    
}
