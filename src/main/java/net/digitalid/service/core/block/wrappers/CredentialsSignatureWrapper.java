package net.digitalid.service.core.block.wrappers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.action.synchronizer.Audit;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.concepts.agent.AgentModule;
import net.digitalid.service.core.concepts.agent.OutgoingRole;
import net.digitalid.service.core.concepts.agent.RandomizedAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.concepts.attribute.CertifiedAttributeValue;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.cryptography.Element;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.Parameters;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.cryptography.credential.ClientCredential;
import net.digitalid.service.core.cryptography.credential.Credential;
import net.digitalid.service.core.cryptography.credential.HostCredential;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.HostEntity;
import net.digitalid.service.core.entity.NonHostAccount;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.RoleModule;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidOperationException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.exceptions.external.signature.ExpiredCredentialsSignatureException;
import net.digitalid.service.core.exceptions.external.signature.InactiveAuthenticationException;
import net.digitalid.service.core.exceptions.external.signature.InactiveCredentialException;
import net.digitalid.service.core.exceptions.external.signature.InvalidCredentialsSignatureException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestErrorCode;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.AttributeType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.utility.system.logger.Log;

/**
 * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code signature@core.digitalid.net} that is signed with credentials.
 * <p>
 * Format:<br>
 * - {@code credentialsSignature = (t, su, v, sv, credentials, certificates, f', sb')}<br>
 * - {@code credentials = [(o, randomizedPermissions, c, se, sb, i, si, (wi, swi, wb, swb))]}<br>
 * - {@code certificates = [HostSignatureWrapper]}
 * <p>
 * Use cases of credentials:<br>
 * - Identity-based requests: The restrictions are disclosed (in v) if the issuer and the subject are the same or the role needs to be shortened.<br>
 * - Attribute-based requests: The value v is the hash of the anonymous identity's identifier and is never disclosed.<br>
 * <p>
 * The randomized permissions are always disclosed unless a commitment is given for certificate shortening.
 */
@Immutable
public final class CredentialsSignatureWrapper extends SignatureWrapper {
    
    /* -------------------------------------------------- Implementation -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code t.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType T = SemanticType.map("t.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code su.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SU = SemanticType.map("su.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code sv.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SV = SemanticType.map("sv.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code f_prime.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType F_PRIME = SemanticType.map("f_prime.credentials.signature@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code sb_prime.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SB_PRIME = SemanticType.map("sb_prime.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    
    /**
     * Stores the semantic type {@code c.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType C = SemanticType.map("c.credential.credentials.signature@core.digitalid.net").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code se.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SE = SemanticType.map("se.credential.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code sb.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SB = SemanticType.map("sb.credential.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code i.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType I = SemanticType.map("i.credential.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code si.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SI = SemanticType.map("si.credential.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    
    /**
     * Stores the semantic type {@code wi.ve.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType WI = SemanticType.map("wi.ve.credential.credentials.signature@core.digitalid.net").load(PublicKey.VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code swi.ve.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SWI = SemanticType.map("swi.ve.credential.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code wb.ve.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType WB = SemanticType.map("wb.ve.credential.credentials.signature@core.digitalid.net").load(PublicKey.VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code swb.ve.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SWB = SemanticType.map("swb.ve.credential.credentials.signature@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code ve.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ENCRYPTION = SemanticType.map("ve.credential.credentials.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, WI, SWI, WB, SWB);
    
    
    /**
     * Stores the semantic type {@code credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CREDENTIAL = SemanticType.map("credential.credentials.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Credential.EXPOSED, RandomizedAgentPermissions.TYPE, C, SE, SB, I, SI, ENCRYPTION);
    
    /**
     * Stores the semantic type {@code list.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CREDENTIALS = SemanticType.map("list.credential.credentials.signature@core.digitalid.net").load(ListWrapper.XDF_TYPE, CREDENTIAL);
    
    /**
     * Stores the semantic type {@code credentials.signature@core.digitalid.net}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.map("credentials.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, T, SU, Restrictions.TYPE, SV, CREDENTIALS, AttributeValue.LIST, F_PRIME, SB_PRIME);
    
    /* -------------------------------------------------- Credentials -------------------------------------------------- */
    
