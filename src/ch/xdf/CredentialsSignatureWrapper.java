package ch.xdf;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.client.Client;
import ch.virtualid.credential.ClientCredential;
import ch.virtualid.credential.Credential;
import ch.virtualid.credential.HostCredential;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.exceptions.FailedEncodingException;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.exceptions.InvalidSignatureException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} that is signed with credentials.
 * <p>
 * Format:<br>
 * - {@code credentialsSignature = (t, su, v, sv, credentials, certificates, value, sb')}<br>
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
 * @version 1.0
 */
public final class CredentialsSignatureWrapper extends SignatureWrapper implements Immutable {
    
    /**
     * Stores the semantic type {@code credentials.signature@virtualid.ch}.
     */
    static final @Nonnull SemanticType SIGNATURE = SemanticType.create("credentials.signature@virtualid.ch").load(TupleWrapper.TYPE, todo);
    
    
    /**
     * Stores the credentials with which the element is signed.
     * 
     * @invariant credentials.isFrozen() : "The credentials are frozen.";
     */
    private final @Nonnull ReadonlyList<Credential> credentials;
    
    /**
     * Stores the certificates that are appended to an identity-based authentication with a single credential.
     * 
     * @invariant certificates.isFrozen() : "The certificates are frozen.";
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
     */
    private final @Nullable PublicKey publicKey;
    
    /**
     * Encodes the element into a new block and signs it according to the arguments for clients.
     * 
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param credentials the credentials with which the element is signed.
     * @param certificates the certificates that are appended to an identity-based authentication.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not to be shortened.
     * 
     * @require subject.exists() : "The given subject has to exist.";
     * @require credentials.isFrozen() : "The credentials have to be frozen.";
     * @require validCredentials(credentials) : "The credentials have to be valid.";
     * @require certificates == null || certificates.isFrozen() : "The certificates have to be frozen.";
     * @require certificatesAreValid(certificates, credentials) : "The certificates have to be valid (given the given credentials).";
     */
    public CredentialsSignatureWrapper(@Nonnull Block element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws FailedEncodingException {
        super(element, subject, audit);
        
        assert subject.exists() : "The given subject has to exist.";
        assert credentials.isFrozen() : "The credentials have to be frozen.";
        assert credentialsAreValid(credentials) : "The credentials have to be valid.";
        assert certificates == null || certificates.isFrozen() : "The certificates have to be frozen.";
        assert certificatesAreValid(certificates, credentials) : "The certificates have to be valid (given the given credentials).";
        
        this.credentials = credentials;
        this.certificates = certificates;
        this.lodged = lodged;
        this.value = value;
        
        if (value == null) {
            this.publicKey = null;
        } else {
            try {
                this.publicKey = new PublicKey(Client.getAttributeNotNullUnwrapped(subject.getHostIdentifier().getIdentity(), SemanticType.HOST_PUBLIC_KEY));
            } catch (@Nonnull IdFailedIdentityException InvalidEncodingException exception) {
                throw new FailedEncodingException("Could not sign the given element with credentials because the public key of the recipient could not be found.", exception);
            }
        }
    }
    
    /**
     * Encodes the element into a new block and signs it according to the arguments for clients.
     * 
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit shall be appended.
     * @param credentials the credentials with which the element is signed.
     * @param certificates the certificates that are appended to an identity-based authentication.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not shortened.
     * 
     * @require subject.exists() : "The given subject has to exist.";
     * @require credentials.isFrozen() : "The credentials have to be frozen.";
     * @require validCredentials(credentials) : "The credentials have to be valid.";
     * @require certificates == null || certificates.isFrozen() : "The certificates have to be frozen.";
     * @require certificatesAreValid(certificates, credentials) : "The certificates have to be valid (given the given credentials).";
     */
    public CredentialsSignatureWrapper(@Nonnull Blockable element, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws FailedEncodingException {
        this(element.toBlock(), subject, audit, credentials, certificates, lodged, value);
    }
    
    /**
     * Wraps the given block and decodes the given signature for hosts.
     * (Only to be called by {@link SignatureWrapper#decodeUnverified(ch.xdf.Block)}.)
     * 
     * @param block the block to be wrapped.
     * @param credentialsSignature the signature to be decoded.
     */
    CredentialsSignatureWrapper(@Nonnull Block block, @Nonnull Block credentialsSignature) throws InvalidEncodingException, FailedIdentityException {
        super(block, true);
        
        @Nonnull Block[] elements = new TupleWrapper(credentialsSignature).getElementsNotNull(7);
        @Nonnull Block restrictions = elements[2];
        @Nonnull List<Block> list = new ListWrapper(elements[4]).getElements();
        credentials = new ArrayList<Credential>(list.size());
        boolean lodged = false;
        for (@Nonnull Block element : list) {
            @Nonnull Block[] subelements = new TupleWrapper(element).getElementsNotNull(8);
            @Nonnull HostCredential hostCredential = new HostCredential(subelements[0], subelements[1], restrictions, subelements[5]);
            credentials.add(hostCredential);
            if (!subelements[7].isEmpty()) lodged = true;
        }
        this.lodged = lodged;
        
        if (!credentialsAreValid(credentials)) throw new InvalidEncodingException("The credentials of the signature have an invalid combination.");
        
        if (elements[5].isNotEmpty()) {
            list = new ListWrapper(elements[5]).getElements();
            certificates = new ArrayList<HostSignatureWrapper>(list.size());
            for (@Nonnull Block element : list) {
                @Nonnull SignatureWrapper certificate = SignatureWrapper.decodeUnverified(element);
                if (certificate instanceof HostSignatureWrapper) {
                    certificates.add((HostSignatureWrapper) certificate);
                } else {
                    throw new InvalidEncodingException("An appended certificate is not signed by a host.");
                }
            }
        } else {
            certificates = null;
        }
        
        if (!certificatesAreValid(certificates, credentials)) throw new InvalidEncodingException("The certificates do not match the credentials of the signature.");
        
        if (elements[6].isEmpty()) {
            value = null;
            publicKey = null;
        } else {
            value = new IntegerWrapper(elements[6]).getValue();
            @Nullable Identifier subject = getSubject();
            assert subject != null : "The subject of signed statements is never null.";
            publicKey = new PublicKey(Client.getAttributeNotNullUnwrapped(subject.getHostIdentifier().getIdentity(), SemanticType.HOST_PUBLIC_KEY));
        }
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
    public static boolean credentialsAreValid(@Nonnull List<Credential> credentials) {
        if (credentials.isEmpty()) return false;
        @Nonnull Iterator<Credential> iterator = credentials.iterator();
        @Nullable Credential credential = iterator.next();
        if (credential == null) return false;
        if (credential.isIdentityBased()) {
            if (credentials.size() > 1) return false;
        } else {
            boolean client = credential instanceof ClientCredential;
            @Nullable Exponent u = null;
            @Nullable Exponent v = null;
            if (client) {
                @Nonnull ClientCredential clientCredential = (ClientCredential) credential;
                u = clientCredential.getU();
                v = clientCredential.getV();
            }
            while (iterator.hasNext()) {
                credential = iterator.next();
                if (credential == null) return false;
                if (credential.isIdentityBased()) return false;
                if (client) {
                    if (!(credential instanceof ClientCredential)) return false;
                    @Nonnull ClientCredential clientCredential = (ClientCredential) credential;
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
     * The certificates must fulfill the following criteria:<br>
     * - {@code credentials.size() == 1 || certificates == null} - if more than one credential is provided, the certificates have to be null.<br>
     * - {@code certificates == null || credential.isIdentityBased} - if the certificates are not null, the only credential is identity-based.<br>
     * - {@code credential.getIssuer().equals((forall certificate : certificates).getSubject())} - the subject of each certificate has to match the issuer of the credential.
     * 
     * @param certificates the certificates that are appended to the signature.
     * @param credentials the credentials with which the element is signed.
     * 
     * @return whether the given certificates are valid (given the given credentials).
     * 
     * @require validCredentials(credentials) : "The credentials have to be valid.";
     */
    public static boolean certificatesAreValid(@Nullable List<HostSignatureWrapper> certificates, @Nonnull List<Credential> credentials) {
        assert credentialsAreValid(credentials) : "The credentials have to be valid.";
        
        if (certificates != null) {
            if (credentials.size() != 1) return false;
            @Nonnull Credential credential = credentials.get(0);
            if (credential.isAttributeBased()) return false;
            @Nonnull NonHostIdentifier issuer = credential.getIssuer();
            for (@Nonnull HostSignatureWrapper certificate : certificates) {
                if (!issuer.equals(certificate.getSubject())) return false;
            }
        }
        return true;
    }
    
    /**
     * Return the credentials with which the element is signed.
     * 
     * @return the credentials with which the element is signed.
     */
    public @Nonnull List<Credential> getCredentials() {
        return credentials;
    }
    
    /**
     * Return the certificates that are appended to an identity-based authentication.
     * 
     * @return the certificates that are appended to an identity-based authentication.
     */
    public @Nullable List<HostSignatureWrapper> getCertificates() {
        return certificates;
    }
    
    /**
     * Returns whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * 
     * @return whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     */
    public boolean isLodged() {
        return lodged;
    }
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     * 
     * @return either the value b' for clients or the value f' for hosts or null if the credentials are not shortened.
     */
    public @Nullable BigInteger getValue() {
        return value;
    }
    
    @Override
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        if (!super.isSignedLike(signature)) return false;
        @Nonnull CredentialsSignatureWrapper other = (CredentialsSignatureWrapper) signature;
        if (this.credentials.size() != other.credentials.size()) return false;
        for (int i = 0; i < credentials.size(); i++) {
            if (!this.credentials.get(i).isSimilarTo(other.credentials.get(i))) return false;
        }
        return Objects.equals(this.certificates, other.certificates) && this.lodged == other.lodged && Objects.equals(this.value, other.value);
    }
    
    /**
     * Returns whether none of the credentials is used only once.
     * 
     * @return whether none of the credentials is used only once.
     */
    public boolean hasNoOneTimeCredential() {
        for (Credential credential : credentials) {
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
    public boolean isIdentityBased() {
        return credentials.get(0).isIdentityBased();
    }
    
    /**
     * Returns whether this authentication is attribute-based.
     * 
     * @return whether this authentication is attribute-based.
     */
    public boolean isAttributeBased() {
        return credentials.get(0).isAttributeBased();
    }
    
    /**
     * Returns whether this authentication is role-based.
     * 
     * @return whether this authentication is role-based.
     */
    public boolean isRoleBased() {
        return credentials.get(0).getRole() != null;
    }
    
    /**
     * Returns the issuer of the first credential if the authentication is identity-based and without a role.
     * 
     * @return the issuer of the first credential if the authentication is identity-based and without a role.
     * 
     * @require isIdentityBased() : "The authentication is identity-based.";
     * @require !isRoleBased() : "The authentication is not role-based.";
     */
    public @Nonnull Person getIssuer() throws FailedIdentityException {
        assert isIdentityBased() : "The authentication is identity-based.";
        assert !isRoleBased(): "The authentication is not role-based.";
        
        return credentials.get(0).getIssuer().getIdentity().toPerson();
    }
    
    /**
     * Checks whether the first and only credential was issued by the given person and throws a {@link PacketException} if not.
     * 
     * @param person the person to check.
     */
    public void checkIssuer(@Nonnull Person issuer) throws PacketException, FailedIdentityException {
        if (!isIdentityBased() || isRoleBased() || !issuer.equals(getIssuer())) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Returns the attribute with the given type from the credentials and certificates or null if not found.
     * 
     * @param type the semantic type of the attribute which is to be returned.
     * 
     * @return the attribute with the given type from the credentials and certificates or null if not found.
     */
    public @Nullable Block getAttribute(@Nonnull SemanticType type) throws FailedIdentityException {
        if (isAttributeBased()) {
            for (@Nonnull Credential credential : credentials) {
                if (credential.getIssuer().getIdentity().equals(type)) return credential.getAttribute();
            }
        } else {
            for (@Nonnull HostSignatureWrapper certificate : certificates) {
                if (certificate.getSigner().getIdentity().equals(type)) return certificate.getElement();
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
     */
    public boolean canRead(@Nonnull SemanticType type) {
        for (@Nonnull Credential credential : credentials) {
            @Nullable AgentPermissions permissions = credential.getPermissions();
            if (permissions == null || !permissions.canRead(type)) return false;
        }
        return true;
    }
    
    /**
     * Checks whether each credential allows to read the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     */
    public void checkRead(@Nonnull SemanticType type) throws PacketException {
        if (!canRead(type)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Returns whether each credential allows to write the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether each credential allows to write the given type.
     */
    public boolean canWrite(@Nonnull SemanticType type) {
        for (@Nonnull Credential credential : credentials) {
            @Nullable AgentPermissions permissions = credential.getPermissions();
            if (permissions == null || !permissions.canWrite(type)) return false;
        }
        return true;
    }
    
    /**
     * Checks whether each credential allows to write the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     */
    public void checkWrite(@Nonnull SemanticType type) throws PacketException {
        if (!canWrite(type)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    /**
     * Returns whether the permissions of each credential covers the given permissions.
     * 
     * @param permissions the permissions that needs to be covered.
     * 
     * @return whether the permissions of each credential covers the given permissions.
     */
    public boolean cover(@Nonnull AgentPermissions permissions) {
        for (@Nonnull Credential credential : credentials) {
            @Nullable AgentPermissions _permissions = credential.getPermissions();
            if (_permissions == null || !_permissions.cover(permissions)) return false;
        }
        return true;
    }
    
    /**
     * Checks whether the permissions of each credential covers the given permissions and throws a {@link PacketException} if not.
     * 
     * @param permissions the permissions that need to be covered.
     */
    public void checkCoverage(@Nonnull AgentPermissions permissions) throws PacketException {
        if (!cover(permissions)) throw new PacketException(PacketError.AUTHORIZATION);
    }
    
    
    @Override
    public void verify() throws InvalidEncodingException, InvalidSignatureException {
        if (System.currentTimeMillis() - getTime() > Server.YEAR) throw new InvalidSignatureException("The credential signature is out of date.");
        
        @Nonnull Block[] elements = new TupleWrapper(getCache()).getElementsNotNull(4);
        @Nonnull BigInteger hash = elements[0].getHash();
        
        @Nonnull Block[] subelements = new TupleWrapper(elements[3]).getElementsNotNull(8);
        @Nonnull Exponent t = new Exponent(subelements[0]);
        @Nonnull Exponent su = new Exponent(subelements[1]);
        if (su.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The credential signature is invalid: The value su is too big.");
        
        @Nullable Restrictions restrictions = credentials.get(0).getRestrictions();
        @Nullable Exponent v = null, sv = null;
        if (restrictions == null) {
            sv = new Exponent(subelements[3]);
            if (sv.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The credential signature is invalid: The value sv is too big.");
        } else {
            v = new Exponent(restrictions.toBlock().getHash());
        }
        
        @Nonnull List<Block> list = new ListWrapper(subelements[3]).getElements();
        @Nonnull List<Block> ts = new ArrayList<Block>(list.size());
        for (int i = 0; i < list.size(); i++) {
            @Nonnull PublicKey publicKey = credentials.get(i).getPublicKey();
            @Nonnull Exponent o = credentials.get(i).getO();
            @Nonnull Block[] blocks = new TupleWrapper(list.get(i)).getElementsNotNull(8);
            @Nonnull Element c = publicKey.getCompositeGroup().getElement(blocks[2]);
            
            @Nonnull Exponent se = new Exponent(blocks[3]);
            if (se.getBitLength() > Parameters.RANDOM_CREDENTIAL_EXPONENT) throw new InvalidSignatureException("The credential signature is invalid: The value se is too big.");
            @Nonnull Exponent sb = new Exponent(blocks[4]);
            if (sb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT + 1) throw new InvalidSignatureException("The credential signature is invalid: The value sb is too big.");
            
            @Nonnull Element hiddenElement = c.pow(se).multiply(publicKey.getAb().pow(sb)).multiply(publicKey.getAu().pow(su));
            @Nonnull Element exposedElement = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            @Nullable Exponent si = null;
            if (blocks[5].isEmpty()) {
                si = new Exponent(blocks[6]);
                if (si.getBitLength() > Parameters.RANDOM_EXPONENT) throw new InvalidSignatureException("The credential signature is invalid: The value si is too big.");
                hiddenElement = hiddenElement.multiply(publicKey.getAi().pow(si));
            } else {
                exposedElement = publicKey.getAi().pow(new Exponent(blocks[5]));
            }
            
            if (v == null) {
                assert sv != null : "The value sv cannot be null if v is null (see code above).";
                hiddenElement = hiddenElement.multiply(publicKey.getAv().pow(sv));
            } else {
                exposedElement = exposedElement.multiply(publicKey.getAv().pow(v));
            }
            
            exposedElement = exposedElement.inverse().multiply(publicKey.getAo().pow(o));
            
            @Nonnull Block[] subblocks = new Block[3];
            subblocks[0] = hiddenElement.multiply(exposedElement.pow(t)).toBlock();
            
            if (lodged && si != null) {
                @Nonnull Block[] encryptions = new TupleWrapper(blocks[7]).getElementsNotNull(4);
                @Nonnull Block[] wis = new TupleWrapper(encryptions[0]).getElementsNotNull(2);
                @Nonnull Exponent swi = new Exponent(encryptions[1]);
                if (swi.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT) throw new InvalidSignatureException("The credential signature is invalid: The value swi is too big.");
                @Nonnull Block[] wbs = new TupleWrapper(encryptions[2]).getElementsNotNull(2);
                @Nonnull Exponent swb = new Exponent(encryptions[3]);
                if (swb.getBitLength() > Parameters.RANDOM_BLINDING_EXPONENT) throw new InvalidSignatureException("The credential signature is invalid: The value swb is too big.");
                
                wis[0] = publicKey.getY().pow(swi).multiply(publicKey.getZPlus1().pow(si)).multiply(publicKey.getSquareGroup().getElement(wis[0]).pow(t)).toBlock();
                wis[1] = publicKey.getG().pow(swi).multiply(publicKey.getSquareGroup().getElement(wis[1]).pow(t)).toBlock();
                
                wbs[0] = publicKey.getY().pow(swb).multiply(publicKey.getZPlus1().pow(sb)).multiply(publicKey.getSquareGroup().getElement(wbs[0]).pow(t)).toBlock();
                wbs[1] = publicKey.getG().pow(swb).multiply(publicKey.getSquareGroup().getElement(wbs[1]).pow(t)).toBlock();
                
                subblocks[1] = new TupleWrapper(wis).toBlock();
                subblocks[2] = new TupleWrapper(wbs).toBlock();
            } else {
                subblocks[1] = Block.EMPTY;
                subblocks[2] = Block.EMPTY;
            }
            
            ts.add(i, new TupleWrapper(subblocks).toBlock());
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        if (value != null) {
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            @Nonnull Exponent sb = new Exponent(subelements[7]);
            
            @Nonnull Element element = publicKey.getAu().pow(su).multiply(publicKey.getAb().pow(sb));
            if (sv != null) {
                element = element.multiply(publicKey.getAv().pow(sv));
            }
            tf = publicKey.getCompositeGroup().getElement(value).pow(t).multiply(element).toBlock().getHash();
        }
        
        if (!t.getValue().equals(hash.xor(new ListWrapper(ts).toBlock().getHash()).xor(tf))) throw new InvalidSignatureException("The credential signature is invalid: The value t is not correct.");
        
        if (certificates != null) {
            for (@Nonnull HostSignatureWrapper certificate : certificates) certificate.verify();
        }
    }
    
    @Override
    protected void sign(@Nonnull Block[] elements, @Nonnull BigInteger hash) {
        assert credentials.get(0) instanceof ClientCredential : "The first credential is a client credential (like all others).";
        @Nonnull ClientCredential mainCredential = (ClientCredential) credentials.get(0);
        @Nonnull Exponent u = mainCredential.getU();
        @Nonnull Exponent v = mainCredential.getV();
        
        @Nonnull SecureRandom random = new SecureRandom();
        @Nonnull Exponent ru = new Exponent(new BigInteger(Parameters.RANDOM_EXPONENT, random));
        
        final boolean vIsDisclosed = mainCredential.getRestrictions() != null && (mainCredential.getIssuer().equals(getSubject()) || mainCredential.getRole() != null);
        @Nullable Exponent rv = null;
        if (!vIsDisclosed) rv = new Exponent(new BigInteger(Parameters.RANDOM_EXPONENT, random));
        
        int length = credentials.size();
        @Nonnull ClientCredential[] randomizedCredentials = new ClientCredential[length];
        
        @Nonnull Exponent[] res = new Exponent[length];
        @Nonnull Exponent[] rbs = new Exponent[length];
        @Nonnull Exponent[] ris = new Exponent[length];
        
        @Nonnull Exponent[] rwis = new Exponent[length];
        @Nonnull Exponent[] rwbs = new Exponent[length];
        
        @Nonnull Block[] wis = new Block[length];
        @Nonnull Block[] wbs = new Block[length];
        
        @Nonnull Exponent[] rrwis = new Exponent[length];
        @Nonnull Exponent[] rrwbs = new Exponent[length];
        
        @Nonnull List<Block> ts = new ArrayList<Block>(length);
        for (int i = 0; i < length; i++) {
            assert credentials.get(i) instanceof ClientCredential : "All credentials have to be client credentials, which was already checked in the constructor.";
            randomizedCredentials[i] = ((ClientCredential) credentials.get(i)).getRandomizedCredential();
            @Nonnull PublicKey publicKey = randomizedCredentials[i].getPublicKey();
            @Nonnull Element element = publicKey.getCompositeGroup().getElement(BigInteger.ONE);
            
            res[i] = new Exponent(new BigInteger(Parameters.RANDOM_CREDENTIAL_EXPONENT, random));
            rbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
            
            if (!randomizedCredentials[i].isOneTime()) {
                ris[i] = new Exponent(new BigInteger(Parameters.RANDOM_EXPONENT, random));
                element = element.multiply(publicKey.getAi().pow(ris[i]));
            }
            
            if (!vIsDisclosed) {
                assert rv != null : "If v is not disclosed, rv is not null (see the code above).";
                element = element.multiply(publicKey.getAv().pow(rv));
            }
            
            @Nonnull Block[] subelements = new Block[3];
            subelements[0] = randomizedCredentials[i].getC().pow(res[i]).multiply(publicKey.getAb().pow(rbs[i])).multiply(publicKey.getAu().pow(ru)).multiply(element).toBlock();
            
            if (lodged && !randomizedCredentials[i].isOneTime()) {
                rwis[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
                rwbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT - Parameters.HASH, random));
                
                wis[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getI(), rwis[i]);
                wbs[i] = publicKey.getVerifiableEncryption(randomizedCredentials[i].getB(), rwbs[i]);
                
                rrwis[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
                rrwbs[i] = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
                
                subelements[1] = publicKey.getVerifiableEncryption(ris[i], rrwis[i]);
                subelements[2] = publicKey.getVerifiableEncryption(rbs[i], rrwbs[i]);
            } else {
                subelements[1] = Block.EMPTY;
                subelements[2] = Block.EMPTY;
            }
            
            ts.add(i, new TupleWrapper(subelements).toBlock());
        }
        
        @Nonnull BigInteger tf = BigInteger.ZERO;
        @Nullable Element f = null;
        @Nullable Exponent rb = null;
        if (value != null) {
            assert publicKey != null : "If credentials are to be shortened, the public key of the receiving host is retrieved in the constructor.";
            rb = new Exponent(new BigInteger(Parameters.RANDOM_BLINDING_EXPONENT, random));
            
            f = publicKey.getAu().pow(ru).multiply(publicKey.getAb().pow(rb));
            if (!vIsDisclosed) {
                assert rv != null : "If v is not disclosed, rv is not null (see the code above).";
                f = f.multiply(publicKey.getAv().pow(rv));
            }
            tf = f.toBlock().getHash();
            
            f = publicKey.getAu().pow(u).multiply(publicKey.getAb().pow(value));
            if (!vIsDisclosed) f = f.multiply(publicKey.getAv().pow(v));
        }
        
        @Nonnull Exponent t = new Exponent(hash.xor(new ListWrapper(ts).toBlock().getHash()).xor(tf));
        
        @Nonnull Block[] subelements = new Block[8];
        subelements[0] = t.toBlock();
        subelements[1] = ru.subtract(t.multiply(u)).toBlock();
        if (vIsDisclosed) {
            Restrictions restrictions = mainCredential.getRestrictions();
            assert restrictions != null : "If the restrictions are disclosed, they are not null (see the condition above).";
            subelements[2] = restrictions.toBlock();
            subelements[3] = Block.EMPTY;
        } else {
            assert rv != null : "If v is not disclosed, rv is not null (see the code above).";
            subelements[2] = Block.EMPTY;
            subelements[3] = rv.subtract(t.multiply(v)).toBlock();
        }
        
        @Nonnull List<Block> list = new ArrayList<Block>(length);
        for (int i = 0; i < length; i++) {
            @Nonnull Block[] blocks = new Block[8];
            blocks[0] = randomizedCredentials[i].getExposed();
            if (value == null || mainCredential.getRole() != null) {
                blocks[1] = randomizedCredentials[i].getRandomizedPermissions().toBlock();
            } else {
                blocks[1] = Block.EMPTY;
            }
            blocks[2] = randomizedCredentials[i].getC().toBlock();
            blocks[3] = res[i].subtract(t.multiply(randomizedCredentials[i].getE())).toBlock();
            blocks[4] = rbs[i].subtract(t.multiply(randomizedCredentials[i].getB())).toBlock();
            if (randomizedCredentials[i].isOneTime()) {
                blocks[5] = randomizedCredentials[i].getI().toBlock();
                blocks[6] = Block.EMPTY;
                blocks[7] = Block.EMPTY;
            } else {
                blocks[5] = Block.EMPTY;
                blocks[6] = ris[i].subtract(t.multiply(randomizedCredentials[i].getI())).toBlock();
                
                if (lodged) {
                    @Nonnull Block swi = rrwis[i].subtract(t.multiply(rwis[i])).toBlock();
                    @Nonnull Block swb = rrwbs[i].subtract(t.multiply(rwbs[i])).toBlock();
                    blocks[7] = new TupleWrapper(new Block[]{wis[i], swi, wbs[i], swb}).toBlock();
                } else {
                    blocks[7] = Block.EMPTY;
                }
            }
            list.add(i, new TupleWrapper(blocks).toBlock());
        }
        subelements[4] = new ListWrapper(list).toBlock();
        
        subelements[5] = new ListWrapper(certificates, true).toBlock();
        
        if (value == null) {
            subelements[6] = Block.EMPTY;
            subelements[7] = Block.EMPTY;
        } else {
            assert f != null && rb != null : "If the credential is shortened, f and rb are not null (see the code above).";
            subelements[6] = f.toBlock();
            subelements[7] = rb.subtract(t.multiply(new Exponent(value))).toBlock();
        }
        
        elements[3] = new TupleWrapper(subelements).toBlock();
    }
    
}
