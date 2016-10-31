package net.digitalid.core.credential.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.selfcontained.Selfcontained;

/**
 * This class models the exposed exponent of {@link Credential credentials}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class ExposedExponent extends RootClass {
    
    /**
     * Returns the internal non-host identity that issued this credential.
     * 
     * @ensure !isIdentityBased() || issuer instanceof InternalPerson : "If this credential is identity-based, then the issuer is an internal person.";
     */
    @Pure
    public abstract @Nonnull InternalNonHostIdentity getIssuer();
    
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
    
    /**
     * Returns the public key of the host that issued this credential.
     */
    @Pure
    @Derive("net.digitalid.core.asymmetrickey.PublicKeyRetriever.retrieve(issuer.getAddress().getHostIdentifier(), issuance)")
    public abstract @Nonnull PublicKey getPublicKey();
    
    /**
     * Returns the client's salted permissions or simply its hash.
     */
    @Pure
    public abstract @Nonnull SaltedAgentPermissions getSaltedPermissions();
    
    /**
     * Returns the permissions of the client or null if they are not shown.
     */
    @Pure
    public @Nullable @Frozen ReadOnlyAgentPermissions getPermissions() {
        return getSaltedPermissions().getPermissions();
    }
    
    /**
     * Returns the permissions of the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public final @Nonnull ReadOnlyAgentPermissions getPermissionsNotNull() {
        Require.that(isRoleBased()).orThrow("This credential is role-based.");
        
        final @Nullable ReadOnlyAgentPermissions permissions = randomizedPermissions.getPermissions();
        Require.that(permissions != null).orThrow("This follows from the class invariant.");
        return permissions;
    }
    
    /**
     * Returns the role that is assumed by the client or null in case no role is assumed.
     * 
     * @ensure role == null || role.isRoleType() : "The role is either null or a role type.";
     */
    @Pure
    public final @Nullable SemanticType getRole() {
        return role;
    }
    
    /**
     * Returns the role that is assumed by the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public final @Nonnull SemanticType getRoleNotNull() {
        Require.that(role != null).orThrow("This credential is role-based.");
        
        return role;
    }
    
    /**
     * Returns the attribute content for anonymous access control or null in case of identity-based authentication.
     */
    @Pure
    public abstract @Nullable Selfcontained getAttributeContent();
    
    /**
     * Returns the attribute content for anonymous access control.
     * 
     * @require isAttributeBased() : "This credential is attribute-based.";
     */
    @Pure
    public final @Nonnull Block getAttributeContentNotNull() {
        Require.that(attributeContent != null).orThrow("This credential is attribute-based.");
        
        return attributeContent;
    }
    
    /**
     * Returns whether this credential is used for attribute-based authentication.
     */
    @Pure
    public final boolean isAttributeBased() {
        return attributeContent != null;
    }
    
    /**
     * Returns whether this credential is used for identity-based authentication.
     */
    @Pure
    public final boolean isIdentityBased() {
        return attributeContent == null;
    }
    
    /**
     * Returns whether this credential is used for role-based authentication.
     */
    @Pure
    public final boolean isRoleBased() {
        return role != null;
    }
    
    /**
     * Returns the restrictions of the client or null in case they are not shown.
     */
    @Pure
    public abstract @Nullable Restrictions getRestrictions();
    
    /**
     * Returns the restrictions of the client.
     * 
     * @require getRestrictions() != null : "The restrictions are not null.";
     */
    @Pure
    public final @Nonnull Restrictions getRestrictionsNotNull() {
        Require.that(restrictions != null).orThrow("The restrictions are not null.");
        
        return restrictions;
    }
    
}