    /**
     * Returns whether the given credentials are valid.
     * The credentials must fulfill the following criteria:<br>
     * - {@code !credentials.isEmpty()} - at least one credential needs to be provided.<br>
     * - {@code for (credential : credentials) credential != null} - none of the credentials may be null.<br>
     * - {@code !credential.isIdentityBased() || credentials.size() == 1} - if one of the credentials is identity-based, no other credential may be provided.<br>
     * - {@code (for (credential : credentials) credential instanceof ClientCredential) || (for (credential : credentials) credential instanceof HostCredential)} - either all credentials are client credentials or host credentials.<br>
     * - {@code for (credential : credentials) credential instanceof HostCredential || clientCredential.getU().equals(u)} - on the client-side, all credentials must have the same value u (the client secret).<br>
     * - {@code for (credential : credentials) credential instanceof HostCredential || clientCredential.getV().equals(v)} - on the client-side, all credentials must have the same value v (the hashed identity).
     * 
     * @param credentials the credentials with which the element is signed.
     * 
     * @return whether the given credentials are valid.
     */
    @Pure
    public static boolean areValid(@Nonnull ReadOnlyList<Credential> credentials) {
        if (credentials.isEmpty()) { return false; }
        final @Nonnull Iterator<Credential> iterator = credentials.iterator();
        @Nullable Credential credential = iterator.next();
        if (credential == null) { return false; }
        if (credential.isIdentityBased()) {
            if (credentials.size() > 1) { return false; }
        } else {
            final boolean client = credential instanceof ClientCredential;
            @Nullable Exponent u = null;
            @Nullable Exponent v = null;
            if (client) {
                final @Nonnull ClientCredential clientCredential = (ClientCredential) credential;
                u = clientCredential.getU();
                v = clientCredential.getV();
            }
            while (iterator.hasNext()) {
                credential = iterator.next();
                if (credential == null) { return false; }
                if (credential.isIdentityBased()) { return false; }
                if (client) {
                    if (!(credential instanceof ClientCredential)) { return false; }
                    final @Nonnull ClientCredential clientCredential = (ClientCredential) credential;
                    if (!clientCredential.getU().equals(u) || !clientCredential.getV().equals(v)) { return false; }
                } else {
                    if (!(credential instanceof HostCredential)) { return false; }
                }
            }
        }
        return true;
    }
    
    /**
     * Stores the credentials with which the element is signed.
     */
    private final @Nonnull @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<Credential> credentials;
    
    /**
     * Returns the credentials with which the element is signed.
     * 
     * @return the credentials with which the element is signed.
     */
    @Pure
    public @Nonnull @Frozen @Validated ReadOnlyList<Credential> getCredentials() {
        return credentials;
    }
    
    /**
     * Returns whether none of the credentials is used only once.
     * 
     * @return whether none of the credentials is used only once.
     */
    @Pure
    public boolean hasNoOneTimeCredential() {
        for (final @Nonnull Credential credential : credentials) {
            if (credential instanceof ClientCredential) {
                if (((ClientCredential) credential).isOneTime()) { return false; }
            } else {
                if (credential.getI() != null) { return false; }
            }
        }
        return true;
    }
    
    /* -------------------------------------------------- Certificates -------------------------------------------------- */
    
    /**
     * Returns whether the given certificates are valid (given the given credentials).
     * The certificates must fulfill the following criteria if they are not null:<br>
     * - {@code credentials.size() == 1} - exactly one credential is provided.<br>
     * - {@code credential.isIdentityBased()} - the only credential is identity-based.<br>
     * - {@code for (certificate : certificates) certificate != null} - none of the certificates is null.<br>
     * - {@code for (certificate : certificates) certificate.getSubject().equals(credential.getIssuer())} - the subject of each certificate matches the issuer of the credential.
     * 
     * @param certificates the certificates that are appended to the signature.
     * @param credentials the credentials with which the element is signed.
     * 
     * @return whether the given certificates are valid (given the given credentials).
     */
    public static boolean areValid(@Nullable ReadOnlyList<CertifiedAttributeValue> certificates, @Nonnull @Validated ReadOnlyList<Credential> credentials) {
        assert areValid(credentials) : "The credentials have to be valid.";
        
        if (certificates != null) {
            if (credentials.size() != 1) { return false; }
            final @Nonnull Credential credential = credentials.getNonNullable(0);
            if (credential.isAttributeBased()) { return false; }
            final @Nonnull InternalNonHostIdentity issuer = credential.getIssuer();
            for (final @Nullable CertifiedAttributeValue certificate : certificates) {
                if (certificate == null || !certificate.getSubject().equals(issuer)) { return false; }
            }
        }
        return true;
    }
    
    /**
     * Stores the certificates that are appended to an identity-based authentication with a single credential.
     */
    private final @Nullable @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<CertifiedAttributeValue> certificates;
    
    /**
     * Returns the certificates that are appended to an identity-based authentication.
     * 
     * @return the certificates that are appended to an identity-based authentication.
     */
    @Pure
    public @Nullable @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<CertifiedAttributeValue> getCertificates() {
        return certificates;
    }
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    /**
     * Stores whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     */
    private final boolean lodged;
    
    /**
     * Returns whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * 
     * @return whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     */
    @Pure
    public boolean isLodged() {
        return lodged;
    }
    
