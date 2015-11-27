package net.digitalid.service.core.cryptography.credential;

import java.math.BigInteger;
import java.security.SecureRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.concepts.agent.RandomizedAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.concepts.attribute.CertifiedAttributeValue;
import net.digitalid.service.core.cryptography.Element;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.Parameters;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.cryptography.credential.annotations.Active;
import net.digitalid.service.core.entity.NonNativeRole;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.entity.annotations.OfInternalPerson;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.signature.InvalidSignatureException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class models credentials on the client-side.
 */
@Immutable
public final class ClientCredential extends Credential {
    
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
     * @param role the role that is assumed or null in case no role is assumed.
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
     * @param attributeContent the attribute content for access control.
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
     * @param role the role that is assumed or null in case no role is assumed.
     * @param attributeContent the attribute content for access control.
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
        
        if (!publicKey.getAo().pow(getO()).equals(c.pow(e).multiply(publicKey.getAb().pow(b)).multiply(publicKey.getAu().pow(u)).multiply(publicKey.getAi().pow(i)).multiply(publicKey.getAv().pow(v)))) { throw new InvalidSignatureException("The credential issued by " + issuer.getAddress() + " is invalid."); }
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
            final @Nonnull Exponent r = Exponent.get(new BigInteger(Parameters.BLINDING_EXPONENT - Parameters.CREDENTIAL_EXPONENT, new SecureRandom()));
            return new ClientCredential(getPublicKey(), getIssuer(), getIssuance(), getRandomizedPermissions(), getRole(), getAttributeContent(), getRestrictions(), c.multiply(getPublicKey().getAb().pow(r)), e, b.subtract(e.multiply(r)), u, getI(), v, oneTime);
        } catch (@Nonnull InvalidSignatureException exception) {
            throw ShouldNeverHappenError.get("The randomization of a client credential should yield another valid client credential.", exception);
        }
    }
    
    
    /**
     * Caches the role-based client credentials given their role and permissions.
     */
    private static final @Nonnull ConcurrentMap<NonNativeRole, ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential>> roleBasedCredentials = new ConcurrentHashMap<>();
    
    /**
     * Returns a role-based credential for the given role and permissions.
     * 
     * @param role the role for which the credential is to be returned.
     * @param permissions the permissions which are to be contained.
     * 
     * @return a role-based credential for the given role and permissions.
     */
    @NonCommitting
    public static @Nonnull @Active ClientCredential getRoleBased(@Nonnull @OfInternalPerson NonNativeRole role, @Nonnull @Frozen @NonEmpty ReadOnlyAgentPermissions permissions) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        @Nullable ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential> map = roleBasedCredentials.get(role);
        if (map == null) { map = roleBasedCredentials.putIfAbsentElseReturnPresent(role, new ConcurrentHashMap<ReadOnlyAgentPermissions, ClientCredential>()); }
        @Nullable ClientCredential credential = map.get(permissions);
        
        if (credential == null || !credential.isActive()) {
            final @Nonnull RandomizedAgentPermissions randomizedPermissions = new RandomizedAgentPermissions(permissions);
            final @Nonnull BigInteger value = new BigInteger(Parameters.BLINDING_EXPONENT, new SecureRandom());
            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions, value).sendNotNull();
            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getInternalCredential(randomizedPermissions, role.getRelation(), value, role.getClient().getSecret()));
        }
        
        return credential;
    }
    
    
    /**
     * Caches the identity-based client credentials given their role and permissions.
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential>> identityBasedCredentials = new ConcurrentHashMap<>();
    
    /**
     * Returns an identity-based credential for the given role and permissions.
     * 
     * @param role the role for which the credential is to be returned.
     * @param permissions the permissions which are to be contained.
     * 
     * @return an identity-based credential for the given role and permissions.
     */
    @NonCommitting
    public static @Nonnull @Active ClientCredential getIdentityBased(@Nonnull @OfInternalPerson Role role, @Nonnull @Frozen @NonEmpty ReadOnlyAgentPermissions permissions) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        @Nullable ConcurrentMap<ReadOnlyAgentPermissions, ClientCredential> map = identityBasedCredentials.get(role);
        if (map == null) { map = identityBasedCredentials.putIfAbsentElseReturnPresent(role, new ConcurrentHashMap<ReadOnlyAgentPermissions, ClientCredential>()); }
        @Nullable ClientCredential credential = map.get(permissions);
        
        if (credential == null || !credential.isActive()) {
            final @Nonnull RandomizedAgentPermissions randomizedPermissions = new RandomizedAgentPermissions(permissions);
            final @Nonnull CredentialReply reply = new CredentialInternalQuery(role, randomizedPermissions).sendNotNull();
            credential = map.putIfAbsentElseReturnPresent(permissions, reply.getInternalCredential(randomizedPermissions, null, BigInteger.ZERO, role.getClient().getSecret()));
        }
        
        return credential;
    }
    
    
    /**
     * Caches the attribute-based client credentials given their role, value and permissions.
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentMap<ReadOnlyPair<CertifiedAttributeValue, ReadOnlyAgentPermissions>, ClientCredential>> attributeBasedCredentials = new ConcurrentHashMap<>();
    
    /**
     * Returns an attribute-based credential for the given role, value and permissions.
     * 
     * @param role the role for which the credential is to be returned.
     * @param value the certified attribute value which is to be shortened.
     * @param permissions the permissions which are to be contained.
     * 
     * @return an attribute-based credential for the given role, value and permissions.
     */
    @NonCommitting
    public static @Nonnull @Active ClientCredential getAttributeBased(@Nonnull @OfInternalPerson Role role, @Nonnull CertifiedAttributeValue value, @Nonnull @Frozen @NonEmpty ReadOnlyAgentPermissions permissions) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        // TODO: Shortening with CredentialExternalQuery.
        throw new UnsupportedOperationException("Credentials for attribute-based access control are not yet supported!");
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
