package ch.virtualid.credential;

import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Cache;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.HashWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Credential implements Immutable {
    
    /**
     * Stores the semantic type {@code issuer.exposed.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ISSUER = SemanticType.create("issuer.exposed.credential@virtualid.ch").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code issuance.exposed.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ISSUANCE = SemanticType.create("issuance.exposed.credential@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code hash.exposed.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType HASH = SemanticType.create("hash.exposed.credential@virtualid.ch").load(RandomizedAgentPermissions.HASH);
    
    /**
     * Stores the semantic type {@code role.exposed.credential@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ROLE = SemanticType.create("role.exposed.credential@virtualid.ch").load(SemanticType.ROLE_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code exposed.credential@virtualid.ch}.
     */
    public static final @Nonnull SemanticType EXPOSED = SemanticType.create("exposed.credential@virtualid.ch").load(TupleWrapper.TYPE, ISSUER, ISSUANCE, HASH, ROLE, Attribute.TYPE);
    
    /**
     * Returns the block containing the exposed arguments of a credential.
     * 
     * @param issuer the non-host identity that issues the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions or its hash.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attribute the attribute without the certificate for attribute-based access control.
     * 
     * @return the block containing the exposed arguments of a credential.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
     * @require attribute == null || attribute.getType().isBasedOn(Attribute.TYPE) : "The attribute is either null or based on the attribute type.";
     * 
     * @ensure return.getType().equals(EXPOSED) : "The returned block has the indicated type.";
     */
    @Pure
    public static @Nonnull Block getExposed(@Nonnull NonHostIdentity issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nullable Block attribute) {
        assert issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
        assert role == null || role.isRoleType() : "The role is either null or a role type.";
        assert attribute == null || attribute.getType().isBasedOn(Attribute.TYPE) : "The attribute is either null or based on the attribute type.";
        
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(5);
        elements.set(0, issuer.getAddress().toBlock().setType(ISSUER));
        elements.set(1, issuance.toBlock().setType(ISSUANCE));
        elements.set(2, new HashWrapper(HASH, randomizedPermissions.getHash()).toBlock());
        elements.set(3, role == null ? null : role.getAddress().toBlock().setType(ROLE));
        elements.set(4, attribute);
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
     * Stores the non-host identity that issued this credential.
     */
    private final @Nonnull NonHostIdentity issuer;
    
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
     * Stores the attribute without the certificate for attribute-based access control or null in case of identity-based authentication.
     * 
     * @invariant attribute == null || attribute.getType().isBasedOn(Attribute.TYPE) : "The attribute is either null or based on the attribute type.";
     */
    private final @Nullable Block attribute;
    
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
     * Creates a new credential with the given public key, issuer, issuance time, permissions, attribute, restrictions and argument for clients.
     * 
     * @param publicKey the public key of the host that issued the credential.
     * @param issuer the non-host identity that issued the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions or its hash.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attribute the attribute without the certificate for anonymous access control.
     * @param restrictions the restrictions of the client.
     * @param i the serial number of the credential.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
     * @require role == null || restrictions != null : "If a role is given, the restrictions are not null.";
     * @require (attribute == null) != (restrictions == null) : "Either the attribute or the restrictions are null (but not both).";
     * @require attribute == null || attribute.getType().isBasedOn(Attribute.TYPE) : "The attribute is either null or based on the attribute type.";
     */
    Credential(@Nonnull PublicKey publicKey, @Nonnull NonHostIdentity issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nullable Block attribute, @Nullable Restrictions restrictions, @Nonnull Exponent i) {
        assert issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
        assert randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
        assert role == null || role.isRoleType() : "The role is either null or a role type.";
        assert role == null || restrictions != null : "If a role is given, the restrictions are not null.";
        assert (attribute == null) != (restrictions == null) : "Either the attribute or the restrictions are null (but not both).";
        assert attribute == null || attribute.getType().isBasedOn(Attribute.TYPE) : "The attribute is either null or based on the attribute type.";
        
        this.publicKey = publicKey;
        this.issuer = issuer;
        this.issuance = issuance;
        this.randomizedPermissions = randomizedPermissions;
        this.role = role;
        this.attribute = attribute;
        this.restrictions = restrictions;
        
        this.exposed = getExposed(issuer, issuance, randomizedPermissions, role, attribute);
        this.o = new Exponent(this.exposed.getHash());
        this.i = i;
        
        assert invariant();
    }
    
    /**
     * Creates a new credential from the given blocks for hosts.
     * 
     * @param entity the entity to which the credential belongs.
     * @param exposed the block containing the exposed arguments of the credential.
     * @param randomizedPermissions the block containing the client's randomized permissions.
     * @param restrictions the block containing the client's restrictions.
     * @param i the block containing the credential's serial number.
     * 
     * @require exposed.getType().isBasedOn(Credential.EXPOSED) : "The exposed block is based on the indicated type.";
     * @require randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE) : "The randomized permissions are either null or based on the indicated type.";
     * @require restrictions == null || restrictions.getType().isBasedOn(Restrictions.TYPE) : "The restrictions are either null or based on the indicated type.";
     * @require i == null || i.getType().isBasedOn(Exponent.TYPE) : "The serial number is either null or based on the indicated type.";
     */
    Credential(@Nonnull Entity entity, @Nonnull Block exposed, @Nullable Block randomizedPermissions, @Nullable Block restrictions, @Nullable Block i) throws InvalidEncodingException, FailedIdentityException, SQLException, InvalidDeclarationException {
        assert exposed.getType().isBasedOn(Credential.EXPOSED) : "The exposed block is based on the indicated type.";
        assert randomizedPermissions == null || randomizedPermissions.getType().isBasedOn(RandomizedAgentPermissions.TYPE) : "The randomized permissions are either null or based on the indicated type.";
        assert restrictions == null || restrictions.getType().isBasedOn(Restrictions.TYPE) : "The restrictions are either null or based on the indicated type.";
        assert i == null || i.getType().isBasedOn(Exponent.TYPE) : "The serial number is either null or based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(exposed);
        this.issuer = new NonHostIdentifier(tuple.getElementNotNull(0)).getIdentity().toNonHostIdentity();
        this.publicKey = new PublicKey(Cache.getAttributeNotNullUnwrapped(issuer.getAddress().getHostIdentifier().getIdentity(), PublicKeyChain.TYPE));
        this.issuance = new Time(tuple.getElementNotNull(1));
        if (!issuance.isPositive() || !issuance.isMultipleOf(Time.HALF_HOUR)) throw new InvalidEncodingException("The issuance time has to be positive and a multiple of half an hour.");
        final @Nonnull BigInteger hash = new HashWrapper(tuple.getElementNotNull(2)).getValue();
        if (randomizedPermissions != null) {
            this.randomizedPermissions = new RandomizedAgentPermissions(randomizedPermissions);
            if (!this.randomizedPermissions.getHash().equals(hash)) throw new InvalidEncodingException("The hash of the given permissions has to equal the credential's exposed hash.");
        } else {
            this.randomizedPermissions = new RandomizedAgentPermissions(hash);
        }
        this.role = tuple.isElementNull(3) ? null : new NonHostIdentifier(tuple.getElementNotNull(3)).getIdentity().toSemanticType();
        if (role != null && !role.isRoleType()) throw new InvalidEncodingException("The role has to be either null or a role type");
        this.attribute = tuple.getElement(4);
        if (role != null && attribute != null) throw new InvalidEncodingException("The role and the attribute may not both be not null.");
        this.restrictions = restrictions != null ? new Restrictions(entity, restrictions) : null;
        if (attribute != null && restrictions != null) throw new InvalidEncodingException("The attribute and the restrictions may not both be not null.");
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
     * Returns the non-host identity that issued this credential.
     * 
     * @return the non-host identity that issued this credential.
     */
    @Pure
    public final @Nonnull NonHostIdentity getIssuer() {
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
     */
    @Pure
    public final @Nullable ReadonlyAgentPermissions getPermissions() {
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
    public final @Nonnull ReadonlyAgentPermissions getPermissionsNotNull() {
        assert isRoleBased() : "This credential is role-based.";
        
        final @Nullable ReadonlyAgentPermissions permissions = randomizedPermissions.getPermissions();
        assert permissions != null : "This follows from the class invariant.";
        return permissions;
    }
    
    /**
     * Returns the role that is assumed by the client or null in case no role is assumed.
     * 
     * @return the role that is assumed by the client or null in case no role is assumed.
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
     * Returns the attribute without the certificate for anonymous access control or null in case of identity-based authentication.
     * 
     * @return the attribute without the certificate for anonymous access control or null in case of identity-based authentication.
     */
    @Pure
    public final @Nullable Block getAttribute() {
        return attribute;
    }
    
    /**
     * Returns whether this credential is used for attribute-based authentication.
     * 
     * @return whether this credential is used for attribute-based authentication.
     */
    @Pure
    public final boolean isAttributeBased() {
        return attribute != null;
    }
    
    /**
     * Returns whether this credential is used for identity-based authentication.
     * 
     * @return whether this credential is used for identity-based authentication.
     */
    @Pure
    public final boolean isIdentityBased() {
        return attribute == null;
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
     * @require isRoleBased() : "This credential is role-based.";
     */
    @Pure
    public final @Nonnull Restrictions getRestrictionsNotNull() {
        assert isRoleBased() : "This credential is role-based.";
        
        assert restrictions != null : "This follows from the class invariant.";
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
                && Objects.equals(attribute, credential.attribute) && Objects.equals(restrictions, credential.restrictions) && Objects.equals(i, credential.i);
    }
    
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("(Issuer: ").append(issuer.getAddress().getString()).append(", Permissions: ").append(randomizedPermissions.getPermissions());
        if (isAttributeBased()) {
            string.append(", Attribute: (");
            try {
                final @Nonnull Block element = new SelfcontainedWrapper(attribute).getElement();
                string.append(element.getType().getAddress().getString());
                if (element.getType().isBasedOn(StringWrapper.TYPE)) {
                    string.append(": ").append(new StringWrapper(element).getString());
                }
            } catch (@Nonnull InvalidEncodingException exception) {
                string.append("InvalidEncodingException");
            } catch (@Nonnull FailedIdentityException exception) {
                string.append("FailedIdentityException");
            } catch (@Nonnull InvalidDeclarationException exception) {
                string.append("InvalidDeclarationException");
            } catch (@Nonnull SQLException exception) {
                string.append("SQLException");
            }
            string.append(")");
        } else {
            if (role != null) string.append(", Role: ").append(role.getAddress().getString());
            if (restrictions != null) string.append(", Restrictions: ").append(restrictions);
        }
        string.append(")");
        return string.toString();
    }
    
}
