package ch.xdf;

import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Cache;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.contact.Contact;
import ch.virtualid.credential.ClientCredential;
import ch.virtualid.credential.Credential;
import ch.virtualid.credential.HostCredential;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PublicKey;
import static ch.virtualid.cryptography.PublicKey.VERIFIABLE_ENCRYPTION;
import static ch.virtualid.cryptography.PublicKey.W1;
import static ch.virtualid.cryptography.PublicKey.W2;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InactiveSignatureException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.module.both.Agents;
import ch.virtualid.packet.Audit;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} that is signed with credentials.
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
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class CredentialsSignatureWrapper extends SignatureWrapper implements Immutable {
    
    /**
     * Stores the semantic type {@code t.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType T = SemanticType.create("t.credentials.signature@virtualid.ch").load(HashWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code su.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SU = SemanticType.create("su.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code sv.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SV = SemanticType.create("sv.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code f_prime.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType F_PRIME = SemanticType.create("f_prime.credentials.signature@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code sb_prime.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SB_PRIME = SemanticType.create("sb_prime.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    
    /**
     * Stores the semantic type {@code c.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType C = SemanticType.create("c.credential.credentials.signature@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code se.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SE = SemanticType.create("se.credential.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code sb.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SB = SemanticType.create("sb.credential.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code i.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType I = SemanticType.create("i.credential.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code si.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SI = SemanticType.create("si.credential.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    
    /**
     * Stores the semantic type {@code wi.encryption.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType WI = SemanticType.create("wi.encryption.credential.credentials.signature@virtualid.ch").load(VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code swi.encryption.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SWI = SemanticType.create("swi.encryption.credential.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code wb.encryption.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType WB = SemanticType.create("wb.encryption.credential.credentials.signature@virtualid.ch").load(VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code swb.encryption.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SWB = SemanticType.create("swb.encryption.credential.credentials.signature@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code encryption.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ENCRYPTION = SemanticType.create("encryption.credential.credentials.signature@virtualid.ch").load(TupleWrapper.TYPE, WI, SWI, WB, SWB);
    
    
    /**
     * Stores the semantic type {@code credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CREDENTIAL = SemanticType.create("credential.credentials.signature@virtualid.ch").load(ListWrapper.TYPE, Credential.EXPOSED, RandomizedAgentPermissions.TYPE, C, SE, SB, I, SI, ENCRYPTION);
    
    /**
     * Stores the semantic type {@code list.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CREDENTIALS = SemanticType.create("list.credential.credentials.signature@virtualid.ch").load(ListWrapper.TYPE, CREDENTIAL);
    
    /**
     * Stores the semantic type {@code credentials.signature@virtualid.ch}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.create("credentials.signature@virtualid.ch").load(TupleWrapper.TYPE, T, SU, Restrictions.TYPE, SV, CREDENTIALS, Certificate.LIST, F_PRIME, SB_PRIME);
    
    
    /**
     * Stores the credentials with which the element is signed.
     * 
     * @invariant credentials.isFrozen() : "The credentials are frozen.";
     * @invariant credentialsAreValid(credentials) : "The credentials are valid.";
     */
    private final @Nonnull ReadonlyList<Credential> credentials;
    
    /**
     * Stores the certificates that are appended to an identity-based authentication with a single credential.
     * 
     * @invariant certificates.isFrozen() : "The certificates are frozen.";
     * @invariant certificatesAreValid(certificates, credentials) : "The certificates are valid.";
     */
    private final @Nullable ReadonlyList<HostSignatureWrapper> certificates;
    
    /**
     * Stores whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     */
    private final boolean lodged;
    
    /**
     * Stores either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     */
    private final @Nullable BigInteger value;
    
    /**
     * Stores the public key of the receiving host or null if the credentials are not shortened.
     * 
     * @invariant (publicKey == null) == (value == null) : "The public key is null if and only if the value is null.";
     */
    private final @Nullable PublicKey publicKey;
    
    /**
     * Encodes the element into a new block and signs it according to the arguments for clients.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param credentials the credentials with which the element is signed.
     * @param certificates the certificates that are appended to an identity-based authentication.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @require credentials.isFrozen() : "The credentials are frozen.";
     * @require credentialsAreValid(credentials) : "The credentials are valid.";
     * @require certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
     * @require certificatesAreValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
     */
    public CredentialsSignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws SQLException, IOException, PacketException, ExternalException {
        super(type, element, subject, audit);
        
        assert credentials.isFrozen() : "The credentials are frozen.";
        assert credentialsAreValid(credentials) : "The credentials are valid.";
        assert certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
        assert certificatesAreValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
        
        this.credentials = credentials;
        this.certificates = certificates;
        this.lodged = lodged;
        this.value = value;
        this.publicKey = value == null ? null : Cache.getPublicKey(subject.getHostIdentifier().getIdentity(), getTimeNotNull());
    }
    
    /**
     * Encodes the element into a new block and signs it according to the arguments for clients.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param credentials the credentials with which the element is signed.
     * @param certificates the certificates that are appended to an identity-based authentication.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @require credentials.isFrozen() : "The credentials are frozen.";
     * @require credentialsAreValid(credentials) : "The credentials are valid.";
     * @require certificates == null || certificates.isFrozen() : "The certificates are either null or frozen.";
     * @require certificatesAreValid(certificates, credentials) : "The certificates are valid (given the given credentials).";
     */
    public CredentialsSignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws SQLException, IOException, PacketException, ExternalException {
        this(type, Block.toBlock(element), subject, audit, credentials, certificates, lodged, value);
    }
    
    /**
     * Wraps the given block and decodes the given signature for hosts.
     * (Only to be called by {@link SignatureWrapper#decodeUnverified(ch.xdf.Block)}.)
     * 
     * @param block the block to be wrapped.
     * @param credentialsSignature the signature to be decoded.
     * @param entity the entity that decodes the signature or null.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     * @require credentialsSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    CredentialsSignatureWrapper(final @Nonnull Block block, final @Nonnull Block credentialsSignature, @Nullable Entity entity) throws SQLException, IOException, PacketException, ExternalException {
        super(block);
        
        assert credentialsSignature.getType().isBasedOn(SIGNATURE) : "The signature is based on the implementation type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(credentialsSignature);
        
        // Restrictions
        final @Nullable Restrictions restrictions;
        final @Nullable Block restrictionsBlock = tuple.getElement(2);
        if (restrictionsBlock != null) {
            if (entity == null) throw new InvalidEncodingException("The restrictions of a credentials signature cannot be decoded without an entity.");
            if (entity instanceof Account) {
                final @Nonnull Host host = ((Account) entity).getHost();
                final @Nonnull Identifier subject = getSubjectNotNull();
                final @Nonnull Person person = subject.getIdentity().toPerson();
                // If the subject is hosted on the given host, the entity is recreated for that subject.
                if (host.getIdentifier().equals(subject.getHostIdentifier())) {
                    entity = new Account(host, person);
                // Otherwise, the context structure is accessed through a role of the corresponding client.
                } else {
                    entity = Role.get(host.getClient(), person);
                }
            }
            restrictions = new Restrictions(entity, restrictionsBlock);
        } else {
            restrictions = null;
        }
        
        // Credentials
        @Nonnull ReadonlyList<Block> list = new ListWrapper(tuple.getElementNotNull(4)).getElementsNotNull();
        final @Nonnull FreezableList<Credential> credentials = new FreezableArrayList<Credential>(list.size());
        boolean lodged = false;
        for (final @Nonnull Block element : list) {
            final @Nonnull TupleWrapper subtuple = new TupleWrapper(element);
            final @Nonnull HostCredential credential = new HostCredential(subtuple.getElementNotNull(0), tuple.getElement(1), restrictions, tuple.getElement(5));
            credentials.add(credential);
            if (tuple.isElementNotNull(7)) lodged = true;
        }
        this.credentials = credentials.freeze();
        this.lodged = lodged;
        if (!credentialsAreValid(credentials)) throw new InvalidEncodingException("The credentials of the signature are invalid.");
        
        // Certificates
        if (tuple.isElementNotNull(5)) {
            list = new ListWrapper(tuple.getElementNotNull(5)).getElementsNotNull();
            final @Nonnull FreezableList<HostSignatureWrapper> certificates = new FreezableArrayList<HostSignatureWrapper>(list.size());
            for (final @Nonnull Block element : list) {
                final @Nonnull SignatureWrapper certificate = SignatureWrapper.decodeUnverified(element, null);
                if (certificate instanceof HostSignatureWrapper) certificates.add((HostSignatureWrapper) certificate);
                else throw new InvalidEncodingException("An appended certificate is not signed by a host.");
            }
            this.certificates = certificates.freeze();
        } else {
            this.certificates = null;
        }
        if (!certificatesAreValid(certificates, credentials)) throw new InvalidEncodingException("The certificates do not match the credentials of the signature.");
        
        // Value and public key
        this.value = tuple.isElementNull(6) ? null : new IntegerWrapper(tuple.getElementNotNull(6)).getValue();
        this.publicKey = tuple.isElementNull(6) ? null : Cache.getPublicKey(getSubjectNotNull().getHostIdentifier().getIdentity(), getTimeNotNull());
    }
    
    
    /**
     * Returns whether the given credentials are valid.
     * The credentials must fulfill the following criteria:<br>
     * - {@code !credentials.isEmpty()} - at least one credential needs to be provided.<br>
     * - {@code (forall credential : credentials) != null} - none of the credentials may be null.<br>
     * - {@code !credential.isIdentityBased() || credentials.size() == 1} - if one of the credentials is identity-based, no other credential may be provided.<br>
     * - {@code (forall credential : credentials) instanceof ClientCredential || (forall credential : credentials) instanceof HostCredential} - either all credentials are client credentials or host credentials.<br>
     * - {@code (forall credential : credentials) instanceof HostCredential || clientCredential.getU().equals(u)} - on the client-side, all credentials must have the same value u (the client secret).<br>
     * - {@code (forall credential : credentials) instanceof HostCredential || clientCredential.getV().equals(v)} - on the client-side, all credentials must have the same value v (the hashed identity).
     * 
     * @param credentials the credentials with which the element is signed.
     * 
     * @return whether the given credentials are valid.
     */
    @Pure
    public static boolean credentialsAreValid(@Nonnull ReadonlyList<Credential> credentials) {
        if (credentials.isEmpty()) return false;
        final @Nonnull Iterator<Credential> iterator = credentials.iterator();
        @Nullable Credential credential = iterator.next();
        if (credential == null) return false;
        if (credential.isIdentityBased()) {
            if (credentials.size() > 1) return false;
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
                if (credential == null) return false;
                if (credential.isIdentityBased()) return false;
                if (client) {
                    if (!(credential instanceof ClientCredential)) return false;
                    final @Nonnull ClientCredential clientCredential = (ClientCredential) credential;
                    if (!clientCredential.getU().equals(u) || !clientCredential.getV().equals(v)) return false;
                } else {
                    if (!(credential instanceof HostCredential)) return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Returns whether the given certificates are valid (given the given credentials).
     * The certificates must fulfill the following criteria if they are not null:<br>
     * - {@code credentials.size() == 1 } - exactly one credential is provided.<br>
     * - {@code credential.isIdentityBased} - the only credential is identity-based.<br>
     * - {@code (forall certificate : certificates) != null} - none of the certificates is null.<br>
     * - {@code (forall certificate : certificates).isCertificate()} - each certificate is indeed a certificate.<br>
     * - {@code credential.getIssuer().equals((forall certificate : certificates).getSubjectNotNull())} - the subject of each certificate matches the issuer of the credential.
     * 
     * @param certificates the certificates that are appended to the signature.
     * @param credentials the credentials with which the element is signed.
     * 
     * @return whether the given certificates are valid (given the given credentials).
     * 
     * @require validCredentials(credentials) : "The credentials have to be valid.";
     */
    public static boolean certificatesAreValid(@Nullable ReadonlyList<HostSignatureWrapper> certificates, @Nonnull ReadonlyList<Credential> credentials) {
        assert credentialsAreValid(credentials) : "The credentials have to be valid.";
        
        if (certificates != null) {
            if (credentials.size() != 1) return false;
            final @Nonnull Credential credential = credentials.getNotNull(0);
            if (credential.isAttributeBased()) return false;
            final @Nonnull Identifier issuer = credential.getIssuer().getAddress();
            for (final @Nullable HostSignatureWrapper certificate : certificates) {
                if (certificate == null || !issuer.equals(certificate.getSubjectNotNull()) || !certificate.isCertificate()) return false;
            }
        }
        return true;
    }
    
    
    /**
     * Return the credentials with which the element is signed.
     * 
     * @return the credentials with which the element is signed.
     * 
     * @ensure return.isFrozen() : "The credentials are frozen.";
     * @ensure credentialsAreValid(return) : "The credentials are valid.";
     */
    @Pure
    public @Nonnull ReadonlyList<Credential> getCredentials() {
        return credentials;
    }
    
    /**
     * Return the certificates that are appended to an identity-based authentication.
     * 
     * @return the certificates that are appended to an identity-based authentication.
     * 
     * @ensure return.isFrozen() : "The certificates are frozen.";
     * @ensure certificatesAreValid(return, getCredentials()) : "The certificates are valid.";
     */
    @Pure
    public @Nullable ReadonlyList<HostSignatureWrapper> getCertificates() {
        return certificates;
    }
    
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
     * Returns either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     * 
     * @return either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     */
    @Pure
    public @Nullable BigInteger getValue() {
        return value;
    }
    
    
    @Pure
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        if (!super.isSignedLike(signature)) return false;
        final @Nonnull CredentialsSignatureWrapper other = (CredentialsSignatureWrapper) signature;
        if (this.credentials.size() != other.credentials.size()) return false;
        for (int i = 0; i < credentials.size(); i++) {
            if (!this.credentials.getNotNull(i).isSimilarTo(other.credentials.getNotNull(i))) return false;
        }
        return Objects.equals(this.certificates, other.certificates) && this.lodged == other.lodged && Objects.equals(this.value, other.value);
    }
    
    @Pure
    @Override
    public void checkRecency() throws InactiveSignatureException {
        super.checkRecency();
        final @Nonnull Time time = Time.HOUR.ago();
        for (final @Nonnull Credential credential : credentials) {
            if (credential.getIssuance().isLessThan(time)) throw new InactiveSignatureException("One of the credentials is older than an hour.");
        }
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
                if (((ClientCredential) credential).isOneTime()) return false;
            } else {
                if (credential.getI() != null) return false;
            }
        }
        return true;
    }
    
    
    /**
     * Returns whether this authentication is identity-based.
     * 
     * @return whether this authentication is identity-based.
     */
    @Pure
    public boolean isIdentityBased() {
        return credentials.getNotNull(0).isIdentityBased();
    }
    
    /**
     * Returns whether this authentication is attribute-based.
     * 
     * @return whether this authentication is attribute-based.
     */
    @Pure
    public boolean isAttributeBased() {
        return credentials.getNotNull(0).isAttributeBased();
    }
    
    /**
     * Returns whether this authentication is role-based.
     * 
     * @return whether this authentication is role-based.
     */
    @Pure
    public boolean isRoleBased() {
        return credentials.getNotNull(0).isRoleBased();
    }
    
    
    /**
     * Returns the issuer of the first and only credential.
     * 
     * @return the issuer of the first and only credential.
     * 
     * @require isIdentityBased() : "The authentication is identity-based.";
     * @require !isRoleBased() : "The authentication is not role-based.";
     */
    @Pure
    public @Nonnull Person getIssuer() {
        assert isIdentityBased() : "The authentication is identity-based.";
        assert !isRoleBased(): "The authentication is not role-based.";
        
        return (Person) credentials.getNotNull(0).getIssuer();
    }
    
    /**
     * Checks whether the first and only credential was issued by the given person and throws a {@link PacketException} if not.
     * 
     * @param person the person to check.
     */
    @Pure
    public void checkIssuer(@Nonnull Person issuer) throws PacketException {
        if (!isIdentityBased() || isRoleBased() || !issuer.equals(getIssuer())) throw new PacketException(PacketError.AUTHORIZATION, "The credential was not issued by " + issuer.getAddress() + ".");
    }
    
    
    /**
     * Returns the attribute with the given type from the credentials and certificates or null if no such attribute can be found.
     * 
     * @param type the semantic type of the attribute which is to be returned.
     * 
     * @return the attribute with the given type from the credentials and certificates or null if no such attribute can be found.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    @Pure
    public @Nullable Block getAttribute(@Nonnull SemanticType type) throws SQLException, IOException, PacketException, ExternalException {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        if (isAttributeBased()) {
            for (final @Nonnull Credential credential : credentials) {
                if (credential.getIssuer().equals(type)) {
                    final @Nonnull Block block = new SelfcontainedWrapper(credential.getAttributeNotNull()).getElement();
                    if (!block.getType().equals(type)) throw new InvalidEncodingException("The type of the attribute does not match the issuer.");
                    return block;
                }
            }
        } else {
            for (final @Nonnull HostSignatureWrapper certificate : certificates) {
                final @Nonnull Block block = new SelfcontainedWrapper(certificate.getElementNotNull()).getElement();
                if (block.getType().equals(type)) return block;
            }
        }
        return null;
    }
    
    
    /**
     * Returns whether each credential allows to read the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether each credential allows to read the given type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public boolean canRead(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        for (final @Nonnull Credential credential : credentials) {
            final @Nullable ReadonlyAgentPermissions permissions = credential.getPermissions();
            if (permissions == null || !permissions.canRead(type)) return false;
        }
        return true;
    }
    
    /**
     * Checks whether each credential allows to read the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkRead(@Nonnull SemanticType type) throws PacketException {
        if (!canRead(type)) throw new PacketException(PacketError.AUTHORIZATION, "Not all credentials can read " + type.getAddress() + ".");
    }
    
    /**
     * Returns whether each credential allows to write the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether each credential allows to write the given type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public boolean canWrite(@Nonnull SemanticType type) {
        assert type.isAttributeType() : "The type is an attribute type.";
        
        for (final @Nonnull Credential credential : credentials) {
            final @Nullable ReadonlyAgentPermissions permissions = credential.getPermissions();
            if (permissions == null || !permissions.canWrite(type)) return false;
        }
        return true;
    }
    
    /**
     * Checks whether each credential allows to write the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkWrite(@Nonnull SemanticType type) throws PacketException {
        if (!canWrite(type)) throw new PacketException(PacketError.AUTHORIZATION, "Not all credentials can write " + type.getAddress() + ".");
    }
    
    /**
     * Returns whether the permissions of each credential covers the given permissions.
     * 
     * @param permissions the permissions that needs to be covered.
     * 
     * @return whether the permissions of each credential covers the given permissions.
     */
    @Pure
    public boolean cover(@Nonnull ReadonlyAgentPermissions permissions) {
        for (final @Nonnull Credential credential : credentials) {
            final @Nullable ReadonlyAgentPermissions _permissions = credential.getPermissions();
            if (_permissions == null || !_permissions.cover(permissions)) return false;
        }
        return true;
    }
    
    /**
     * Checks whether the permissions of each credential covers the given permissions and throws a {@link PacketException} if not.
     * 
     * @param permissions the permissions that need to be covered.
     */
    @Pure
    public void checkCover(@Nonnull ReadonlyAgentPermissions permissions) throws PacketException {
        if (!cover(permissions)) throw new PacketException(PacketError.AUTHORIZATION, "Not all credentials cover " + permissions + ".");
    }
    
    
    /**
     * Stores the semantic type {@code twi.array.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TWI = SemanticType.create("twi.array.credential.credentials.signature@virtualid.ch").load(VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code twb.array.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TWB = SemanticType.create("twb.array.credential.credentials.signature@virtualid.ch").load(VERIFIABLE_ENCRYPTION);
    
    /**
     * Stores the semantic type {@code array.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ARRAY = SemanticType.create("array.credential.credentials.signature@virtualid.ch").load(ListWrapper.TYPE, Element.TYPE, TWI, TWB);
    
    /**
     * Stores the semantic type {@code list.array.credential.credentials.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ARRAYS = SemanticType.create("list.array.credential.credentials.signature@virtualid.ch").load(ListWrapper.TYPE, ARRAY);
    
    @Pure
    @Override
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        if (getTimeNotNull().isLessThan(Time.TROPICAL_YEAR.ago())) throw new InvalidSignatureException("The credentials signature is out of date.");
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(getCache());
        final @Nonnull BigInteger hash = tuple.getElementNotNull(0).getHash();
        
        final @Nonnull TupleWrapper signature = new TupleWrapper(tuple.getElementNotNull(3));
        final @Nonnull Exponent t = new Exponent(signature.getElementNotNull(0));
        final @Nonnull Exponent su = new Exponent(signature.getElementNotNull(1));
        if (su.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The credentials signature is invalid: The value su is too big.");
        
        @Nullable Exponent v = null, sv = null;
        if (signature.isElementNull(2)) {
            sv = new Exponent(signature.getElementNotNull(3));
            if (sv.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The credentials signature is invalid: The value sv is too big.");
        } else {
            v = new Exponent(signature.getElementNotNull(2).getHash());
        }
        
        final @Nonnull ReadonlyList<Block> list = new ListWrapper(signature.getElementNotNull(4)).getElementsNotNull();
        final @Nonnull FreezableList<Block> ts = new FreezableArrayList<Block>(list.size());
        for (int i = 0; i < list.size(); i++) {
            final @Nonnull PublicKey publicKey = credentials.getNotNull(i).getPublicKey();
            final @Nonnull Exponent o = credentials.getNotNull(i).getO();
            final @Nonnull TupleWrapper credential = new TupleWrapper(list.getNotNull(i));
            final @Nonnull Element c = publicKey.getCompositeGroup().getElement(credential.getElementNotNull(2));
            
            final @Nonnull Exponent se = new Exponent(credential.getElementNotNull(3));
            if (se.getBitLength() > Parameters.RANDOM_CREDENTIAL_EXPONENT) throw new InvalidSignatureException("The credentials signature is invalid: The value se is too big.");
            final @Nonnull Exponent sb = new Exponent(credential.getElementNotNull(4));
            if (sb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT + 1) throw new InvalidSignatureException("The credentials signature is invalid: The value sb is too big.");
            
            @Nonnull Element hiddenElement = c.pow(se).multiply(publicKey.getAb().pow(sb)).multiply(publicKey.getAu().pow(su));
            @Nonnull Element shownElement = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            @Nullable Exponent si = null;
            if (credential.isElementNull(5)) {
                si = new Exponent(credential.getElementNotNull(6));
                if (si.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The credentials signature is invalid: The value si is too big.");
                hiddenElement = hiddenElement.multiply(publicKey.getAi().pow(si));
            } else {
                shownElement = publicKey.getAi().pow(new Exponent(credential.getElementNotNull(5)));
            }
            
            if (v == null) {
                assert sv != null : "The value sv cannot be null if v is null (see code above).";
                hiddenElement = hiddenElement.multiply(publicKey.getAv().pow(sv));
            } else {
                shownElement = shownElement.multiply(publicKey.getAv().pow(v));
            }
            
            shownElement = shownElement.inverse().multiply(publicKey.getAo().pow(o));
            
            final @Nonnull FreezableArray<Block> array = new FreezableArray<Block>(3);
            array.set(0, hiddenElement.multiply(shownElement.pow(t)).toBlock());
            
            if (lodged && si != null) {
                final @Nonnull ReadonlyArray<Block> encryptions = new TupleWrapper(credential.getElementNotNull(7)).getElementsNotNull(4);
                final @Nonnull FreezableArray<Block> wis = new TupleWrapper(encryptions.getNotNull(0)).getElementsNotNull(2).clone();
                final @Nonnull Exponent swi = new Exponent(encryptions.getNotNull(1));
                if (swi.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT) throw new InvalidSignatureException("The credentials signature is invalid: The value swi is too big.");
                final @Nonnull FreezableArray<Block> wbs = new TupleWrapper(encryptions.getNotNull(2)).getElementsNotNull(2).clone();
                final @Nonnull Exponent swb = new Exponent(encryptions.getNotNull(3));
                if (swb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT) throw new InvalidSignatureException("The credentials signature is invalid: The value swb is too big.");
                
                wis.set(0, publicKey.getY().pow(swi).multiply(publicKey.getZPlus1().pow(si)).multiply(publicKey.getSquareGroup().getElement(wis.getNotNull(0)).pow(t)).toBlock().setType(W1));
                wis.set(1, publicKey.getG().pow(swi).multiply(publicKey.getSquareGroup().getElement(wis.getNotNull(1)).pow(t)).toBlock().setType(W2));
                
                wbs.set(0, publicKey.getY().pow(swb).multiply(publicKey.getZPlus1().pow(sb)).multiply(publicKey.getSquareGroup().getElement(wbs.getNotNull(0)).pow(t)).toBlock().setType(W1));
                wbs.set(1, publicKey.getG().pow(swb).multiply(publicKey.getSquareGroup().getElement(wbs.getNotNull(1)).pow(t)).toBlock().setType(W2));
                
                array.set(1, new TupleWrapper(TWI, wis.freeze()).toBlock());
                array.set(2, new TupleWrapper(TWB, wbs.freeze()).toBlock());
            }
            
            ts.add(i, new TupleWrapper(ARRAY, array.freeze()).toBlock());
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        if (value != null) {
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            final @Nonnull Exponent sb = new Exponent(signature.getElementNotNull(7));
            
            @Nonnull Element element = publicKey.getAu().pow(su).multiply(publicKey.getAb().pow(sb));
            if (sv != null) element = element.multiply(publicKey.getAv().pow(sv));
            tf = publicKey.getCompositeGroup().getElement(value).pow(t).multiply(element).toBlock().getHash();
        }
        
        if (!t.getValue().equals(hash.xor(new ListWrapper(ARRAYS, ts).toBlock().getHash()).xor(tf))) throw new InvalidSignatureException("The credentials signature is invalid: The value t is not correct.");
        
        if (certificates != null) {
            for (final @Nonnull HostSignatureWrapper certificate : certificates) certificate.verifyAsCertificate();
        }
    }
    
    @Override
    protected void sign(@Nonnull FreezableArray<Block> elements, @Nonnull BigInteger hash) {
        assert credentials.get(0) instanceof ClientCredential : "The first credential is a client credential (like all others).";
        final @Nonnull ClientCredential mainCredential = (ClientCredential) credentials.getNotNull(0);
        final @Nonnull Exponent u = mainCredential.getU();
        final @Nonnull Exponent v = mainCredential.getV();
        
        final @Nonnull SecureRandom random = new SecureRandom();
        final @Nonnull Exponent ru = new Exponent(new BigInteger(Parameters.RANDOM_EXPONENT, random));
        final @Nullable Exponent rv = mainCredential.getRestrictions() != null && (mainCredential.getIssuer().getAddress().equals(getSubjectNotNull()) || mainCredential.isRoleBased()) ? null : new Exponent(new BigInteger(Parameters.RANDOM_EXPONENT, random));
        
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
        
        final @Nonnull FreezableList<Block> ts = new FreezableArrayList<Block>(size);
        for (int i = 0; i < size; i++) {
            assert credentials.getNotNull(i) instanceof ClientCredential : "All credentials have to be client credentials, which was already checked in the constructor.";
            randomizedCredentials[i] = ((ClientCredential) credentials.getNotNull(i)).getRandomizedCredential();
            final @Nonnull PublicKey publicKey = randomizedCredentials[i].getPublicKey();
            @Nonnull Element element = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            res[i] = new Exponent(new BigInteger(Parameters.RANDOM_CREDENTIAL_EXPONENT, random));
            rbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
            
            if (!randomizedCredentials[i].isOneTime()) {
                ris[i] = new Exponent(new BigInteger(Parameters.RANDOM_EXPONENT, random));
                element = element.multiply(publicKey.getAi().pow(ris[i]));
            }
            
            if (rv != null) element = element.multiply(publicKey.getAv().pow(rv));
            
            final @Nonnull FreezableArray<Block> array = new FreezableArray<Block>(3);
            array.set(0, randomizedCredentials[i].getC().pow(res[i]).multiply(publicKey.getAb().pow(rbs[i])).multiply(publicKey.getAu().pow(ru)).multiply(element).toBlock());
            
            if (lodged && !randomizedCredentials[i].isOneTime()) {
                rwis[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
                rwbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
                
                wis[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getI(), rwis[i]).setType(WI);
                wbs[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getB(), rwbs[i]).setType(WB);
                
                rrwis[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
                rrwbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
                
                array.set(1, publicKey.getVerifiableEncryption(ris[i], rrwis[i]).setType(TWI));
                array.set(2, publicKey.getVerifiableEncryption(rbs[i], rrwbs[i]).setType(TWB));
            }
            
            ts.add(i, new TupleWrapper(ARRAY, array.freeze()).toBlock());
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        @Nullable Element f = null;
        @Nullable Exponent rb = null;
        if (value != null) {
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            rb = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
            
            f = publicKey.getAu().pow(ru).multiply(publicKey.getAb().pow(rb));
            if (rv != null) f = f.multiply(publicKey.getAv().pow(rv));
            tf = f.toBlock().getHash();
            
            f = publicKey.getAu().pow(u).multiply(publicKey.getAb().pow(value));
            if (rv != null) f = f.multiply(publicKey.getAv().pow(v));
        }
        
        final @Nonnull Exponent t = new Exponent(hash.xor(new ListWrapper(ARRAYS, ts.freeze()).toBlock().getHash()).xor(tf));
        
        final @Nonnull FreezableArray<Block> signature = new FreezableArray<Block>(8);
        signature.set(0, t.toBlock().setType(T));
        signature.set(1, ru.subtract(t.multiply(u)).toBlock().setType(SU));
        if (rv != null) {
            signature.set(3, rv.subtract(t.multiply(v)).toBlock().setType(SV));
        } else {
            signature.set(2, mainCredential.getRestrictionsNotNull().toBlock());
        }
        
        final @Nonnull FreezableList<Block> list = new FreezableArrayList<Block>(size);
        for (int i = 0; i < size; i++) {
            final @Nonnull FreezableArray<Block> credential = new FreezableArray<Block>(8);
            credential.set(0, randomizedCredentials[i].getExposed());
            if (value == null || mainCredential.isRoleBased()) {
                credential.set(1, randomizedCredentials[i].getRandomizedPermissions().toBlock());
            }
            credential.set(2, randomizedCredentials[i].getC().toBlock().setType(C));
            credential.set(3, res[i].subtract(t.multiply(randomizedCredentials[i].getE())).toBlock().setType(SE));
            credential.set(4, rbs[i].subtract(t.multiply(randomizedCredentials[i].getB())).toBlock().setType(SB));
            if (randomizedCredentials[i].isOneTime()) {
                credential.set(5, randomizedCredentials[i].getI().toBlock().setType(I));
            } else {
                credential.set(6, ris[i].subtract(t.multiply(randomizedCredentials[i].getI())).toBlock().setType(SI));
                
                if (lodged) {
                    final @Nonnull Block swi = rrwis[i].subtract(t.multiply(rwis[i])).toBlock().setType(SWI);
                    final @Nonnull Block swb = rrwbs[i].subtract(t.multiply(rwbs[i])).toBlock().setType(SWB);
                    credential.set(7, new TupleWrapper(ENCRYPTION, new FreezableArray<Block>(wis[i], swi, wbs[i], swb).freeze()).toBlock());
                }
            }
            list.add(i, new TupleWrapper(CREDENTIAL, credential.freeze()).toBlock());
        }
        signature.set(4, new ListWrapper(CREDENTIALS, list.freeze()).toBlock());
        
        if (certificates != null) {
            final @Nonnull FreezableList<Block> certificateList = new FreezableArrayList<Block>(certificates.size());
            for (final @Nonnull HostSignatureWrapper certificate : certificates) {
                certificateList.add(certificate.toBlock().setType(Certificate.TYPE));
            }
            signature.set(5, new ListWrapper(Certificate.LIST, certificateList.freeze()).toBlock());
        }
        
        if (value != null) {
            assert f != null && rb != null : "If the credential is shortened, f and rb are not null (see the code above).";
            signature.set(6, f.toBlock().setType(F_PRIME));
            signature.set(7, rb.subtract(t.multiply(new Exponent(value))).toBlock().setType(SB_PRIME));
        }
        
        elements.set(3, new TupleWrapper(SIGNATURE, signature.freeze()).toBlock());
    }
    
    
    @Pure
    @Override
    public @Nullable OutgoingRole getAgent(@Nonnull Entity entity) throws SQLException {
        final @Nonnull Credential credential = getCredentials().getNotNull(0);
        return credential.isRoleBased() ? Agents.getOutgoingRole(entity, credential.getRoleNotNull(), false) : null;
    }
    
    @Pure
    @Override
    public @Nonnull OutgoingRole getAgentCheckedAndRestricted(@Nonnull Entity entity) throws SQLException, PacketException {
        final @Nonnull Credential credential = getCredentials().getNotNull(0);
        if (credential.isRoleBased()) {
            final @Nullable OutgoingRole outgoingRole = Agents.getOutgoingRole(entity, credential.getRoleNotNull(), true);
            if (outgoingRole != null && outgoingRole.getContext().contains(Contact.get(entity, (Person) credential.getIssuer()))) {
                outgoingRole.checkCovers(credential);
                outgoingRole.restrictTo(credential);
                outgoingRole.checkNotRemoved();
                return outgoingRole;
            }
        }
        throw new PacketException(PacketError.AUTHORIZATION, "The credential does not belong to an authorized role.");
    }
    
}
