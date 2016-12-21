package net.digitalid.core.credential.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.credential.Credential;
import net.digitalid.core.identification.annotations.type.kind.RoleType;
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
    protected @Nonnull PublicKey derivePublicKey() {
        try {
            return PublicKeyRetriever.retrieve(getIssuer().getAddress().getHostIdentifier(), getIssuance());
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception); // TODO: How to handle or propagate such exceptions?
        }
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
    
}
