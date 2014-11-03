package ch.xdf;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InactiveSignatureException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Audit;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} for encoding and decoding.
 * <p>
 * Format: {@code block = ((identifier, time, element, audit), hostSignature, clientSignature, credentialsSignature)}
 * 
 * @invariant !isSigned() || hasSubject() : "If this signature is signed, it has a subject.";
 * 
 * @see HostSignatureWrapper
 * @see ClientSignatureWrapper
 * @see CredentialsSignatureWrapper
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class SignatureWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code signature@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("signature@xdf.ch").load(1);
    
    /**
     * Stores the semantic type {@code subject.content.signature@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SUBJECT = SemanticType.create("subject.content.signature@virtualid.ch").load(InternalIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code content.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CONTENT = SemanticType.create("content.signature@virtualid.ch").load(TupleWrapper.TYPE, SUBJECT, Time.TYPE, SemanticType.UNKNOWN, Audit.TYPE);
    
    /**
     * Stores the semantic type {@code signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType IMPLEMENTATION = SemanticType.create("signature@virtualid.ch").load(TupleWrapper.TYPE, CONTENT, HostSignatureWrapper.SIGNATURE, ClientSignatureWrapper.SIGNATURE, CredentialsSignatureWrapper.SIGNATURE);
    
    
    /**
     * Stores the element of this wrapper.
     */
    private final @Nullable Block element;
    
    /**
     * Stores the identifier of the identity about which a statement is made and may only be null in case of unsigned attributes.
     */
    private final @Nullable InternalIdentifier subject;
    
    /**
     * Stores the time of the signature generation or null if this signature has no subject.
     * 
     * @invariant time == null || time.isPositive() : "The time is either null or positive.";
     */
    private final @Nullable Time time;
    
    /**
     * Stores the audit or null if no audit is or shall be appended.
     */
    private final @Nullable Audit audit;
    
    /**
     * Encodes the element into a new block. (Only to be called by subclasses!)
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit is or shall be appended.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    protected SignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
        super(type);
        
        assert element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
        
        this.element = element;
        this.subject = subject;
        this.time = new Time();
        this.audit = audit;
    }
    
    /**
     * Encodes the element into a new block without signing.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made or null if not required.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    public SignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nullable InternalIdentifier subject) {
        super(type);
        
        assert element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
        
        this.element = element;
        this.subject = subject;
        this.time = subject == null ? null : new Time();
        this.audit = null;
    }
    
    /**
     * Encodes the element into a new block without signing.
     * 
     * @param type the semantic type of the new block.
     * @param element the element to encode into the new block.
     * @param subject the identifier of the identity about which a statement is made or null if not required.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getSyntacticType()) : "The given type is based on the indicated syntactic type.";
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the given type.";
     */
    public SignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nullable InternalIdentifier subject) {
        this(type, Block.toBlock(element), subject);
    }
    
    
    /**
     * Wraps and decodes the given block with verifying the signature.
     * 
     * @param block the block to be wrapped and decoded.
     * @param entity the entity that decodes the signature.
     * 
     * @return the signature wrapper of the appropriate subclass.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper decode(@Nonnull Block block, @Nullable Entity entity) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull SignatureWrapper signatureWrapper = decodeUnverified(block, entity);
        signatureWrapper.verify();
        return signatureWrapper;
    }
    
    /**
     * Wraps and decodes the given block without verifying the signature.
     * 
     * @param block the block to be wrapped and decoded.
     * @param entity the entity that decodes the signature.
     * 
     * @return the signature wrapper of the appropriate subclass.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper decodeUnverified(@Nonnull Block block, @Nullable Entity entity) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(new Block(IMPLEMENTATION, block)).getElements(4);
        final @Nullable Block hostSignature = elements.get(1);
        final @Nullable Block clientSignature = elements.get(2);
        final @Nullable Block credentialsSignature = elements.get(3);
        
        if (hostSignature != null && clientSignature == null && credentialsSignature == null) return new HostSignatureWrapper(block, hostSignature);
        if (hostSignature == null && clientSignature != null && credentialsSignature == null) return new ClientSignatureWrapper(block, clientSignature);
        if (hostSignature == null && clientSignature == null && credentialsSignature != null) return new CredentialsSignatureWrapper(block, credentialsSignature, entity);
        if (hostSignature == null && clientSignature == null && credentialsSignature == null) return new SignatureWrapper(block);
        throw new InvalidEncodingException("The element may only be signed by either a host, a client, with credentials or not at all.");
    }
    
    /**
     * Wraps and decodes the given block.
     * 
     * @param block the block to be wrapped and decoded.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    SignatureWrapper(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        this.cache = new Block(IMPLEMENTATION, block);
        final @Nonnull Block content = new TupleWrapper(cache).getElementNotNull(0);
        final @Nonnull TupleWrapper tuple = new TupleWrapper(content);
        this.subject = tuple.isElementNull(0) ? null : Identifier.create(tuple.getElementNotNull(0)).toInternalIdentifier();
        if (isSigned() && subject == null) throw new InvalidEncodingException("The subject may not be null if the element is signed.");
        this.time = tuple.isElementNull(1) ? null : new Time(tuple.getElementNotNull(1));
        if (hasSubject() && time == null) throw new InvalidEncodingException("The signature time may not be null if this signature has a subject.");
        if (time != null && !time.isPositive()) throw new InvalidEncodingException("The signature time has to be positive.");
        this.element = tuple.getElementNotNull(2);
        this.audit = tuple.isElementNull(3) ? null : new Audit(tuple.getElementNotNull(3));
    }
    
    
    /**
     * Returns whether the element is signed.
     * 
     * @return whether the element is signed.
     */
    @Pure
    public final boolean isSigned() {
        return this instanceof HostSignatureWrapper || this instanceof ClientSignatureWrapper || this instanceof CredentialsSignatureWrapper;
    }
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     * 
     * @ensure element == null || element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is either null or based on the parameter of the block's type.";
     */
    @Pure
    public final @Nullable Block getElement() {
        return element;
    }
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     * 
     * @ensure element.getType().isBasedOn(getType().getParameters().getNotNull(0)) : "The element is based on the parameter of the block's type.";
     */
    @Pure
    public final @Nonnull Block getElementNotNull() throws InvalidEncodingException {
        if (element == null) throw new InvalidEncodingException("The signed element is null.");
        return element;
    }
    
    /**
     * Returns the identifier of the identity about which a statement is made or possibly null in case of unsigned attributes.
     * 
     * @return the identifier of the identity about which a statement is made or possibly null in case of unsigned attributes.
     * 
     * @ensure !isSigned() || return != null : "If this signature is signed, the return is not null.";
     */
    @Pure
    public final @Nullable InternalIdentifier getSubject() {
        return subject;
    }
    
    /**
     * Returns whether this signature has a subject.
     * 
     * @return whether this signature has a subject.
     */
    @Pure
    public final boolean hasSubject() {
        return subject != null;
    }
    
    /**
     * Returns the identifier of the identity about which a statement is made.
     * 
     * @return the identifier of the identity about which a statement is made.
     * 
     * @require hasSubject() : "This signature has a subject.";
     */
    @Pure
    public final @Nonnull InternalIdentifier getSubjectNotNull() {
        assert subject != null : "This signature has a subject.";
        
        return subject;
    }
    
    /**
     * Returns the time of the signature generation or null if this signature has no subject.
     * 
     * @return the time of the signature generation or null if this signature has no subject.
     * 
     * @ensure time == null || time.isPositive() : "The time is either null or positive.";
     */
    @Pure
    public final @Nullable Time getTime() {
        return time;
    }
    
    /**
     * Returns the time of the signature generation.
     * 
     * @return the time of the signature generation.
     * 
     * @require hasSubject() : "This signature has a subject.";
     * 
     * @ensure time.isPositive() : "The time is positive.";
     */
    @Pure
    public final @Nonnull Time getTimeNotNull() {
        assert hasSubject() : "This signature has a subject.";
        
        assert time != null : "This then follows from the constructor implementations.";
        return time;
    }
    
    /**
     * Returns the audit or null if no audit is or shall be appended.
     * 
     * @return the audit or null if no audit is or shall be appended.
     */
    @Pure
    public final @Nullable Audit getAudit() {
        return audit;
    }
    
    
    /**
     * Returns whether this signature is signed like the given signature.
     * 
     * @param signature the signature to compare this signature with.
     * 
     * @return whether this signature is signed like the given signature.
     */
    @Pure
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return getClass().equals(signature.getClass()) && Objects.equals(subject, signature.subject);
    }
    
    /**
     * Checks this signature for recency (it may be signed at most half an hour ago).
     */
    @Pure
    public void checkRecency() throws InactiveSignatureException {
        if (time != null && time.isLessThan(Time.HALF_HOUR.ago())) throw new InactiveSignatureException("The signature was signed more than half an hour ago.");
    }
    
    
    /**
     * Verifies the signature and throws an exception if it is not valid.
     */
    @Pure
    public void verify() throws SQLException, IOException, PacketException, ExternalException {}
    
    /**
     * Signs the element. (This method should be overridden in subclasses.)
     * 
     * @param elements the elements of the wrapped block with the indexes 1 to 3 reserved for the signatures.
     * @param hash the hash of the element with index 0, which is to be signed.
     * 
     * @require elements.isNotFrozen() : "The elements are not frozen.";
     * @require elements.isNotNull(0) : "The first element is not null.";
     */
    protected void sign(@Nonnull FreezableArray<Block> elements) {}
    
    
    /**
     * Stores the signed element.
     * 
     * @invariant cache.getType().equals(IMPLEMENTATION) : "The cache is of the implementation type.";
     */
    private @Nullable Block cache;
    
    /**
     * Returns the signed element.
     * 
     * @return the signed element.
     */
    @Pure
    protected final @Nonnull Block getCache() {
        if (cache == null) {
            final @Nonnull FreezableArray<Block> subelements = new FreezableArray<Block>(4);
            subelements.set(0, Block.toBlock(SUBJECT, subject));
            subelements.set(1, Block.toBlock(time));
            subelements.set(2, element);
            subelements.set(3, Block.toBlock(audit));
            
            final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(4);
            final @Nonnull Block block = new TupleWrapper(CONTENT, subelements.freeze()).toBlock();
            elements.set(0, block);
            
            sign(elements);
            cache = new TupleWrapper(IMPLEMENTATION, elements.freeze()).toBlock();
        }
        return cache;
    }
    
    
    @Pure
    @Override
    public final @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected final int determineLength() {
        return getCache().getLength();
    }
    
    @Pure
    @Override
    protected final void encode(@Exposed @Nonnull Block block) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
        assert block.getLength() == determineLength() : "The block's length has to match the determined length.";
        
        getCache().writeTo(block);
    }
    
    
    /**
     * Returns this signature wrapper as a {@link HostSignatureWrapper}.
     * 
     * @return this signature wrapper as a {@link HostSignatureWrapper}.
     * 
     * @throws PacketException if this signature wrapper is not an instance of {@link HostSignatureWrapper}.
     */
    @Pure
    public final @Nonnull HostSignatureWrapper toHostSignatureWrapper() throws PacketException {
        if (this instanceof HostSignatureWrapper) return (HostSignatureWrapper) this;
        throw new PacketException(PacketError.SIGNATURE, "The element was not signed by a host.");
    }
    
    /**
     * Returns this signature wrapper as a {@link ClientSignatureWrapper}.
     * 
     * @return this signature wrapper as a {@link ClientSignatureWrapper}.
     * 
     * @throws PacketException if this signature wrapper is not an instance of {@link ClientSignatureWrapper}.
     */
    @Pure
    public final @Nonnull ClientSignatureWrapper toClientSignatureWrapper() throws PacketException {
        if (this instanceof ClientSignatureWrapper) return (ClientSignatureWrapper) this;
        throw new PacketException(PacketError.SIGNATURE, "The element was not signed by a client.");
    }
    
    /**
     * Returns this signature wrapper as a {@link CredentialsSignatureWrapper}.
     * 
     * @return this signature wrapper as a {@link CredentialsSignatureWrapper}.
     * 
     * @throws PacketException if this signature wrapper is not an instance of {@link CredentialsSignatureWrapper}.
     */
    @Pure
    public final @Nonnull CredentialsSignatureWrapper toCredentialsSignatureWrapper() throws PacketException {
        if (this instanceof CredentialsSignatureWrapper) return (CredentialsSignatureWrapper) this;
        throw new PacketException(PacketError.SIGNATURE, "The element was not signed with credentials.");
    }
    
    
    /**
     * Returns whether this signature is a certificate.
     * 
     * @return whether this signature is a certificate.
     */
    @Pure
    public final boolean isCertificate() {
        return getType().isBasedOn(Certificate.TYPE);
    }
    
    /**
     * Verifies this signature as a certificate and throws an exception if it is not valid.
     * 
     * @require isCertificate() : "This signature is a certificate.";
     */
    @Pure
    public void verifyAsCertificate() throws SQLException, IOException, PacketException, ExternalException {
        assert isCertificate() : "This signature is a certificate.";
    }
    
    
    /**
     * Returns the agent that signed the wrapped element or null if no such agent is found.
     * 
     * @param entity the entity whose agent is to be returned.
     * 
     * @return the agent that signed the wrapped element or null if no such agent is found.
     */
    @Pure
    public @Nullable Agent getAgent(@Nonnull Entity entity) throws SQLException {
        return null;
    }
    
    /**
     * Returns the restricted agent that signed the wrapped element.
     * 
     * @param entity the entity whose agent is to be returned.
     * @param publicKey the active public key of the recipient.
     * 
     * @return the restricted agent that signed the wrapped element.
     * 
     * @throws PacketException if no such agent is found or the check failed.
     */
    @Pure
    public @Nonnull Agent getAgentCheckedAndRestricted(@Nonnull Entity entity, @Nullable PublicKey publicKey) throws SQLException, PacketException {
        throw new PacketException(PacketError.AUTHORIZATION, "The element was not signed by an authorized agent.");
    }
    
}
