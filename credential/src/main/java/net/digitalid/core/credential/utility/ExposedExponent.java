package net.digitalid.core.credential.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.Frozen;
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
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.selfcontained.Selfcontained;

/**
 * This class models the exposed exponent of {@link Credential credentials}.
 * 
 * @invariant isIdentityBased() != isAttributeBased() : "This credential is either identity- or attribute-based.";
 * @invariant !isRoleBased() || isIdentityBased() : "If this credential is role-based, it is also identity-based";
 * @invariant !isAttributeBased() || getRestrictions() == null : "If this credential is attribute-based, the restrictions are null.";
 * @invariant !isRoleBased() || getPermissions() != null && getRestrictions() != null : "If this credential is role-based, both the permissions and the restrictions are not null.";
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class ExposedExponent extends RootClass {
    
    /* -------------------------------------------------- Issuer -------------------------------------------------- */
    
    /**
     * Returns the internal non-host identity that issued this credential.
     * 
     * @ensure !isIdentityBased() || issuer instanceof InternalPerson : "If this credential is identity-based, then the issuer is an internal person.";
     */
    @Pure
    public abstract @Nonnull InternalNonHostIdentity getIssuer();
    
    /* -------------------------------------------------- Issuance Time -------------------------------------------------- */
    
    /**
     * Returns the issuance time rounded down to the last half-hour.
     */
    @Pure
    public abstract @Nonnull @Positive @MultipleOf(1_800_000l) Time getIssuance();
    
    /**
     * Returns whether this credential is still valid.
     */
    @Pure
    public boolean isValid() {
        return getIssuance().isGreaterThan(Time.TROPICAL_YEAR.ago());
    }
    
    /**
     * Returns whether this credential is still active.
     */
    @Pure
    public boolean isActive() {
        return getIssuance().isGreaterThan(Time.HOUR.ago());
    }
    
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
     * Returns the client's salted permissions or simply its hash.
     */
    @Pure
    public abstract @Nonnull HashedOrSaltedAgentPermissions getHashedOrSaltedPermissions();
    
    /**
     * Returns the permissions of the client or null if they are not shown.
     */
    @Pure
    public @Nullable @Frozen ReadOnlyAgentPermissions getPermissions() {
        return getHashedOrSaltedPermissions().getPermissions(); // TODO: Wrong delegation.
    }
    
    /**
     * Returns the permissions of the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public final @Nonnull ReadOnlyAgentPermissions getPermissionsNotNull() {
        Require.that(isRoleBased()).orThrow("This credential is role-based.");
        
        final @Nullable ReadOnlyAgentPermissions permissions = getPermissions(); // TODO
        Require.that(permissions != null).orThrow("This follows from the class invariant.");
        return permissions;
    }
    
    /* -------------------------------------------------- Role -------------------------------------------------- */
    
    /**
     * Returns the role that is assumed by the client or null in case no role is assumed.
     * 
     * @ensure role == null || role.isRoleType() : "The role is either null or a role type.";
     */
    @Pure
    public abstract @Nullable SemanticType getRole();
    
    /**
     * Returns whether this credential is used for role-based authentication.
     */
    @Pure
    public final boolean isRoleBased() {
        return getRole() != null;
    }
    
    /**
     * Returns the role that is assumed by the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public final @Nonnull SemanticType getRoleNotNull() {
        Require.that(getRole() != null).orThrow("This credential is role-based.");
        
        return getRole();
    }
    
    /* -------------------------------------------------- Attribute Content -------------------------------------------------- */
    
    /**
     * Returns the attribute content for anonymous access control or null in case of identity-based authentication.
     */
    @Pure
    public abstract @Nullable Selfcontained getAttributeContent();
    
    /**
     * Returns whether this credential is used for attribute-based authentication.
     */
    @Pure
    public final boolean isAttributeBased() {
        return getAttributeContent() != null;
    }
    
    /**
     * Returns whether this credential is used for identity-based authentication.
     */
    @Pure
    public final boolean isIdentityBased() {
        return getAttributeContent() == null;
    }
    
    /**
     * Returns the attribute content for anonymous access control.
     * 
     * @require isAttributeBased() : "This credential is attribute-based.";
     */
    @Pure
    public final @Nonnull Selfcontained getAttributeContentNotNull() {
        Require.that(getAttributeContent() != null).orThrow("This credential is attribute-based.");
        
        return getAttributeContent();
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that(isIdentityBased() != isAttributeBased()).orThrow("This credential is either identity- or attribute-based.");
        Validate.that(!isRoleBased() || isIdentityBased()).orThrow("If this credential is role-based, it is also identity-based");
//        Validate.that(!isAttributeBased() || getRestrictions() == null).orThrow("If this credential is attribute-based, the restrictions are null.");
//        Validate.that(!isRoleBased() || getPermissions() != null && getRestrictions() != null).orThrow("If this credential is role-based, both the permissions and the restrictions are not null.");
        super.validate();
    }
    
}
