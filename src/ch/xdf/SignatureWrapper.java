package ch.xdf;

import ch.virtualid.annotations.Exposed;
import ch.virtualid.annotations.Pure;
import ch.virtualid.agent.Agent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.credential.Credential;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.SyntacticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.exceptions.InvalidSignatureException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a block with the syntactic type {@code signature@xdf.ch} for encoding and decoding.
 * <p>
 * Format: {@code block = ((identifier, time, element, audit), hostSignature, clientSignature, credentialsSignature)}
 * 
 * @invariant !isSigned() || getSubject() != null : "If the signature is signed, the subject is not null.";
 * 
 * @see HostSignatureWrapper
 * @see ClientSignatureWrapper
 * @see CredentialsSignatureWrapper
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public class SignatureWrapper extends BlockWrapper implements Immutable {
    
    /**
     * Stores the syntactic type {@code signature@xdf.ch}.
     */
    public static final @Nonnull SyntacticType TYPE = SyntacticType.create("signature@xdf.ch").load(1);
    
    /**
     * Stores the semantic type {@code content.signature@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CONTENT = SemanticType.create("content.signature@virtualid.ch").load(TupleWrapper.TYPE, Identity.IDENTIFIER, Time.TYPE, SemanticType.UNKNOWN, Audit.TYPE);
    
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
    private final @Nullable Identifier subject;
    
    /**
     * Stores the time of the signature generation or null if the element is not signed.
     * 
     * @invariant time == null || time.isNonNegative() : "The time is either null or non-negative.";
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
    protected SignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nonnull Identifier subject, @Nullable Audit audit) {
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
    public SignatureWrapper(@Nonnull SemanticType type, @Nullable Block element, @Nullable Identifier subject) {
        super(type);
        
        this.element = element;
        this.subject = subject;
        this.time = null;
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
    public SignatureWrapper(@Nonnull SemanticType type, @Nullable Blockable element, @Nullable Identifier subject) {
        this(type, Block.toBlock(element), subject);
    }
    
    
    /**
     * Wraps and decodes the given block with verifying the signature.
     * 
     * @param block the block to be wrapped and decoded.
     * 
     * @return the signature wrapper of the appropriate subclass.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper decode(@Nonnull Block block) throws SQLException, InvalidEncodingException, InvalidSignatureException, FailedIdentityException {
        final @Nonnull SignatureWrapper signatureWrapper = decodeUnverified(block);
        signatureWrapper.verify();
        return signatureWrapper;
    }
    
    /**
     * Wraps and decodes the given block without verifying the signature.
     * 
     * @param block the block to be wrapped and decoded.
     * 
     * @return the signature wrapper of the appropriate subclass.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    @Pure
    public static @Nonnull SignatureWrapper decodeUnverified(@Nonnull Block block) throws SQLException, InvalidEncodingException, FailedIdentityException {
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElements(4);
        final @Nullable Block hostSignature = elements.get(1);
        final @Nullable Block clientSignature = elements.get(2);
        final @Nullable Block credentialsSignature = elements.get(3);
        
        if (hostSignature != null && clientSignature == null && credentialsSignature == null) return new HostSignatureWrapper(block, hostSignature);
        if (hostSignature == null && clientSignature != null && credentialsSignature == null) return new ClientSignatureWrapper(block, clientSignature);
        if (hostSignature == null && clientSignature == null && credentialsSignature != null) return new CredentialsSignatureWrapper(block, credentialsSignature);
        if (hostSignature == null && clientSignature == null && credentialsSignature == null) return new SignatureWrapper(block, false);
        throw new InvalidEncodingException("The element may only be signed by either a host, a client, with credentials or not at all.");
    }
    
    /**
     * Wraps and decodes the given block. (Only to be called by subclasses!)
     * 
     * @param block the block to be wrapped and decoded.
     * @param signed whether the block was signed.
     * 
     * @require block.getType().isBasedOn(getSyntacticType()) : "The block is based on the indicated syntactic type.";
     */
    protected SignatureWrapper(@Nonnull Block block, boolean signed) throws InvalidEncodingException {
        super(block);
        cache = block;
        
        final @Nonnull Block content = new TupleWrapper(new Block(IMPLEMENTATION, block)).getElementNotNull(0);
        final @Nonnull TupleWrapper tuple = new TupleWrapper(content);
        this.subject = tuple.isElementNull(0) ? null : new NonHostIdentifier(tuple.getElementNotNull(0));
        if (signed && subject == null) throw new InvalidEncodingException("The subject may not be null if the element is signed.");
        this.time = tuple.isElementNull(1) ? null : new Time(tuple.getElementNotNull(1));
        if (signed && time == null) throw new InvalidEncodingException("The signature time may not be null if the element is signed.");
        if (time != null && time.isNegative()) throw new InvalidEncodingException("The signature time may not be negative.");
        this.element = tuple.getElementNotNull(2);
        this.audit = tuple.isElementNull(3) ? null : new Audit(tuple.getElementNotNull(3));
    }
    
    
    /**
     * Returns the element of the wrapped block.
     * 
     * @return the element of the wrapped block.
     */
    @Pure
    public final @Nullable Block getElement() {
        return element;
    }
    
    /**
     * Returns the identifier of the identity about which a statement is made or null in case of unsigned attributes.
     * 
     * @return the identifier of the identity about which a statement is made or null in case of unsigned attributes.
     * 
     * @ensure !isSigned() || getSubject() != null : "If this signature is signed, the subject is not null.";
     */
    @Pure
    public final @Nullable Identifier getSubject() {
        return subject;
    }
    
    /**
     * Returns the identifier of the identity about which a statement is made.
     * 
     * @return the identifier of the identity about which a statement is made.
     * 
     * @require isSigned() : "This signature is signed.";
     */
    @Pure
    public final @Nonnull Identifier getSubjectNotNull() {
        assert isSigned() : "This signature is signed.";
        
        assert subject != null : "This then follows from the class invariant.";
        return subject;
    }
    
    /**
     * Returns the time of the signature generation or null if the element is not signed.
     * 
     * @return the time of the signature generation or null if the element is not signed.
     * 
     * @ensure time == null || time.isNonNegative() : "The time is either null or non-negative.";
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
     * @require isSigned() : "This signature is signed.";
     * 
     * @ensure time.isNonNegative() : "The time is non-negative.";
     */
    @Pure
    public final @Nonnull Time getTimeNotNull() {
        assert isSigned() : "This signature is signed.";
        
        assert time != null : "This then follows from the method implementation.";
        return time;
    }
    
    /**
     * Returns the time of the signature generation rounded down to the last half hour.
     * 
     * @return the time of the signature generation rounded down to the last half hour.
     * 
     * @require isSigned() : "This signature is signed.";
     * 
     * @ensure return.isNonNegative() && return.isMultipleOf(Time.HALF_HOUR) : "The returned time is non-negative and a multiple of half an hour.";
     */
    @Pure
    public final @Nonnull Time getSignatureTimeRoundedDown() {
        assert isSigned() : "This signature is signed.";
        
        assert time != null : "This then follows from the method implementation.";
        return time.roundDown(Time.HALF_HOUR);
    }
    
    /**
     * Returns whether the element is signed.
     * 
     * @return whether the element is signed.
     */
    @Pure
    public final boolean isSigned() {
        return time != null;
    }
    
    /**
     * Returns whether this signature is signed like the given signature.
     * 
     * @param signature the signature to compare this signature with.
     * 
     * @return whether this signature is signed like the given signature.
     */
    @Pure
    @SuppressWarnings("null")
    public boolean isSignedLike(@Nonnull SignatureWrapper signature) {
        return getClass().equals(signature.getClass()) && (subject == null && signature.subject == null || subject != null && subject.equals(signature.subject));
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
     * Verifies the signature and throws an exception if it is not valid.
     */
    @Pure
    public void verify() throws InvalidSignatureException, InvalidEncodingException {}
    
    /**
     * Signs the element. (This method should be overridden in subclasses.)
     * 
     * @param elements the elements of the wrapped block with the indexes 1 to 3 reserved for the signatures.
     * @param hash the hash of the element with index 0, which is to be signed.
     * 
     * @require elements.isNotFrozen() : "The elements are not frozen.";
     */
    protected void sign(@Nonnull FreezableArray<Block> elements, @Nonnull BigInteger hash) {}
    
    
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
            subelements.set(0, Block.toBlock(subject));
            subelements.set(1, Block.toBlock(time));
            subelements.set(2, element);
            subelements.set(3, Block.toBlock(audit));
            
            final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(4);
            final @Nonnull Block block = new TupleWrapper(CONTENT, subelements.freeze()).toBlock();
            elements.set(0, block);
            elements.set(1, null);
            elements.set(2, null);
            elements.set(3, null);
            
            sign(elements, block.getHash());
            cache = new TupleWrapper(IMPLEMENTATION, elements.freeze()).toBlock();
        }
        return cache;
    }
    
    
    @Pure
    @Override
    public @Nonnull SyntacticType getSyntacticType() {
        return TYPE;
    }
    
    @Pure
    @Override
    protected final int determineLength() {
        return getCache().getLength();
    }
    
    @Pure
    @Override
    protected void encode(@Exposed @Nonnull Block block) {
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
        throw new PacketException(PacketError.SIGNATURE);
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
        throw new PacketException(PacketError.SIGNATURE);
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
        throw new PacketException(PacketError.SIGNATURE);
    }
    
    
    /**
     * Returns the agent that signed the wrapped element.
     * 
     * @return the agent that signed the wrapped element.
     * 
     * @throws PacketException if the agent's authorization is not sufficient.
     * 
     * @require getSubject() != null : "The subject is not null.";
     */
    @Pure
    public final @Nonnull Agent getAgent() throws PacketException, SQLException, FailedIdentityException, InvalidEncodingException {
        @Nullable Identifier identifier = getSubject();
        assert identifier != null : "The subject is not null";
        
        // TODO: Fix and adapt!
        
        @Nonnull NonHostIdentity identity = identifier.getIdentity().toNonHostIdentity();
        if (this instanceof ClientSignatureWrapper) {
            @Nullable Agent agent = Host.getClientAgent(connection, identity, ((ClientSignatureWrapper) this).getCommitment());
            if (agent != null) return agent;
        } else if (this instanceof CredentialsSignatureWrapper) {
            @Nonnull Credential credential = ((CredentialsSignatureWrapper) this).getCredentials().get(0);
            @Nullable NonHostIdentifier relation = credential.getRole();
            if (relation != null) {
                @Nullable OutgoingRole outgoingRole = Host.getOutgoingRole(connection, identity, relation.getIdentity().toSemanticType());
                if (outgoingRole != null && Host.isInContext(connection, identity, credential.getIssuer().getIdentity().toPerson(), outgoingRole.getContext())) {
                    outgoingRole.checkCovers(credential);
                    outgoingRole.restrictTo(credential);
                    return outgoingRole;
                }
            }
        }
        throw new PacketException(PacketError.AUTHORIZATION);
    }
    
}
