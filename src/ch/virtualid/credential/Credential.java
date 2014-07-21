package ch.virtualid.credential;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.client.Client;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.HashWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class abstracts from client and host credentials.
 * 
 * @invariant isIdentityBased() != isAttributeBased() : "The credential is either identity- or attribute-based.";
 * @invariant !isAttributeBased() || getRole() == null && getRestrictions() == null : "If the credential is attribute-based, the role and the restrictions are null.";
 * @invariant getRole() == null || getRestrictions() != null && getPermissions() != null : "If a role is given, the restrictions and the permissions are not null.";
 * 
 * @see ClientCredential
 * @see HostCredential
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Credential implements Immutable {
    
    /**
     * Stores the interval to which the issuance time is rounded down: half an hour in milliseconds.
     */
    public static final long ROUNDING = 1800000;
    
    /**
     * Stores the interval for which credentials are considered to be valid: an hour in milliseconds.
     */
    public static final long VALIDITY = 2 * ROUNDING;
    
    
    /**
     * Asserts that the class invariant still holds.
     */
    private boolean invariant() {
        assert isIdentityBased() != isAttributeBased() : "The credential is either identity- or attribute-based.";
        assert !isAttributeBased() || getRole() == null && getRestrictions() == null : "If the credential is attribute-based, the role and the restrictions are null.";
        assert getRole() == null || getRestrictions() != null && getPermissions() != null : "If a role is given, the restrictions and the permissions are not null.";
        return true;
    }
    
    /**
     * Stores the public key of the host that issued the credential.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Stores the identifier of the non-host identity that issued the credential.
     */
    private final @Nonnull NonHostIdentifier issuer; // TODO: Why not as an identity?
    
    /**
     * Stores the issuance time rounded down to the last half-hour and is always positive.
     */
    private final long issuance;
    
    /**
     * Stores the client's randomized permissions or its randomized hash.
     */
    private final @Nonnull RandomizedAgentPermissions randomizedPermissions;
    
    /**
     * Stores the role that is assumed by the client or null in case no role is assumed.
     */
    private final @Nullable NonHostIdentifier role; // TODO: Why not as an identity?
    
    /**
     * Stores the attribute without the certificate for anonymous access control or null in case of identity-based authentication.
     */
    private final @Nullable Block attribute;
    
    /**
     * Stores the restrictions of the client or null in case of attribute-based authentication.
     */
    private final @Nullable Restrictions restrictions;
    
    
    /**
     * Stores the exposed block of this credential.
     */
    private final @Nonnull Block exposed;
    
    /**
     * Stores the hash of the exposed block.
     */
    private final @Nonnull Exponent o;
    
    
    /**
     * Stores the serial number of this credential or null if it is not disclosed.
     */
    private final @Nullable Exponent i;
    
    
    /**
     * Creates a new credential with the given public key, issuer, issuance time, permissions, attribute, restrictions and argument for clients.
     * 
     * @param publicKey the public key of the host that issued the credential.
     * @param issuer the identifier of the identity that issued the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions or its hash.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attribute the attribute without the certificate for anonymous access control.
     * @param restrictions the restrictions of the client.
     * @param i the serial number of this credential.
     * 
     * @require issuance > 0l && issuance % Credential.ROUNDING == 0 : "The issuance time is always positive and a multiple of the rounding interval.";
     * @require randomizedPermissions.getPermissions() != null : "The permissions may never be null for client credentials.";
     * @require (attribute == null) != (restrictions == null) : "Either the attribute or the restrictions is not null (but not both).";
     * @require attribute == null || role == null && restrictions == null : "If the attribute is not null, both the role and the restrictions have to be null.";
     * @require role == null || restrictions != null : "If a role is given, the restrictions is not null.";
     */
    Credential(@Nonnull PublicKey publicKey, @Nonnull NonHostIdentifier issuer, long issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable NonHostIdentifier role, @Nullable Block attribute, @Nullable Restrictions restrictions, @Nonnull Exponent i) {
        assert issuance > 0l && issuance % ROUNDING == 0 : "The issuance time is always positive and a multiple of the rounding interval.";
        assert randomizedPermissions.getPermissions() != null : "The permissions may never be null for client credentials.";
        assert (attribute == null) != (restrictions == null) : "Either the attribute or the restrictions is not null (but not both).";
        assert attribute == null || role == null && restrictions == null : "If the attribute is not null, both the role and the restrictions have to be null.";
        assert role == null || restrictions != null : "If a role is given, the restrictions is not null.";
        
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
     * @param exposed the block containing the exposed argument of the credential.
     * @param randomizedPermissions the block containing the client's randomized permissions.
     * @param restrictions the block containing the client's restrictions.
     * @param i the block containing the credential's serial number.
     */
    Credential(@Nonnull Block exposed, @Nonnull Block randomizedPermissions, @Nonnull Block restrictions, @Nonnull Block i) throws InvalidEncodingException, FailedIdentityException {
        @Nonnull Block[] elements = new TupleWrapper(exposed).getElementsNotNull(5);
        this.issuer = new NonHostIdentifier(elements[0]);
        this.publicKey = new PublicKey(Client.getAttributeNotNullUnwrapped(issuer.getHostIdentifier().getIdentity(), SemanticType.HOST_PUBLIC_KEY));
        this.issuance = new Int64Wrapper(elements[1]).getValue();
        if (issuance <= 0l && issuance % ROUNDING != 0) throw new InvalidEncodingException("The issuance time is positive and a multiple of the rounding interval.");
        @Nonnull BigInteger hash = new HashWrapper(elements[2]).getValue();
        if (randomizedPermissions.isNotEmpty()) {
            this.randomizedPermissions = new RandomizedAgentPermissions(randomizedPermissions);
            if (!randomizedPermissions.getHash().equals(hash)) throw new InvalidEncodingException("The hash of the given permissions has to equal the credential's exposed hash.");
        } else {
            this.randomizedPermissions = new RandomizedAgentPermissions(hash);
        }
        this.role = elements[3].isNotEmpty() ? new NonHostIdentifier(elements[3]) : null;
        this.attribute = elements[4].isNotEmpty() ? elements[4] : null;
        this.restrictions = restrictions.isNotEmpty() ? new Restrictions(restrictions) : null;
        
        if (this.attribute != null && (this.role != null || this.restrictions != null)) throw new InvalidEncodingException("If the credential is attribute-based, the role and the restrictions have to be null.");
        if (this.role != null && this.restrictions == null) throw new InvalidEncodingException("If a role is given, the restrictions is not null.");
        if (this.role != null && this.getPermissions() == null) throw new InvalidEncodingException("If a role is given, the permissions is not null.");
        
        this.exposed = exposed;
        this.o = new Exponent(exposed.getHash());
        this.i = i.isNotEmpty() ? new Exponent(i) : null;
        
        if (isIdentityBased() && this.i != null) throw new InvalidEncodingException("If the credential is identity-based, the value i is null.");
        
        assert invariant();
    }
    
    /**
     * Returns the public key of the host that issued the credential.
     * 
     * @return the public key of the host that issued the credential.
     */
    public final @Nonnull PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * Returns the identifier of the identity that issued the credential.
     * 
     * @return the identifier of the identity that issued the credential.
     */
    public final @Nonnull NonHostIdentifier getIssuer() {
        return issuer;
    }
    
    /**
     * Returns the issuance time rounded down to the last half-hour and the result is always positive.
     * 
     * @return the issuance time rounded down to the last half-hour and the result is always positive.
     * 
     * @ensure getIssuance > 0l && getIssuance % Credential.ROUNDING == 0 : "The issuance time is always positive and a multiple of the rounding interval.";
     */
    public final long getIssuance() {
        return issuance;
    }
    
    /**
     * Returns the client's randomized permissions or simply its hash.
     * 
     * @return the client's randomized permissions or simply its hash.
     */
    public final @Nonnull RandomizedAgentPermissions getRandomizedPermissions() {
        return randomizedPermissions;
    }
    
    /**
     * Returns the client's permissions or null if it is not disclosed.
     * 
     * @return the client's permissions or null if it is not disclosed.
     */
    public final @Nullable AgentPermissions getPermissions() {
        return randomizedPermissions.getPermissions();
    }
    
    /**
     * Returns the role that is assumed by the client or null in case no role is assumed.
     * 
     * @return the role that is assumed by the client or null in case no role is assumed.
     */
    public final @Nullable NonHostIdentifier getRole() {
        return role;
    }
    
    /**
     * Returns the attribute without the certificate for anonymous access control or null in case of identity-based authentication.
     * 
     * @return the attribute without the certificate for anonymous access control or null in case of identity-based authentication.
     */
    public final @Nullable Block getAttribute() {
        return attribute;
    }
    
    /**
     * Returns whether this credential is used for attribute-based authentication.
     * 
     * @return whether this credential is used for attribute-based authentication.
     */
    public final boolean isAttributeBased() {
        return attribute != null;
    }
    
    /**
     * Returns the restrictions of the client or null in case of attribute-based authentication.
     * 
     * @return the restrictions of the client or null in case of attribute-based authentication.
     */
    public final @Nullable Restrictions getRestrictions() {
        return restrictions;
    }
    
    /**
     * Returns whether this credential is used for identity-based authentication.
     * 
     * @return whether this credential is used for identity-based authentication.
     */
    public final boolean isIdentityBased() {
        return attribute == null;
    }
    
    /**
     * Returns the exposed block of this credential.
     * 
     * @return the exposed block of this credential.
     */
    public final @Nonnull Block getExposed() {
        return exposed;
    }
    
    /**
     * Returns the hash of the exposed block.
     * 
     * @return the hash of the exposed block.
     */
    public final @Nonnull Exponent getO() {
        return o;
    }
    
    /**
     * Returns the serial number of this credential or null if it is not disclosed.
     * 
     * @return the serial number of this credential or null if it is not disclosed.
     */
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
    public final boolean isSimilarTo(@Nonnull Credential credential) {
        return issuer.equals(credential.issuer) && issuance == credential.issuance && randomizedPermissions.equals(credential.randomizedPermissions) && Objects.equals(role, credential.role) 
                && Objects.equals(attribute, credential.attribute) && Objects.equals(restrictions, credential.restrictions) && Objects.equals(i, credential.i);
    }
    
    @Override
    public final @Nonnull String toString() {
        @Nonnull StringBuilder string = new StringBuilder("(Issuer: ").append(issuer).append(", Permissions: ").append(randomizedPermissions.getPermissions());
        if (isAttributeBased()) {
            string.append(", Attribute: (");
            try {
                @Nonnull SelfcontainedWrapper selfcontainedWrapper = new SelfcontainedWrapper(attribute);
                string.append(selfcontainedWrapper.getIdentifier());
                try {
                    if (selfcontainedWrapper.getIdentifier().getIdentity().toSemanticType().isBasedOn(SyntacticType.STRING)) {
                        string.append(": ").append(new StringWrapper(selfcontainedWrapper.getElement()).getString());
                    }
                } catch (FailedIdentityException exception) {
                    string.append(": IdentityNotFoundException");
                } catch (InvalidDeclarationException exception) {
                    string.append(": InvalidDeclarationException");
                }
            } catch (InvalidEncodingException exception) {
                string.append("InvalidEncodingException");
            }
            string.append(")");
        } else {
            if (role != null) string.append(", Role: ").append(role);
            if (restrictions != null) string.append(", Restrictions: ").append(restrictions);
        }
        string.append(")");
        return string.toString();
    }
    
    
    /**
     * Returns the block containing the exposed argument of the credential.
     * 
     * @param issuer the identifier of the identity that issued the credential.
     * @param issuance the issuance time rounded to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions or its hash.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attribute the attribute without the certificate for anonymous access control.
     * 
     * @return the block containing the exposed argument of the credential.
     * 
     * @require issuance > 0l && issuance % Credential.ROUNDING == 0 : "The issuance time is always positive and a multiple of the rounding interval.";
     */
    public static @Nonnull Block getExposed(@Nonnull NonHostIdentifier issuer, long issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable NonHostIdentifier role, @Nullable Block attribute) {
        assert issuance > 0l && issuance % ROUNDING == 0 : "The issuance time is always positive and a multiple of the rounding interval.";
        
        @Nonnull Block[] elements = new Block[5];
        elements[0] = issuer.toBlock();
        elements[1] = new Int64Wrapper(issuance).toBlock();
        elements[2] = new HashWrapper(randomizedPermissions.getHash()).toBlock();
        elements[3] = role == null ? Block.EMPTY : role.toBlock();
        elements[4] = attribute == null ? Block.EMPTY : attribute;
        return new TupleWrapper(elements).toBlock();
    }
    
}
