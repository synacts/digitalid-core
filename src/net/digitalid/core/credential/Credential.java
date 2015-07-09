package net.digitalid.core.credential;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.RandomizedAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.NonHostIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.HashWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.StringWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

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
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Credential implements Immutable {
    
    /**
     * Stores the semantic type {@code issuer.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ISSUER = SemanticType.create("issuer.exposed.credential@core.digitalid.net").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code issuance.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ISSUANCE = SemanticType.create("issuance.exposed.credential@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code hash.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType HASH = SemanticType.create("hash.exposed.credential@core.digitalid.net").load(RandomizedAgentPermissions.HASH);
    
    /**
     * Stores the semantic type {@code role.exposed.credential@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ROLE = SemanticType.create("role.exposed.credential@core.digitalid.net").load(SemanticType.ROLE_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code exposed.credential@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType EXPOSED = SemanticType.create("exposed.credential@core.digitalid.net").load(TupleWrapper.TYPE, ISSUER, ISSUANCE, HASH, ROLE, AttributeValue.CONTENT);
    
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
        assert issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
        assert role == null || role.isRoleType() : "The role is either null or a role type.";
        
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(5);
        elements.set(0, issuer.toBlock(ISSUER));
        elements.set(1, issuance.toBlock().setType(ISSUANCE));
        elements.set(2, new HashWrapper(HASH, randomizedPermissions.getHash()).toBlock());
        elements.set(3, Block.toBlock(ROLE, role));
        elements.set(4, SelfcontainedWrapper.toBlock(AttributeValue.CONTENT, attributeContent));
        return new TupleWrapper(EXPOSED, elements.freeze()).toBlock();
    }
    
    
    /**
     * Asserts that the class invariant still holds.
     */
    @Pure
    private boolean invariant() {
        assert isIdentityBased() != isAttributeBased() : "This credential is either identity- or attribute-based.";
        assert !isRoleBased() || isIdentityBased() : "If this credential is role-based, it is also identity-based";
        assert !isAttributeBased() || getRestrictions() == null : "If this credential is attribute-based, the restrictions are null.";
        assert !isRoleBased() || getPermissions() != null && getRestrictions() != null : "If this credential is role-based, both the permissions and the restrictions are not null.";
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
        assert issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
        assert randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
        assert role == null || role.isRoleType() : "The role is either null or a role type.";
        assert role == null || restrictions != null : "If a role is given, the restrictions are not null.";
        assert attributeContent != null || issuer instanceof InternalPerson : "If the attribute content is null, the issuer is an internal person.";
        assert (attributeContent == null) != (restrictions == null) : "Either the attribute content or the restrictions are null (but not both).";
        
        this.publicKey = publicKey;
        this.issuer = issuer;
        this.issuance = issuance;
        this.randomizedPermissions = randomizedPermissions;
        this.role = role;
        this.attributeContent = attributeContent;
        this.restrictions = restrictions;
        
        this.exposed = getExposed(issuer, issuance, randomizedPermissions, role, attributeContent);
        this.o = new Exponent(this.exposed.getHash());
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
    Credential(@Nonnull Block exposed, @Nullable Block randomizedPermissions, @Nullable Restrictions restrictions, @Nullable Block i) throws SQLException, IOException, PacketException, ExternalException {
        assert exposed.getType().isBasedOn(Credential.EXPOSED) : "The exposed block is based on the indicated type.";
        assert randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE) : "The randomized permissions are either null or based on the indicated type.";
        assert i == null || i.getType().isBasedOn(Exponent.TYPE) : "The serial number is either null or based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(exposed);
        this.issuer = IdentifierClass.create(tuple.getElementNotNull(0)).getIdentity().toInternalNonHostIdentity();
        this.issuance = new Time(tuple.getElementNotNull(1));
        if (!issuance.isPositive() || !issuance.isMultipleOf(Time.HALF_HOUR)) throw new InvalidEncodingException("The issuance time has to be positive and a multiple of half an hour.");
        this.publicKey = Cache.getPublicKey(issuer.getAddress().getHostIdentifier(), issuance);
        final @Nonnull BigInteger hash = new HashWrapper(tuple.getElementNotNull(2)).getValue();
        if (randomizedPermissions != null) {
            this.randomizedPermissions = new RandomizedAgentPermissions(randomizedPermissions);
            if (!this.randomizedPermissions.getHash().equals(hash)) throw new InvalidEncodingException("The hash of the given permissions has to equal the credential's exposed hash.");
        } else {
            this.randomizedPermissions = new RandomizedAgentPermissions(hash);
        }
        this.role = tuple.isElementNull(3) ? null : IdentifierClass.create(tuple.getElementNotNull(3)).getIdentity().toSemanticType();
        if (role != null && !role.isRoleType()) throw new InvalidEncodingException("The role has to be either null or a role type");
        this.attributeContent = SelfcontainedWrapper.toElement(tuple.getElement(4));
        if (role != null && attributeContent != null) throw new InvalidEncodingException("The role and the attribute may not both be not null.");
        this.restrictions = restrictions;
        if (attributeContent == null && !(issuer instanceof InternalPerson)) throw new InvalidEncodingException("If the attribute is null, the issuer has to be an internal person.");
        if (attributeContent != null && restrictions != null) throw new InvalidEncodingException("The attribute and the restrictions may not both be not null.");
        if (role != null && getPermissions() == null) throw new InvalidEncodingException("If a role is given, the permissions may not be null.");
        if (role != null && restrictions == null) throw new InvalidEncodingException("If a role is given, the restrictions may not be null.");
        
        this.exposed = exposed;
        this.o = new Exponent(exposed.getHash());
        this.i = i != null ? new Exponent(i) : null;
        
        if (isIdentityBased() && i != null) throw new InvalidEncodingException("If the credential is identity-based, the value i has to be null.");
        
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
        assert isRoleBased() : "This credential is role-based.";
        
        final @Nullable ReadOnlyAgentPermissions permissions = randomizedPermissions.getPermissions();
        assert permissions != null : "This follows from the class invariant.";
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
        assert role != null : "This credential is role-based.";
        
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
        assert attributeContent != null : "This credential is attribute-based.";
        
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
        assert restrictions != null : "The restrictions are not null.";
        
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
                if (attributeContent.getType().isBasedOn(StringWrapper.TYPE)) {
                    string.append(": ").append(new StringWrapper(attributeContent).getString());
                }
            } catch (@Nonnull InvalidEncodingException exception) {
                string.append("InvalidEncodingException"); // This should never happen.
            }
            string.append(")");
        } else {
            if (role != null) string.append(", Role: ").append(role.getAddress().getString());
            if (restrictions != null) string.append(", Restrictions: ").append(restrictions.toFormattedString());
        }
        string.append(")");
        return string.toString();
    }
    
}
