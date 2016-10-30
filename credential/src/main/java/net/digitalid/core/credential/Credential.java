package net.digitalid.core.credential;

import java.math.BigInteger;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.agent.RandomizedAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.binary.Binary256Wrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.identification.identifier.IdentifierImplementation;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.NonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

/**
 * This class abstracts from client and host credentials.
 * 
 * @invariant isIdentityBased() != isAttributeBased() : "This credential is either identity- or attribute-based.";
 * @invariant !isRoleBased() || isIdentityBased() : "If this credential is role-based, it is also identity-based";
 * @invariant !isAttributeBased() || getRestrictions() == null : "If this credential is attribute-based, the restrictions are null.";
 * @invariant !isRoleBased() || getPermissions() != null && getRestrictions() != null : "If this credential is role-based, both the permissions and the restrictions are not null.";
 * 
 * @see ClientCredential
 * @see HostCredential
 */
@Immutable
public abstract class Credential {
    
    /**
     * Stores the semantic type {@code issuer.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ISSUER = SemanticType.map("issuer.exposed.credential@core.digitalid.net").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code issuance.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ISSUANCE = SemanticType.map("issuance.exposed.credential@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code hash.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType HASH = SemanticType.map("hash.exposed.credential@core.digitalid.net").load(RandomizedAgentPermissions.HASH);
    
    /**
     * Stores the semantic type {@code role.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ROLE = SemanticType.map("role.exposed.credential@core.digitalid.net").load(SemanticType.ROLE_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code exposed.credential@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType EXPOSED = SemanticType.map("exposed.credential@core.digitalid.net").load(TupleWrapper.XDF_TYPE, ISSUER, ISSUANCE, HASH, ROLE, AttributeValue.CONTENT);
    
    /**
     * Returns the block containing the exposed arguments of a credential.
     * 
     * @param issuer the internal non-host identity that issues the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions or its hash.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attributeContent the attribute content for attribute-based access control.
     * 
     * @return the block containing the exposed arguments of a credential.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
     * 
     * @ensure return.getType().equals(EXPOSED) : "The returned block has the indicated type.";
     */
    @Pure
    public static @Nonnull Block getExposed(@Nonnull InternalNonHostIdentity issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nullable Block attributeContent) {
        Require.that(issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR)).orThrow("The issuance time is positive and a multiple of half an hour.");
        Require.that(role == null || role.isRoleType()).orThrow("The role is either null or a role type.");
        
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(5);
        elements.set(0, issuer.toBlock(ISSUER));
        elements.set(1, issuance.toBlock().setType(ISSUANCE));
        elements.set(2, Binary256Wrapper.encodeNonNullable(HASH, randomizedPermissions.getHash()));
        elements.set(3, Block.toBlock(ROLE, role));
        elements.set(4, SelfcontainedWrapper.toBlock(AttributeValue.CONTENT, attributeContent));
        return TupleWrapper.encode(EXPOSED, elements.freeze());
    }
    
    
    /**
     * Asserts that the class invariant still holds.
     */
    @Pure
    private boolean invariant() {
        Require.that(isIdentityBased() != isAttributeBased()).orThrow("This credential is either identity- or attribute-based.");
        Require.that(!isRoleBased() || isIdentityBased()).orThrow("If this credential is role-based, it is also identity-based");
        Require.that(!isAttributeBased() || getRestrictions() == null).orThrow("If this credential is attribute-based, the restrictions are null.");
        Require.that(!isRoleBased() || getPermissions() != null && getRestrictions() != null).orThrow("If this credential is role-based, both the permissions and the restrictions are not null.");
        return true;
    }
    
    
    /**
     * Stores the public key of the host that issued this credential.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Stores the internal non-host identity that issued this credential.
     * 
     * @invariant !isIdentityBased() || issuer instanceof InternalPerson : "If this credential is identity-based, then the issuer is an internal person.";
     */
    private final @Nonnull InternalNonHostIdentity issuer;
    
    /**
     * Stores the issuance time rounded down to the last half-hour.
     * 
     * @invariant issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     */
    private final @Nonnull Time issuance;
    
    /**
     * Stores the client's randomized permissions or its randomized hash.
     */
    private final @Nonnull RandomizedAgentPermissions randomizedPermissions;
    
    /**
     * Stores the role that is assumed by the client or null in case no role is assumed.
     * 
     * @invariant role == null || role.isRoleType() : "The role is either null or a role type.";
     */
    private final @Nullable SemanticType role;
    
    /**
     * Stores the attribute content for attribute-based access control or null in case of identity-based authentication.
     */
    private final @Nullable Block attributeContent;
    
    /**
     * Stores the restrictions of the client or null in case of attribute-based authentication.
     */
    private final @Nullable Restrictions restrictions;
    
    
    /**
     * Stores the exposed block of this credential.
     * 
     * @invariant exposed.getType().isBasedOn(EXPOSED) : "The block is based on the indicated type.";
     */
    private final @Nonnull Block exposed;
    
    /**
     * Stores the hash of the exposed block.
     */
    private final @Nonnull Exponent o;
    
    
    /**
     * Stores the serial number of this credential or null if it is not shown.
     */
    private final @Nullable Exponent i;
    
    
    /**
     * Creates a new credential with the given public key, issuer, issuance time, permissions, attribute content, restrictions and argument for clients.
     * 
     * @param publicKey the public key of the host that issued the credential.
     * @param issuer the internal non-host identity that issued the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions or its hash.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attributeContent the attribute content for anonymous access control.
     * @param restrictions the restrictions of the client.
     * @param i the serial number of the credential.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
     * @require role == null || restrictions != null : "If a role is given, the restrictions are not null.";
     * @require attributeContent != null || issuer instanceof InternalPerson : "If the attribute content is null, the issuer is an internal person.";
     * @require (attributeContent == null) != (restrictions == null) : "Either the attribute content or the restrictions are null (but not both).";
     */
    Credential(@Nonnull PublicKey publicKey, @Nonnull InternalNonHostIdentity issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nullable Block attributeContent, @Nullable Restrictions restrictions, @Nonnull Exponent i) {
        Require.that(issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR)).orThrow("The issuance time is positive and a multiple of half an hour.");
        Require.that(randomizedPermissions.areShown()).orThrow("The randomized permissions are shown for client credentials.");
        Require.that(role == null || role.isRoleType()).orThrow("The role is either null or a role type.");
        Require.that(role == null || restrictions != null).orThrow("If a role is given, the restrictions are not null.");
        Require.that(attributeContent != null || issuer instanceof InternalPerson).orThrow("If the attribute content is null, the issuer is an internal person.");
        Require.that((attributeContent == null) != (restrictions == null)).orThrow("Either the attribute content or the restrictions are null (but not both).");
        
        this.publicKey = publicKey;
        this.issuer = issuer;
        this.issuance = issuance;
        this.randomizedPermissions = randomizedPermissions;
        this.role = role;
        this.attributeContent = attributeContent;
        this.restrictions = restrictions;
        
        this.exposed = getExposed(issuer, issuance, randomizedPermissions, role, attributeContent);
        this.o = Exponent.get(this.exposed.getHash());
        this.i = i;
        
        assert invariant();
    }
    
    /**
     * Creates a new credential from the given blocks for hosts.
     * 
     * @param exposed the block containing the exposed arguments of the credential.
     * @param randomizedPermissions the block containing the client's randomized permissions.
     * @param restrictions the client's restrictions or null if they are not shown.
     * @param i the block containing the credential's serial number.
     * 
     * @require exposed.getType().isBasedOn(Credential.EXPOSED) : "The exposed block is based on the indicated type.";
     * @require randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE) : "The randomized permissions are either null or based on the indicated type.";
     * @require i == null || i.getType().isBasedOn(Exponent.TYPE) : "The serial number is either null or based on the indicated type.";
     */
    @NonCommitting
    Credential(@Nonnull Block exposed, @Nullable Block randomizedPermissions, @Nullable Restrictions restrictions, @Nullable Block i) throws ExternalException {
        Require.that(exposed.getType().isBasedOn(Credential.EXPOSED)).orThrow("The exposed block is based on the indicated type.");
        Require.that(randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE)).orThrow("The randomized permissions are either null or based on the indicated type.");
        Require.that(i == null || i.getType().isBasedOn(Exponent.TYPE)).orThrow("The serial number is either null or based on the indicated type.");
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(exposed);
        this.issuer = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(0)).getIdentity().castTo(InternalNonHostIdentity.class);
        this.issuance = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(1));
        if (!issuance.isPositive() || !issuance.isMultipleOf(Time.HALF_HOUR)) { throw InvalidParameterValueException.get("issuance time", issuance); }
        this.publicKey = Cache.getPublicKey(issuer.getAddress().getHostIdentifier(), issuance);
        final @Nonnull BigInteger hash = Binary256Wrapper.decodeNonNullable(tuple.getNonNullableElement(2));
        if (randomizedPermissions != null) {
            this.randomizedPermissions = new RandomizedAgentPermissions(randomizedPermissions);
            if (!this.randomizedPermissions.getHash().equals(hash)) { throw InvalidParameterValueCombinationException.get("The hash of the given permissions has to equal the credential's exposed hash."); }
        } else {
            this.randomizedPermissions = new RandomizedAgentPermissions(hash);
        }
        this.role = tuple.isElementNull(3) ? null : IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(3)).getIdentity().castTo(SemanticType.class);
        if (role != null && !role.isRoleType()) { throw InvalidParameterValueException.get("role", role); }
        this.attributeContent = SelfcontainedWrapper.toElement(tuple.getNullableElement(4));
        if (role != null && attributeContent != null) { throw InvalidParameterValueCombinationException.get("The role and the attribute may not both be not null."); }
        this.restrictions = restrictions;
        if (attributeContent == null && !(issuer instanceof InternalPerson)) { throw InvalidParameterValueCombinationException.get("If the attribute is null, the issuer has to be an internal person."); }
        if (attributeContent != null && restrictions != null) { throw InvalidParameterValueCombinationException.get("The attribute and the restrictions may not both be not null."); }
        if (role != null && getPermissions() == null) { throw InvalidParameterValueCombinationException.get("If a role is given, the permissions may not be null."); }
        if (role != null && restrictions == null) { throw InvalidParameterValueCombinationException.get("If a role is given, the restrictions may not be null."); }
        
        this.exposed = exposed;
        this.o = Exponent.get(exposed.getHash());
        this.i = i != null ? Exponent.get(i) : null;
        
        if (isIdentityBased() && i != null) { throw InvalidParameterValueCombinationException.get("If the credential is identity-based, the value i has to be null."); }
        
        assert invariant();
    }
    
    
    /**
     * Returns the public key of the host that issued this credential.
     * 
     * @return the public key of the host that issued this credential.
     */
    @Pure
    public final @Nonnull PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * Returns the internal non-host identity that issued this credential.
     * 
     * @return the internal non-host identity that issued this credential.
     * 
     * @ensure !isIdentityBased() || issuer instanceof InternalPerson : "If this credential is identity-based, then the issuer is an internal person.";
     */
    @Pure
    public final @Nonnull InternalNonHostIdentity getIssuer() {
        return issuer;
    }
    
    /**
     * Returns the issuance time rounded down to the last half-hour.
     * 
     * @return the issuance time rounded down to the last half-hour.
     * 
     * @ensure issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     */
    @Pure
    public final @Nonnull Time getIssuance() {
        return issuance;
    }
    
    /**
     * Returns whether this credential is still valid.
     * 
     * @return whether this credential is still valid.
     */
    @Pure
    public final boolean isValid() {
        return !issuance.isLessThan(Time.TROPICAL_YEAR.ago());
    }
    
    /**
     * Returns whether this credential is still active.
     * 
     * @return whether this credential is still active.
     */
    @Pure
    public final boolean isActive() {
        return !issuance.isLessThan(Time.HOUR.ago());
    }
    
    /**
     * Returns the client's randomized permissions or simply its hash.
     * 
     * @return the client's randomized permissions or simply its hash.
     */
    @Pure
    public final @Nonnull RandomizedAgentPermissions getRandomizedPermissions() {
        return randomizedPermissions;
    }
    
    /**
     * Returns the permissions of the client or null if they are not shown.
     * 
     * @return the permissions of the client or null if they are not shown.
     * 
     * @ensure permissions.isFrozen() : "The permissions are frozen.";
     */
    @Pure
    public final @Nullable ReadOnlyAgentPermissions getPermissions() {
        return randomizedPermissions.getPermissions();
    }
    
    /**
     * Returns the permissions of the client.
     * 
     * @return the permissions of the client.
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
     * @return the role that is assumed by the client or null in case no role is assumed.
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
     * @return the role that is assumed by the client.
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
     * 
     * @return the attribute content for anonymous access control or null in case of identity-based authentication.
     */
    @Pure
    public final @Nullable Block getAttributeContent() {
        return attributeContent;
    }
    
    /**
     * Returns the attribute content for anonymous access control.
     * 
     * @return the attribute content for anonymous access control.
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
     * 
     * @return whether this credential is used for attribute-based authentication.
     */
    @Pure
    public final boolean isAttributeBased() {
        return attributeContent != null;
    }
    
    /**
     * Returns whether this credential is used for identity-based authentication.
     * 
     * @return whether this credential is used for identity-based authentication.
     */
    @Pure
    public final boolean isIdentityBased() {
        return attributeContent == null;
    }
    
    /**
     * Returns whether this credential is used for role-based authentication.
     * 
     * @return whether this credential is used for role-based authentication.
     */
    @Pure
    public final boolean isRoleBased() {
        return role != null;
    }
    
    /**
     * Returns the restrictions of the client or null in case they are not shown.
     * 
     * @return the restrictions of the client or null in case they are not shown.
     */
    @Pure
    public final @Nullable Restrictions getRestrictions() {
        return restrictions;
    }
    
    /**
     * Returns the restrictions of the client.
     * 
     * @return the restrictions of the client.
     * 
     * @require getRestrictions() != null : "The restrictions are not null.";
     */
    @Pure
    public final @Nonnull Restrictions getRestrictionsNotNull() {
        Require.that(restrictions != null).orThrow("The restrictions are not null.");
        
        return restrictions;
    }
    
    /**
     * Returns the exposed block of this credential.
     * 
     * @return the exposed block of this credential.
     * 
     * @ensure exposed.getType().isBasedOn(EXPOSED) : "The block is based on the indicated type.";
     */
    @Pure
    public final @Nonnull Block getExposed() {
        return exposed;
    }
    
    /**
     * Returns the hash of the exposed block.
     * 
     * @return the hash of the exposed block.
     */
    @Pure
    public final @Nonnull Exponent getO() {
        return o;
    }
    
    /**
     * Returns the serial number of this credential or null if it is not shown.
     * 
     * @return the serial number of this credential or null if it is not shown.
     */
    @Pure
    public @Nullable Exponent getI() {
        return i;
    }
    
    
    /**
     * Returns whether this credential is similar to the given credential.
     * Credentials are similar to each other if they are a randomization of the same credential.
     * 
     * @param credential the credential to compare this credential with.
     * 
     * @return whether this credential is similar to the given credential.
     */
    @Pure
    public final boolean isSimilarTo(@Nonnull Credential credential) {
        return issuer.equals(credential.issuer) && issuance.equals(credential.issuance) && randomizedPermissions.equals(credential.randomizedPermissions) && Objects.equals(role, credential.role) 
                && Objects.equals(attributeContent, credential.attributeContent) && Objects.equals(restrictions, credential.restrictions) && Objects.equals(i, credential.i);
    }
    
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("(Issuer: ").append(issuer.getAddress().getString()).append(", Permissions: ").append(randomizedPermissions.getPermissions());
        if (attributeContent != null) {
            string.append(", Attribute: (");
            try {
                string.append(attributeContent.getType().getAddress().getString());
                if (attributeContent.getType().isBasedOn(StringWrapper.XDF_TYPE)) {
                    string.append(": ").append(StringWrapper.decodeNonNullable(attributeContent));
                }
            } catch (@Nonnull InvalidEncodingException exception) {
                string.append("InvalidEncodingException"); // This should never happen.
            }
            string.append(")");
        } else {
            if (role != null) { string.append(", Role: ").append(role.getAddress().getString()); }
            if (restrictions != null) { string.append(", Restrictions: ").append(restrictions.toFormattedString()); }
        }
        string.append(")");
        return string.toString();
    }
    
}