    /**
     * Checks whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     */
    @Pure
    public void checkIsLogded() throws RequestException {
        if (!isLodged()) { throw new RequestException(RequestErrorCode.SIGNATURE, "The credentials signature has to be lodged."); }
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     */
    private final @Nullable BigInteger value;
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     * 
     * @return either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     */
    @Pure
    public @Nullable BigInteger getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */
    
    /**
     * Stores the public key of the receiving host or null if the credentials are not shortened.
     * 
     * @invariant (publicKey == null) == (value == null) : "The public key is null if and only if the value is null.";
     */
    private final @Nullable PublicKey publicKey;
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new credentials signature wrapper with the given parameters.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param credentials the credentials with which the element is signed.
     * @param certificates the certificates that are appended to an identity-based authentication.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * 
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    @Locked
    @NonCommitting
    private CredentialsSignatureWrapper(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable Block element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<Credential> credentials, @Nullable @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<CertifiedAttributeValue> certificates, boolean lodged, @Nullable BigInteger value) throws DatabaseException, RequestException, ExternalException, NetworkException {
        super(type, element, subject, audit);
        
        assert credentials.isFrozen() : "The credentials are frozen.";
        assert CredentialsSignatureWrapper.areValid(credentials) : "The credentials are valid.";
        assert certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
        assert areValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
        
        this.credentials = credentials;
        this.certificates = certificates;
        this.lodged = lodged;
        this.value = value;
        this.publicKey = value == null ? null : Cache.getPublicKey(subject.getHostIdentifier(), getNonNullableTime());
    }
    
    /**
     * Creates a new credentials signature wrapper from the given blocks.
     * (Only to be called by {@link SignatureWrapper#decodeWithoutVerifying(ch.xdf.Block, boolean, net.digitalid.service.core.entity.Entity)}.)
     * 
     * @param block the block that contains the signed element.
     * @param credentialsSignature the signature to be decoded.
     * @param verified whether the signature is already verified.
     * @param entity the entity that decodes the signature or null.
     */
    @Locked
    @NonCommitting
    CredentialsSignatureWrapper(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, @Nonnull @NonEncoding @BasedOn("credentials.signature@core.digitalid.net") Block credentialsSignature, boolean verified, @Nullable Entity entity) throws DatabaseException, RequestException, ExternalException, NetworkException {
        super(block, verified);
        
        assert credentialsSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(credentialsSignature);
        
        // Restrictions
        final @Nullable Restrictions restrictions;
        final @Nullable Block restrictionsBlock = tuple.getNullableElement(2);
        if (restrictionsBlock != null) {
            if (entity == null) { throw InvalidOperationException.get("The restrictions of a credentials signature cannot be decoded without an entity."); }
            final @Nonnull NonHostEntity nonHostEntity;
            if (entity instanceof HostEntity) {
                final @Nonnull Host host = ((HostEntity) entity).getHost();
                final @Nonnull InternalIdentifier subject = getNonNullableSubject();
                final @Nonnull InternalPerson person = subject.getIdentity().castTo(InternalPerson.class);
                // If the subject is hosted on the given host, the entity is recreated for that subject.
                if (host.getIdentifier().equals(subject.getHostIdentifier())) {
                    nonHostEntity = NonHostAccount.get(host, person);
                // Otherwise, the context structure is accessed through a role of the corresponding client.
                } else {
                    nonHostEntity = RoleModule.getRole(host.getClient(), person);
                }
            } else {
                nonHostEntity = (NonHostEntity) entity;
            }
            restrictions = Restrictions.XDF_CONVERTER.decodeNonNullable(nonHostEntity, restrictionsBlock);
        } else {
            restrictions = null;
        }
        
        // Credentials
        @Nonnull ReadOnlyList<Block> list = ListWrapper.decodeNonNullableElements(tuple.getNonNullableElement(4));
        final @Nonnull FreezableList<Credential> credentials = FreezableArrayList.getWithCapacity(list.size());
        boolean lodged = false;
        for (final @Nonnull Block element : list) {
            final @Nonnull TupleWrapper subtuple = TupleWrapper.decode(element);
            final @Nonnull HostCredential credential = new HostCredential(subtuple.getNonNullableElement(0), subtuple.getNullableElement(1), restrictions, subtuple.getNullableElement(5));
            credentials.add(credential);
            if (!subtuple.isElementNull(7)) { lodged = true; }
        }
        this.credentials = credentials.freeze();
        this.lodged = lodged;
        if (!CredentialsSignatureWrapper.areValid(credentials)) { throw InvalidParameterValueException.get("credentials", credentials); }
        
        // Certificates
        if (!tuple.isElementNull(5)) {
            list = ListWrapper.decodeNonNullableElements(tuple.getNonNullableElement(5));
            final @Nonnull FreezableList<CertifiedAttributeValue> certificates = FreezableArrayList.getWithCapacity(list.size());
            for (final @Nonnull Block element : list) { certificates.add(AttributeValue.get(element, verified).castTo(CertifiedAttributeValue.class)); }
            this.certificates = certificates.freeze();
        } else {
            this.certificates = null;
        }
        if (!areValid(certificates, credentials)) { throw InvalidParameterValueCombinationException.get("The certificates do not match the credentials of the signature."); }
        
        // Value and public key
        this.value = IntegerWrapper.decodeNullable(tuple.getNullableElement(6));
        this.publicKey = value == null ? null : Cache.getPublicKey(getNonNullableSubject().getHostIdentifier(), getNonNullableTime());
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Encodes the element with a new credentials signature wrapper and signs it according to the arguments.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param credentials the credentials with which the element is signed.
     * @param certificates the certificates that are appended to an identity-based authentication.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * 
     * @require element == null || element.getFactory().getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @ensure return.isVerified() : "The returned signature is verified.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull <E extends XDF<E, ?>> CredentialsSignatureWrapper sign(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable E element, @Nonnull InternalIdentifier subject, @Nullable Audit audit, @Nonnull @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<Credential> credentials, @Nullable @NonNullableElements @NonEmpty @Frozen @Validated ReadOnlyList<CertifiedAttributeValue> certificates, boolean lodged, @Nullable BigInteger value) throws DatabaseException, RequestException, ExternalException, NetworkException {
        return new CredentialsSignatureWrapper(type, ConvertToXDF.nullable(element), subject, audit, credentials, certificates, lodged, value);
    }
    
    /* -------------------------------------------------- Checks -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        if (!super.isSignedLike(signature)) { return false; }
        final @Nonnull CredentialsSignatureWrapper other = (CredentialsSignatureWrapper) signature;
        if (this.credentials.size() != other.credentials.size()) { return false; }
        for (int i = 0; i < credentials.size(); i++) {
            if (!this.credentials.getNonNullable(i).isSimilarTo(other.credentials.getNonNullable(i))) { return false; }
        }
        return Objects.equals(this.certificates, other.certificates) && this.lodged == other.lodged && Objects.equals(this.value, other.value);
    }
    
    @Pure
    @Override
    public void checkRecency() throws InactiveAuthenticationException {
        super.checkRecency();
        final @Nonnull Time time = Time.HOUR.ago();
        for (final @Nonnull Credential credential : credentials) {
            if (credential.getIssuance().isLessThan(time)) { throw InactiveCredentialException.get(credential); }
        }
    }
    
    /* -------------------------------------------------- Based -------------------------------------------------- */
    
    /**
     * Returns whether this authentication is identity-based.
     * 
     * @return whether this authentication is identity-based.
     */
    @Pure
    public boolean isIdentityBased() {
        return credentials.getNonNullable(0).isIdentityBased();
    }
    
    /**
     * Returns whether this authentication is attribute-based.
     * 
     * @return whether this authentication is attribute-based.
     */
    @Pure
    public boolean isAttributeBased() {
        return credentials.getNonNullable(0).isAttributeBased();
    }
    
    /**
     * Returns whether this authentication is role-based.
     * 
     * @return whether this authentication is role-based.
     */
    @Pure
    public boolean isRoleBased() {
        return credentials.getNonNullable(0).isRoleBased();
    }
    
    /* -------------------------------------------------- Issuer -------------------------------------------------- */
    
    /**
     * Returns the issuer of the first and only credential.
     * 
     * @return the issuer of the first and only credential.
     * 
     * @require isIdentityBased() : "The authentication is identity-based.";
     * @require !isRoleBased() : "The authentication is not role-based.";
     */
    @Pure
    public @Nonnull InternalPerson getIssuer() {
        assert isIdentityBased() : "The authentication is identity-based.";
        assert !isRoleBased() : "The authentication is not role-based.";
        
        return (InternalPerson) credentials.getNonNullable(0).getIssuer();
    }
    
    /**
     * Checks whether the first and only credential was issued by the given internal person and throws a {@link RequestException} if not.
     * 
     * @param issuer the issuer to check.
     */
    @Pure
    public void checkIssuer(@Nonnull InternalPerson issuer) throws RequestException {
        if (!isIdentityBased() || isRoleBased() || !issuer.equals(getIssuer())) { throw new RequestException(RequestErrorCode.AUTHORIZATION, "The credential was not issued by " + issuer.getAddress() + "."); }
    }
    
    /* -------------------------------------------------- Attribute Content -------------------------------------------------- */
    
    /**
     * Returns the attribute content with the given type from the credentials and certificates or null if no such attribute can be found.
     * 
     * @param type the semantic type of the attribute content which is to be returned.
     * 
     * @return the attribute content with the given type from the credentials and certificates or null if no such attribute can be found.
     * 
     * @ensure return == null || return.getType().equals(type) : "The returned block is either null or has the given type.";
     */
    @Pure
    public @Nullable Block getAttributeContent(@Nonnull @AttributeType SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        if (isAttributeBased()) {
            for (final @Nonnull Credential credential : credentials) {
                if (credential.getIssuer().equals(type)) {
                    final @Nonnull Block block = credential.getAttributeContentNotNull();
                    if (!block.getType().equals(type)) { return null; }
                    return block;
                }
            }
        } else if (certificates != null) {
            for (final @Nonnull CertifiedAttributeValue certificate : certificates) {
                if (certificate.getContent().getType().equals(type)) { return certificate.getContent(); }
            }
        }
        return null;
    }
    
    /* -------------------------------------------------- Covering -------------------------------------------------- */
    
    /**
     * Returns whether each credential allows to read the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether each credential allows to read the given type.
     */
    @Pure
    public boolean canRead(@Nonnull @AttributeType SemanticType type) {
        for (final @Nonnull Credential credential : credentials) {
            final @Nullable ReadOnlyAgentPermissions permissions = credential.getPermissions();
            if (permissions == null || !permissions.canRead(type)) { return false; }
        }
        return true;
    }
    
    /**
     * Checks whether each credential allows to read the given type and throws a {@link RequestException} if not.
     * 
     * @param type the semantic type to check.
     */
    @Pure
    public void checkCanRead(@Nonnull @AttributeType SemanticType type) throws RequestException {
        if (!canRead(type)) { throw new RequestException(RequestErrorCode.AUTHORIZATION, "Not all credentials can read " + type.getAddress() + "."); }
    }
    
    /**
     * Returns whether each credential allows to write the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether each credential allows to write the given type.
     */
    @Pure
    public boolean canWrite(@Nonnull @AttributeType SemanticType type) {
        for (final @Nonnull Credential credential : credentials) {
            final @Nullable ReadOnlyAgentPermissions permissions = credential.getPermissions();
            if (permissions == null || !permissions.canWrite(type)) { return false; }
        }
        return true;
    }
    
    /**
     * Checks whether each credential allows to write the given type and throws a {@link RequestException} if not.
     * 
     * @param type the semantic type to check.
     */
    @Pure
    public void checkCanWrite(@Nonnull @AttributeType SemanticType type) throws RequestException {
        if (!canWrite(type)) { throw new RequestException(RequestErrorCode.AUTHORIZATION, "Not all credentials can write " + type.getAddress() + "."); }
    }
    
    /**
     * Returns whether the permissions of each credential cover the given permissions.
     * 
     * @param permissions the permissions that needs to be covered.
     * 
     * @return whether the permissions of each credential cover the given permissions.
     */
    @Pure
    public boolean cover(@Nonnull ReadOnlyAgentPermissions permissions) {
        for (final @Nonnull Credential credential : credentials) {
            final @Nullable ReadOnlyAgentPermissions _permissions = credential.getPermissions();
            if (_permissions == null || !_permissions.cover(permissions)) { return false; }
        }
        return true;
    }
    
    /**
     * Checks whether the permissions of each credential cover the given permissions and throws a {@link RequestException} if not.
     * 
     * @param permissions the permissions that need to be covered.
     */
    @Pure
    public void checkCover(@Nonnull ReadOnlyAgentPermissions permissions) throws RequestException {
        if (!cover(permissions)) { throw new RequestException(RequestErrorCode.AUTHORIZATION, "Not all credentials cover " + permissions + "."); }
    }
    
    /* -------------------------------------------------- Verifying -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code twi.array.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TWI = SemanticType.map("twi.array.credential.credentials.signature@core.digitalid.net").load(PublicKey.VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code twb.array.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TWB = SemanticType.map("twb.array.credential.credentials.signature@core.digitalid.net").load(PublicKey.VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code array.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ARRAY = SemanticType.map("array.credential.credentials.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Element.TYPE, TWI, TWB);
    
    /**
     * Stores the semantic type {@code list.array.credential.credentials.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ARRAYS = SemanticType.map("list.array.credential.credentials.signature@core.digitalid.net").load(ListWrapper.XDF_TYPE, ARRAY);
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public void verify() throws DatabaseException, RequestException, ExternalException, NetworkException {
        assert !isVerified() : "This signature is not verified.";
        
        final @Nonnull Time start = Time.getCurrent();
        
        if (getNonNullableTime().isLessThan(Time.TROPICAL_YEAR.ago())) { throw ExpiredCredentialsSignatureException.get(this); }
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(getCache());
        final @Nonnull BigInteger hash = tuple.getNonNullableElement(0).getHash();
        
        final @Nonnull TupleWrapper signature = TupleWrapper.decode(tuple.getNonNullableElement(3));
        final @Nonnull Exponent t = Exponent.get(signature.getNonNullableElement(0));
        final @Nonnull Exponent su = Exponent.get(signature.getNonNullableElement(1));
        if (su.getBitLength() > Parameters.RANDOM_EXPONENT) { throw InvalidCredentialsSignatureException.get(this, "su"); }
        
        @Nullable Exponent v = null, sv = null;
        if (signature.isElementNull(2)) {
            sv = Exponent.get(signature.getNonNullableElement(3));
            if (sv.getBitLength() > Parameters.RANDOM_EXPONENT) { throw InvalidCredentialsSignatureException.get(this, "sv"); }
        } else {
            v = Exponent.get(signature.getNonNullableElement(2).getHash());
        }
        
        final @Nonnull ReadOnlyList<Block> list = ListWrapper.decodeNonNullableElements(signature.getNonNullableElement(4));
        final @Nonnull FreezableList<Block> ts = FreezableArrayList.getWithCapacity(list.size());
        for (int i = 0; i < list.size(); i++) {
            final @Nonnull PublicKey publicKey = credentials.getNonNullable(i).getPublicKey();
            final @Nonnull Exponent o = credentials.getNonNullable(i).getO();
            final @Nonnull TupleWrapper credential = TupleWrapper.decode(list.getNonNullable(i));
            final @Nonnull Element c = publicKey.getCompositeGroup().getElement(credential.getNonNullableElement(2));
            
            final @Nonnull Exponent se = Exponent.get(credential.getNonNullableElement(3));
            if (se.getBitLength() > Parameters.RANDOM_CREDENTIAL_EXPONENT) { throw InvalidCredentialsSignatureException.get(this, "se"); }
            final @Nonnull Exponent sb = Exponent.get(credential.getNonNullableElement(4));
            if (sb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT + 1) { throw InvalidCredentialsSignatureException.get(this, "sb"); }
            
            @Nonnull Element hiddenElement = c.pow(se).multiply(publicKey.getAb().pow(sb)).multiply(publicKey.getAu().pow(su));
            @Nonnull Element shownElement = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            @Nullable Exponent si = null;
            if (credential.isElementNull(5)) {
                si = Exponent.get(credential.getNonNullableElement(6));
                if (si.getBitLength() > Parameters.RANDOM_EXPONENT) { throw InvalidCredentialsSignatureException.get(this, "si"); }
                hiddenElement = hiddenElement.multiply(publicKey.getAi().pow(si));
            } else {
                shownElement = publicKey.getAi().pow(Exponent.get(credential.getNonNullableElement(5)));
            }
            
            if (v == null) {
                assert sv != null : "The value sv cannot be null if v is null (see code above).";
                hiddenElement = hiddenElement.multiply(publicKey.getAv().pow(sv));
            } else {
                shownElement = shownElement.multiply(publicKey.getAv().pow(v));
            }
            
            shownElement = shownElement.inverse().multiply(publicKey.getAo().pow(o));
            
            final @Nonnull FreezableArray<Block> array = FreezableArray.get(3);
            array.set(0, ConvertToXDF.nonNullable(hiddenElement.multiply(shownElement.pow(t))));
            
            if (lodged && si != null) {
                final @Nonnull ReadOnlyArray<Block> encryptions = TupleWrapper.decode(credential.getNonNullableElement(7)).getNonNullableElements(4);
                final @Nonnull FreezableArray<Block> wis = TupleWrapper.decode(encryptions.getNonNullable(0)).getNonNullableElements(2).clone();
                final @Nonnull Exponent swi = Exponent.get(encryptions.getNonNullable(1));
                if (swi.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT) { throw InvalidCredentialsSignatureException.get(this, "swi"); }
                final @Nonnull FreezableArray<Block> wbs = TupleWrapper.decode(encryptions.getNonNullable(2)).getNonNullableElements(2).clone();
                final @Nonnull Exponent swb = Exponent.get(encryptions.getNonNullable(3));
                if (swb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT) { throw InvalidCredentialsSignatureException.get(this, "swb"); }
                
                wis.set(0, ConvertToXDF.nonNullable(PublicKey.W1, publicKey.getY().pow(swi).multiply(publicKey.getZPlus1().pow(si)).multiply(publicKey.getSquareGroup().getElement(wis.getNonNullable(0)).pow(t))));
                wis.set(1, ConvertToXDF.nonNullable(PublicKey.W2, publicKey.getG().pow(swi).multiply(publicKey.getSquareGroup().getElement(wis.getNonNullable(1)).pow(t))));
                
                wbs.set(0, ConvertToXDF.nonNullable(PublicKey.W1, publicKey.getY().pow(swb).multiply(publicKey.getZPlus1().pow(sb)).multiply(publicKey.getSquareGroup().getElement(wbs.getNonNullable(0)).pow(t))));
                wbs.set(1, ConvertToXDF.nonNullable(PublicKey.W2, publicKey.getG().pow(swb).multiply(publicKey.getSquareGroup().getElement(wbs.getNonNullable(1)).pow(t))));
                
                array.set(1, TupleWrapper.encode(TWI, wis.freeze()));
                array.set(2, TupleWrapper.encode(TWB, wbs.freeze()));
            }
            
            ts.add(i, TupleWrapper.encode(ARRAY, array.freeze()));
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        if (value != null) {
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            final @Nonnull Exponent sb = Exponent.get(signature.getNonNullableElement(7));
            
            @Nonnull Element element = publicKey.getAu().pow(su).multiply(publicKey.getAb().pow(sb));
            if (sv != null) { element = element.multiply(publicKey.getAv().pow(sv)); }
            tf = ConvertToXDF.nonNullable(publicKey.getCompositeGroup().getElement(value).pow(t).multiply(element)).getHash();
        }
        
        if (!t.getValue().equals(hash.xor(ListWrapper.encode(ARRAYS, ts.freeze()).getHash()).xor(tf))) { throw InvalidCredentialsSignatureException.get(this, null); }
        
        if (certificates != null) {
            for (final @Nonnull CertifiedAttributeValue certificate : certificates) {
                certificate.verify();
                certificate.checkIsValid(getNonNullableTime());
            }
        }
        
        Log.verbose("Signature verified in " + start.ago().getValue() + " ms.");
        
        setVerified();
    }
    
    /* -------------------------------------------------- Signing -------------------------------------------------- */
    
    @Override
    void sign(@Nonnull @NonFrozen FreezableArray<Block> elements) {
        final @Nonnull Time start = Time.getCurrent();
        
        assert credentials.getNonNullable(0) instanceof ClientCredential : "The first credential is a client credential (like all others).";
        final @Nonnull ClientCredential mainCredential = (ClientCredential) credentials.getNonNullable(0);
        final @Nonnull Exponent u = mainCredential.getU();
        final @Nonnull Exponent v = mainCredential.getV();
        
        final @Nonnull SecureRandom random = new SecureRandom();
        final @Nonnull Exponent ru = Exponent.get(new BigInteger(Parameters.RANDOM_EXPONENT, random));
        final @Nullable Exponent rv = mainCredential.getRestrictions() != null && (mainCredential.getIssuer().getAddress().equals(getNonNullableSubject()) || mainCredential.isRoleBased()) ? null : Exponent.get(new BigInteger(Parameters.RANDOM_EXPONENT, random));
        
        final int size = credentials.size();
        final @Nonnull ClientCredential[] randomizedCredentials = new ClientCredential[size];
        
        final @Nonnull Exponent[] res = new Exponent[size];
        final @Nonnull Exponent[] rbs = new Exponent[size];
        final @Nonnull Exponent[] ris = new Exponent[size];
        
        final @Nonnull Exponent[] rwis = new Exponent[size];
        final @Nonnull Exponent[] rwbs = new Exponent[size];
        
        final @Nonnull Block[] wis = new Block[size];
        final @Nonnull Block[] wbs = new Block[size];
        
        final @Nonnull Exponent[] rrwis = new Exponent[size];
        final @Nonnull Exponent[] rrwbs = new Exponent[size];
        
        final @Nonnull FreezableList<Block> ts = FreezableArrayList.getWithCapacity(size);
        for (int i = 0; i < size; i++) {
            assert credentials.getNonNullable(i) instanceof ClientCredential : "All credentials have to be client credentials, which was already checked in the constructor.";
            randomizedCredentials[i] = ((ClientCredential) credentials.getNonNullable(i)).getRandomizedCredential();
            final @Nonnull PublicKey publicKey = randomizedCredentials[i].getPublicKey();
            @Nonnull Element element = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            res[i] = Exponent.get(new BigInteger(Parameters.RANDOM_CREDENTIAL_EXPONENT, random));
            rbs[i] = Exponent.get(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
            
            if (!randomizedCredentials[i].isOneTime()) {
                ris[i] = Exponent.get(new BigInteger(Parameters.RANDOM_EXPONENT, random));
                element = element.multiply(publicKey.getAi().pow(ris[i]));
            }
            
            if (rv != null) { element = element.multiply(publicKey.getAv().pow(rv)); }
            
            final @Nonnull FreezableArray<Block> array = FreezableArray.get(3);
            array.set(0, ConvertToXDF.nonNullable(randomizedCredentials[i].getC().pow(res[i]).multiply(publicKey.getAb().pow(rbs[i])).multiply(publicKey.getAu().pow(ru)).multiply(element)));
            
            if (lodged && !randomizedCredentials[i].isOneTime()) {
                rwis[i] = Exponent.get(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
                rwbs[i] = Exponent.get(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
                
                wis[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getI(), rwis[i]).setType(WI);
                wbs[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getB(), rwbs[i]).setType(WB);
                
                rrwis[i] = Exponent.get(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
                rrwbs[i] = Exponent.get(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
                
                array.set(1, publicKey.getVerifiableEncryption(ris[i], rrwis[i]).setType(TWI));
                array.set(2, publicKey.getVerifiableEncryption(rbs[i], rrwbs[i]).setType(TWB));
            }
            
            ts.add(i, TupleWrapper.encode(ARRAY, array.freeze()));
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        @Nullable Element f = null;
        @Nullable Exponent rb = null;
        if (value != null) {
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            rb = Exponent.get(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
            
            f = publicKey.getAu().pow(ru).multiply(publicKey.getAb().pow(rb));
            if (rv != null) { f = f.multiply(publicKey.getAv().pow(rv)); }
            tf = ConvertToXDF.nonNullable(f).getHash();
            
            f = publicKey.getAu().pow(u).multiply(publicKey.getAb().pow(value));
            if (rv != null) { f = f.multiply(publicKey.getAv().pow(v)); }
        }
        
        final @Nonnull Exponent t = Exponent.get(elements.getNonNullable(0).getHash().xor(ListWrapper.encode(ARRAYS, ts.freeze()).getHash()).xor(tf));
        
        final @Nonnull FreezableArray<Block> signature = FreezableArray.get(8);
        signature.set(0, ConvertToXDF.nonNullable(T, t));
        signature.set(1, ConvertToXDF.nonNullable(SU, ru.subtract(t.multiply(u))));
        if (rv != null) {
            signature.set(3, ConvertToXDF.nonNullable(SV, rv.subtract(t.multiply(v))));
        } else {
            signature.set(2, Restrictions.XDF_CONVERTER.encodeNonNullable(mainCredential.getRestrictionsNotNull()));
        }
        
        final @Nonnull FreezableList<Block> list = FreezableArrayList.getWithCapacity(size);
        for (int i = 0; i < size; i++) {
            final @Nonnull FreezableArray<Block> credential = FreezableArray.get(8);
            credential.set(0, randomizedCredentials[i].getExposed());
            if (value == null || mainCredential.isRoleBased()) {
                credential.set(1, randomizedCredentials[i].getRandomizedPermissions().toBlock());
            }
            credential.set(2, ConvertToXDF.nonNullable(C, randomizedCredentials[i].getC()));
            credential.set(3, ConvertToXDF.nonNullable(SE, res[i].subtract(t.multiply(randomizedCredentials[i].getE()))));
            credential.set(4, ConvertToXDF.nonNullable(SB, rbs[i].subtract(t.multiply(randomizedCredentials[i].getB()))));
            if (randomizedCredentials[i].isOneTime()) {
                credential.set(5, ConvertToXDF.nonNullable(I, randomizedCredentials[i].getI()));
            } else {
                credential.set(6, ConvertToXDF.nonNullable(SI, ris[i].subtract(t.multiply(randomizedCredentials[i].getI()))));
                
                if (lodged) {
                    final @Nonnull Block swi = ConvertToXDF.nonNullable(SWI, rrwis[i].subtract(t.multiply(rwis[i])));
                    final @Nonnull Block swb = ConvertToXDF.nonNullable(SWB, rrwbs[i].subtract(t.multiply(rwbs[i])));
                    credential.set(7, TupleWrapper.encode(ENCRYPTION, wis[i], swi, wbs[i], swb));
                }
            }
            list.add(i, TupleWrapper.encode(CREDENTIAL, credential.freeze()));
        }
        signature.set(4, ListWrapper.encode(CREDENTIALS, list.freeze()));
        
        if (certificates != null) {
            final @Nonnull FreezableList<Block> certificateList = FreezableArrayList.getWithCapacity(certificates.size());
            for (final @Nonnull CertifiedAttributeValue certificate : certificates) { certificateList.add(certificate.toBlock()); }
            signature.set(5, ListWrapper.encode(AttributeValue.LIST, certificateList.freeze()));
        }
        
        if (value != null) {
            assert f != null && rb != null : "If the credential is shortened, f and rb are not null (see the code above).";
            signature.set(6, ConvertToXDF.nonNullable(F_PRIME, f));
            signature.set(7, ConvertToXDF.nonNullable(SB_PRIME, rb.subtract(t.multiply(Exponent.get(value)))));
        }
        
        elements.set(3, TupleWrapper.encode(SIGNATURE, signature.freeze()));
        
        Log.verbose("Element signed in " + start.ago().getValue() + " ms.");
    }
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nullable OutgoingRole getAgent(@Nonnull NonHostEntity entity) throws DatabaseException {
        final @Nonnull Credential credential = getCredentials().getNonNullable(0);
        return credential.isRoleBased() ? AgentModule.getOutgoingRole(entity, credential.getRoleNotNull(), false) : null;
    }
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull OutgoingRole getAgentCheckedAndRestricted(@Nonnull NonHostEntity entity, @Nullable PublicKey publicKey) throws DatabaseException, RequestException {
        final @Nonnull Credential credential = getCredentials().getNonNullable(0);
        if (credential.isRoleBased()) {
            final @Nullable OutgoingRole outgoingRole = AgentModule.getOutgoingRole(entity, credential.getRoleNotNull(), true);
            if (outgoingRole != null && outgoingRole.getContext().contains(Contact.get(entity, (Person) credential.getIssuer()))) {
                outgoingRole.checkCovers(credential);
                outgoingRole.restrictTo(credential);
                outgoingRole.checkNotRemoved();
                return outgoingRole;
            }
        }
        throw new RequestException(RequestErrorCode.AUTHORIZATION, "The credential does not belong to an authorized role.");
    }
    
}
