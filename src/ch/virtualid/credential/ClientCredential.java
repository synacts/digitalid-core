package ch.virtualid.credential;

import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.attribute.CertifiedAttributeValue;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.entity.NonNativeRole;
import ch.virtualid.entity.Role;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.InternalPerson;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.xdf.Block;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class models credentials on the client-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientCredential extends Credential implements Immutable {
    
    /**
     * Stores the certifying base of this credential.
     */
    private final @Nonnull Element c;
    
    /**
     * Stores the certifying exponent of this credential.
     */
    private final @Nonnull Exponent e;
    
    /**
     * Stores the blinding exponent of this credential.
     */
    private final @Nonnull Exponent b;
    
    /**
     * Stores the client's secret of this credential.
     */
    private final @Nonnull Exponent u;
    
    /**
     * Stores the hash of restrictions or the hash of the subject's identifier.
     */
    private final @Nonnull Exponent v;
    
    /**
     * Stores whether the credential can be used only once (i.e. 'i' is to be disclosed).
     */
    private final boolean oneTime;
    
    /**
     * Creates a new identity-based credential with the given public key, issuer, issuance, permissions, role, restrictions and arguments for clients.
     * 
     * @param publicKey the public key of the host that issued the credential.
     * @param issuer the internal person that issued the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param restrictions the restrictions of the client.
     * 
     * @param c the certifying base of this credential.
     * @param e the certifying exponent of this credential.
     * @param b the blinding exponent of this credential.
     * @param u the client's secret of this credential.
     * @param i the serial number of this credential.
     * @param v the hash of restrictions.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
     * @require restrictions == null || restrictions.toBlock().getHash().equals(v.getValue()) : "If the restrictions are not null, their hash has to equal v.";
     */
    public ClientCredential(@Nonnull PublicKey publicKey, @Nonnull InternalPerson issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nonnull Restrictions restrictions, @Nonnull Element c, @Nonnull Exponent e, @Nonnull Exponent b, @Nonnull Exponent u, @Nonnull Exponent i, @Nonnull Exponent v) throws InvalidSignatureException {
        this(publicKey, issuer, issuance, randomizedPermissions, role, null, restrictions, c, e, b, u, i, v, false);
    }
    
    /**
     * Creates a new attribute-based credential with the given public key, issuer, issuance, permissions, attribute content and arguments for clients.
     * 
     * @param publicKey the public key of the host that issued the credential.
     * @param issuer the internal non-host identity that issued the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions.
     * @param attributeContent the attribute content for anonymous access control.
     * 
     * @param c the certifying base of this credential.
     * @param e the certifying exponent of this credential.
     * @param b the blinding exponent of this credential.
     * @param u the client's secret of this credential.
     * @param i the serial number of this credential.
     * @param v the hash of the subject's identifier.
     * 
     * @param oneTime whether the credential can be used only once.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
     */
    public ClientCredential(@Nonnull PublicKey publicKey, @Nonnull InternalNonHostIdentity issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nonnull Block attributeContent, @Nonnull Element c, @Nonnull Exponent e, @Nonnull Exponent b, @Nonnull Exponent u, @Nonnull Exponent i, @Nonnull Exponent v, boolean oneTime) throws InvalidSignatureException {
        this(publicKey, issuer, issuance, randomizedPermissions, null, attributeContent, null, c, e, b, u, i, v, oneTime);
    }
    
    /**
     * Creates a new credential with the given given public key, issuer, issuance, permissions, role, attribute content, restrictions and arguments for clients.
     * 
     * @param publicKey the public key of the host that issued the credential.
     * @param issuer the internal non-host identity that issued the credential.
     * @param issuance the issuance time rounded down to the last half-hour.
     * @param randomizedPermissions the client's randomized permissions.
     * @param role the role that is assumed by the client or null in case no role is assumed.
     * @param attributeContent the attribute content for anonymous access control.
     * @param restrictions the restrictions of the client.
     * 
     * @param c the certifying base of this credential.
     * @param e the certifying exponent of this credential.
     * @param b the blinding exponent of this credential.
     * @param u the client's secret of this credential.
     * @param i the serial number of this credential.
     * @param v the hash of restrictions or the hash of the subject's identifier.
     * 
     * @param oneTime whether the credential can be used only once.
     * 
     * @require issuance.isPositive() && issuance.isMultipleOf(Time.HALF_HOUR) : "The issuance time is positive and a multiple of half an hour.";
     * @require randomizedPermissions.areShown() : "The randomized permissions are shown for client credentials.";
     * @require role == null || role.isRoleType() : "The role is either null or a role type.";
     * @require role == null || restrictions != null : "If a role is given, the restrictions are not null.";
     * @require attributeContent != null || issuer instanceof Person : "If the attribute content is null, the issuer is a person.";
     * @require (attributeContent == null) != (restrictions == null) : "Either the attribute content or the restrictions are null (but not both).";
     * @require restrictions == null || restrictions.toBlock().getHash().equals(v.getValue()) : "If the restrictions are not null, their hash has to equal v.";
     * @require !oneTime || attributeContent != null : "If the credential can be used only once, the attribute content may not be null.";
     */
    private ClientCredential(@Nonnull PublicKey publicKey, @Nonnull InternalNonHostIdentity issuer, @Nonnull Time issuance, @Nonnull RandomizedAgentPermissions randomizedPermissions, @Nullable SemanticType role, @Nullable Block attributeContent, @Nullable Restrictions restrictions, @Nonnull Element c, @Nonnull Exponent e, @Nonnull Exponent b, @Nonnull Exponent u, @Nonnull Exponent i, @Nonnull Exponent v, boolean oneTime) throws InvalidSignatureException {
        super(publicKey, issuer, issuance, randomizedPermissions, role, attributeContent, restrictions, i);
        
        assert restrictions == null || restrictions.toBlock().getHash().equals(v.getValue()) : "If the restrictions are not null, their hash has to equal v.";
        assert !oneTime || attributeContent != null : "If the credential can be used only once, the attribute content may not be null.";
        
        this.c = c;
        this.e = e;
        this.b = b;
        this.u = u;
        this.v = v;
        
        this.oneTime = oneTime;
        
        if (!publicKey.getAo().pow(getO()).equals(c.pow(e).multiply(publicKey.getAb().pow(b)).multiply(publicKey.getAu().pow(u)).multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)))) throw new InvalidSignatureException("The credential issued by " + issuer.getAddress() + " is invalid.");
    }
    
    /**
     * Returns the certifying base of this credential.
     * 
     * @return the certifying base of this credential.
     */
    public @Nonnull Element getC() {
        return c;
    }
    
    /**
     * Returns the certifying exponent of this credential.
     * 
     * @return the certifying exponent of this credential.
     */
    public @Nonnull Exponent getE() {
        return e;
    }
    
    /**
     * Returns the blinding exponent of this credential.
     * 
     * @return the blinding exponent of this credential.
     */
    public @Nonnull Exponent getB() {
        return b;
    }
    
    /**
     * Returns the client's secret of this credential.
     * 
     * @return the client's secret of this credential.
     */
    public @Nonnull Exponent getU() {
        return u;
    }
    
    /**
     * Returns the hash of restrictions or the hash of the subject's identifier.
     * 
     * @return the hash of restrictions or the hash of the subject's identifier.
     */
    public @Nonnull Exponent getV() {
        return v;
    }
    
    /**
     * Returns the serial number of this credential.
     * 
     * @return the serial number of this credential.
     */
    @Override
    public @Nonnull Exponent getI() {
        final @Nullable Exponent i = super.getI();
        assert i != null : "The value i is always known in client credentials (see the constructor above).";
        return i;
    }
    
    /**
     * Returns whether this credential can be used only once (i.e. 'i' is shown).
     * 
     * @return whether this credential can be used only once (i.e. 'i' is shown).
     */
    public boolean isOneTime() {
        return oneTime;
    }
    
    /**
     * Returns a randomized version of this credential.
     * 
     * @return a randomized version of this credential.
     */
    public @Nonnull ClientCredential getRandomizedCredential() {
        try {
            final @Nonnull Exponent r = new Exponent(new BigInteger(Parameters.BLINDING_EXPONENT - Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
            return new ClientCredential(getPublicKey(), getIssuer(), getIssuance(), getRandomizedPermissions(), getRole(), getAttributeContent(), getRestrictions(), c.multiply(getPublicKey().getAb().pow(r)), e, b.subtract(e.multiply(r)), u, getI(), v, oneTime);
        } catch (@Nonnull InvalidSignatureException exception) {
            throw new ShouldNeverHappenError("The randomization of a client credential should yield another valid client credential.", exception);
        }
    }
    
    
    /**
     * Caches the role-based client credentials given their role and permissions.
     */
    private static final @Nonnull ConcurrentMap<NonNativeRole, ConcurrentMap<ReadonlyAgentPermissions, ClientCredential>> roleBasedCredentials = new ConcurrentHashMap<NonNativeRole, ConcurrentMap<ReadonlyAgentPermissions, ClientCredential>>();
    
    /**
     * Returns a role-based credential for the given role and permissions.
     * 
     * @param role the role for which the credential is to be returned.
     * @param permissions the permissions which are to be contained.
     * 
     * @return a role-based credential for the given role and permissions.
     * 
     * @require role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require permissions.isNotEmpty() : "The permissions are not empty.";
     * 
     * @ensure return.isActive() : "The returned credential is active.";
     */
    public static @Nonnull ClientCredential getRoleBased(@Nonnull NonNativeRole role, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException, IOException, PacketException, ExternalException {
        assert role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
        assert permissions.isFrozen() : "The permissions are frozen.";
        assert permissions.isNotEmpty() : "The permissions are not empty.";
        
        @Nullable ConcurrentMap<ReadonlyAgentPermissions, ClientCredential> map = roleBasedCredentials.get(role);
        if (map == null) map = roleBasedCredentials.putIfAbsentElseReturnPresent(role, new ConcurrentHashMap<ReadonlyAgentPermissions, ClientCredential>());
        @Nullable ClientCredential credential = map.get(permissions);
        
        if (credential == null || !credential.isActive()) {
            final @Nonnull RandomizedAgentPermissions randomizedPermissions = new RandomizedAgentPermissions(permissions);
            final @Nonnull BigInteger value = new BigInteger(Parameters.BLINDING_EXPONENT, new SecureRandom());
            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions, value).sendNotNull();
            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getCredential(randomizedPermissions));
        }
        
        return credential;
    }
    
    
    /**
     * Caches the identity-based client credentials given their role and permissions.
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<ReadonlyAgentPermissions, ClientCredential>> identityBasedCredentials = new ConcurrentHashMap<Role, ConcurrentMap<ReadonlyAgentPermissions, ClientCredential>>();
    
    /**
     * Returns an identity-based credential for the given role and permissions.
     * 
     * @param role the role for which the credential is to be returned.
     * @param permissions the permissions which are to be contained.
     * 
     * @return an identity-based credential for the given role and permissions.
     * 
     * @require role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
     * @require permissions.isFrozen() : "The permissions are frozen.";
     * @require permissions.isNotEmpty() : "The permissions are not empty.";
     * 
     * @ensure return.isActive() : "The returned credential is active.";
     */
    public static @Nonnull ClientCredential getIdentityBased(@Nonnull Role role, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException, IOException, PacketException, ExternalException {
        assert role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
        assert permissions.isFrozen() : "The permissions are frozen.";
        assert permissions.isNotEmpty() : "The permissions are not empty.";
        
        @Nullable ConcurrentMap<ReadonlyAgentPermissions, ClientCredential> map = identityBasedCredentials.get(role);
        if (map == null) map = identityBasedCredentials.putIfAbsentElseReturnPresent(role, new ConcurrentHashMap<ReadonlyAgentPermissions, ClientCredential>());
        @Nullable ClientCredential credential = map.get(permissions);
        
        if (credential == null || !credential.isActive()) {
            final @Nonnull RandomizedAgentPermissions randomizedPermissions = new RandomizedAgentPermissions(permissions);
            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions).sendNotNull();
            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getCredential(randomizedPermissions));
        }
        
        return credential;
    }
    
    
    /**
     * Caches the attribute-based client credentials given their role, value and permissions.
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<Pair<CertifiedAttributeValue, ReadonlyAgentPermissions>, ClientCredential>> attributeBasedCredentials = new ConcurrentHashMap<Role, ConcurrentMap<Pair<CertifiedAttributeValue, ReadonlyAgentPermissions>, ClientCredential>>();
    
    public static @Nonnull ClientCredential getAttributeBased(@Nonnull Role role, @Nonnull CertifiedAttributeValue value, @Nonnull ReadonlyAgentPermissions permissions) throws SQLException, IOException, PacketException, ExternalException {
        assert role.getIdentity() instanceof InternalPerson : "The role belongs to an internal person.";
        assert permissions.isFrozen() : "The permissions are frozen.";
        assert permissions.isNotEmpty() : "The permissions are not empty.";
        
        // TODO: Shortening with CredentialShorteningQuery.
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * Removes the credentials of the given role.
     * 
     * @param role the role whose credentials are to be removed.
     */
    public static void remove(@Nonnull Role role) {
        roleBasedCredentials.remove(role);
        identityBasedCredentials.remove(role);
        attributeBasedCredentials.remove(role);
    }
    
}
