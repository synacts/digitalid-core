package net.digitalid.core.credential;

import java.math.BigInteger;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.credential.utility.ExposedExponent;
import net.digitalid.core.credential.utility.ExposedExponentConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.annotations.type.kind.RoleType;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.selfcontained.Selfcontained;

/**
 * This class abstracts from client and host credentials.
 * 
 * @invariant isIdentityBased() != isAttributeBased() : "This credential is either identity- or attribute-based.";
 * @invariant !isRoleBased() || isIdentityBased() : "If this credential is role-based, it also is identity-based.";
 * @invariant !isAttributeBased() || getRestrictions() == null : "If this credential is attribute-based, then the restrictions are null.";
 * @invariant !isRoleBased() || getPermissions() != null && getRestrictions() != null : "If this credential is role-based, both the permissions and the restrictions are not null.";
 * @invariant !isIdentityBased()|| getExposedExponent().getIssuer() instanceof InternalPerson : "If this credential is identity-based, then the issuer is an internal person.";
 * @invariant !isIdentityBased()|| getI() == null : "If this credential is identity-based, then the value i is null.";
 * 
 * @see ClientCredential
 * @see HostCredential
 */
@Immutable
public abstract class Credential extends RootClass {
    
    /* -------------------------------------------------- Exposed Exponent -------------------------------------------------- */
    
    /**
     * Returns the exposed exponent of this credential.
     */
    @Pure
    public abstract @Nonnull ExposedExponent getExposedExponent();
    
    /* -------------------------------------------------- Hash of Exponent -------------------------------------------------- */
    
    @Pure
    protected @Nonnull Exponent deriveO() {
        try {
            return ExponentBuilder.withValue(new BigInteger(1, XDF.hash(getExposedExponent(), ExposedExponentConverter.INSTANCE))).build();
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception); // TODO: How to handle or propagate such exceptions?
        }
    }
    
    /**
     * Returns the hash of the exposed exponent.
     */
    @Pure
    @Derive("deriveO()")
    public abstract @Nonnull Exponent getO();
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether this credential is still valid.
     */
    @Pure
    public boolean isValid() {
        return getExposedExponent().getIssuance().isGreaterThan(Time.TROPICAL_YEAR.ago());
    }
    
    /**
     * Returns whether this credential is still active.
     */
    @Pure
    public boolean isActive() {
        return getExposedExponent().getIssuance().isGreaterThan(Time.HOUR.ago());
    }
    
    /* -------------------------------------------------- Authentication Mode -------------------------------------------------- */
    
    /**
     * Returns whether this credential is used for role-based authentication.
     */
    @Pure
    public boolean isRoleBased() {
        return getExposedExponent().getRole() != null;
    }
    
    /**
     * Returns whether this credential is used for identity-based authentication.
     */
    @Pure
    public boolean isIdentityBased() {
        return getExposedExponent().getAttributeContent() == null;
    }
    
    /**
     * Returns whether this credential is used for attribute-based authentication.
     */
    @Pure
    public boolean isAttributeBased() {
        return getExposedExponent().getAttributeContent() != null;
    }
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of the client or null if they are not shown.
     */
    @Pure
    public @Nullable @Frozen ReadOnlyAgentPermissions getPermissions() {
        return getExposedExponent().getHashedOrSaltedPermissions().getPermissions();
    }
    
    /**
     * Returns the exposed permissions of the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public @Nonnull @Frozen ReadOnlyAgentPermissions getExposedPermissions() {
        Require.that(isRoleBased()).orThrow("This credential has to be role-based.");
        
        return getExposedExponent().getHashedOrSaltedPermissions().getExposedPermissions();
    }
    
    /* -------------------------------------------------- Role -------------------------------------------------- */
    
    /**
     * Returns the role that is assumed by the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public @Nonnull @RoleType SemanticType getProvidedRole() {
        Require.that(isRoleBased()).orThrow("This credential has to be role-based.");
        
        return getExposedExponent().getRole();
    }
    
    /* -------------------------------------------------- Attribute Content -------------------------------------------------- */
    
    /**
     * Returns the attribute content for attribute-based access control.
     * 
     * @require isAttributeBased() : "This credential is attribute-based.";
     */
    @Pure
    public @Nonnull Selfcontained getProvidedAttributeContent() {
        Require.that(isAttributeBased()).orThrow("This credential has to be attribute-based.");
        
        return getExposedExponent().getAttributeContent();
    }
    
    /* -------------------------------------------------- Restrictions -------------------------------------------------- */
    
    /**
     * Returns the restrictions of the client or null in case they are not exposed.
     */
    @Pure
    public abstract @Nullable Restrictions getRestrictions();
    
    /**
     * Returns the restrictions of the client.
     * 
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public @Nonnull Restrictions getProvidedRestrictions() {
        Require.that(isRoleBased()).orThrow("This credential has to be role-based.");
        
        return getRestrictions();
    }
    
    /* -------------------------------------------------- Serial Number -------------------------------------------------- */
    
    /**
     * Returns the serial number of this credential or null if it is not shown.
     */
    @Pure
    public abstract @Nullable Exponent getI();
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    /**
     * Returns whether this credential is similar to the given credential.
     * Credentials are similar to each other if they are a randomization of the same credential.
     */
    @Pure
    public boolean isSimilarTo(@Nonnull Credential credential) {
        return getExposedExponent().equals(credential.getExposedExponent()) && Objects.equals(getI(), credential.getI());
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that(isIdentityBased() != isAttributeBased()).orThrow("This credential has to be either identity- or attribute-based.");
        Validate.that(!isRoleBased() || isIdentityBased()).orThrow("If this credential is role-based, it also has to be identity-based.");
        Validate.that(!isAttributeBased() || getRestrictions() == null).orThrow("If this credential is attribute-based, then the restrictions have to be null.");
        Validate.that(!isRoleBased() || getPermissions() != null && getRestrictions() != null).orThrow("If this credential is role-based, both the permissions and the restrictions may not be null.");
        Validate.that(!isIdentityBased()|| getExposedExponent().getIssuer() instanceof InternalPerson).orThrow("If this credential is identity-based, then the issuer has to be an internal person.");
        Validate.that(!isIdentityBased()|| getI() == null).orThrow("If this credential is identity-based, then the value i has to be null.");
        super.validate();
    }
    
}
