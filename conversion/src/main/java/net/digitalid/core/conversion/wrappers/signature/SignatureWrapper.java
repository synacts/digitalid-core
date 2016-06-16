package net.digitalid.core.conversion.wrappers.signature;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.elements.NullableElements;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.annotations.Encoding;
import net.digitalid.core.conversion.annotations.NonEncoding;
import net.digitalid.core.conversion.wrappers.AbstractWrapper;
import net.digitalid.core.conversion.wrappers.BlockBasedWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.cryptography.signature.exceptions.InactiveAuthenticationException;
import net.digitalid.core.cryptography.signature.exceptions.InactiveSignatureException;
import net.digitalid.core.cryptography.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;
import net.digitalid.core.synchronizer.Audit;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.converter.xdf.Encode;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.cryptography.PublicKey;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.SyntacticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.annotations.Loaded;

/**
 * This class wraps an {@link Block element} for encoding and decoding a block of the syntactic type {@code signature@core.digitalid.net}.
 * <p>
 * Format: {@code block = ((identifier, time, element, audit), hostSignature, clientSignature, credentialsSignature)}
 * 
 * @invariant !isSigned() || hasSubject() : "If this signature is signed, it has a subject.";
 * 
 * @see HostSignatureWrapper
 * @see ClientSignatureWrapper
 * @see CredentialsSignatureWrapper
 */
@Immutable
public class SignatureWrapper extends BlockBasedWrapper<SignatureWrapper> {
    
    /* -------------------------------------------------- Implementation -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code subject.content.signature@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SUBJECT = SemanticType.map("subject.content.signature@core.digitalid.net").load(InternalIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code content.signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType CONTENT = SemanticType.map("content.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, SUBJECT, Time.TYPE, SemanticType.UNKNOWN, Audit.TYPE);
    
    /**
     * Stores the semantic type {@code signature@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType IMPLEMENTATION = SemanticType.map("implementation.signature@core.digitalid.net").load(TupleWrapper.XDF_TYPE, CONTENT, HostSignatureWrapper.SIGNATURE, ClientSignatureWrapper.SIGNATURE, CredentialsSignatureWrapper.SIGNATURE);
    
    /* -------------------------------------------------- Element -------------------------------------------------- */
    
    /**
     * Stores the element of this wrapper.
     */
    private final @Nullable Block element;
    
    /**
     * Returns the nullable element of this wrapper.
     * 
     * @return the nullable element of this wrapper.
     * 
     * @ensure element == null || element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the semantic type.";
     */
    @Pure
    public final @Nullable Block getNullableElement() {
        return element;
    }
    
    /**
     * Returns the non-nullable element of this wrapper.
     * 
     * @return the non-nullable element of this wrapper.
     * 
     * @ensure element.getType().isBasedOn(getSemanticType().getParameters().getNonNullable(0)) : "The element is based on the parameter of the semantic type.";
     */
    @Pure
    public final @Nonnull Block getNonNullableElement() throws InvalidParameterValueCombinationException {
        if (element == null) { throw InvalidParameterValueCombinationException.get("The signed element is null."); }
        return element;
    }
    
    /* -------------------------------------------------- Signed -------------------------------------------------- */
    
    /**
     * Returns whether the element is signed.
     * 
     * @return whether the element is signed.
     */
    @Pure
    public final boolean isSigned() {
        return this instanceof HostSignatureWrapper || this instanceof ClientSignatureWrapper || this instanceof CredentialsSignatureWrapper;
    }
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Stores the identifier of the identity about which a statement is made or null in case of unsigned attributes.
     */
    private final @Nullable InternalIdentifier subject;
    
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
     * Returns the identifier of the identity about which a statement is made or null in case of unsigned attributes.
     * 
     * @return the identifier of the identity about which a statement is made or null in case of unsigned attributes.
     * 
     * @ensure !isSigned() || return != null : "If this signature is signed, the returned identifier is not null.";
     */
    @Pure
    public final @Nullable InternalIdentifier getNullableSubject() {
        return subject;
    }
    
    /**
     * Returns the identifier of the identity about which a statement is made.
     * 
     * @return the identifier of the identity about which a statement is made.
     * 
     * @require hasSubject() : "This signature has a subject.";
     */
    @Pure
    public final @Nonnull InternalIdentifier getNonNullableSubject() {
        Require.that(subject != null).orThrow("This signature has a subject.");
        
        return subject;
    }
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * Stores the time of the signature generation or null if this signature has no subject.
     */
    private final @Nullable @Positive Time time;
    
    /**
     * Returns the time of the signature generation or null if this signature has no subject.
     * 
     * @return the time of the signature generation or null if this signature has no subject.
     */
    @Pure
    public final @Nullable @Positive Time getNullableTime() {
        return time;
    }
    
    /**
     * Returns the time of the signature generation.
     * 
     * @return the time of the signature generation.
     * 
     * @require hasSubject() : "This signature has a subject.";
     */
    @Pure
    public final @Nonnull @Positive Time getNonNullableTime() {
        Require.that(hasSubject()).orThrow("This signature has a subject.");
        
        Require.that(time != null).orThrow("This then follows from the constructor implementations.");
        return time;
    }
    
    /* -------------------------------------------------- Audit -------------------------------------------------- */
    
    /**
     * Stores the audit or null if no audit is or shall be appended.
     */
    private final @Nullable Audit audit;
    
    /**
     * Returns the audit or null if no audit is or shall be appended.
     * 
     * @return the audit or null if no audit is or shall be appended.
     */
    @Pure
    public final @Nullable Audit getAudit() {
        return audit;
    }
    
    /* -------------------------------------------------- Verified -------------------------------------------------- */
    
    /**
     * Stores whether this signature is verified.
     */
    private boolean verified;
    
    /**
     * Returns whether this signature is verified.
     * 
     * @return whether this signature is verified.
     */
    @Pure
    public final boolean isVerified() {
        return verified;
    }
    
    /**
     * Sets this signature to verified.
     */
    final void setVerified() {
        this.verified = true;
    }
    
    /**
     * Verifies this signature.
     * 
     * @throws InvalidSignatureException if this signature is not valid.
     * 
     * @require !isVerified() : "This signature is not verified.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    @Pure
    @Locked
    @NonCommitting
    public void verify() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(!isVerified()).orThrow("This signature is not verified.");
        
        setVerified();
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new signature wrapper with the given type, element, subject and audit.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit or null if no audit is or shall be appended.
     * 
     * @require element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is either null or based on the parameter of the given type.";
     * 
     * @ensure isVerified() : "This signature is verified.";
     */
    SignatureWrapper(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nullable Block element, @Nullable InternalIdentifier subject, @Nullable Audit audit) {
        super(type);
        
        Require.that(element == null || element.getType().isBasedOn(type.getParameters().getNonNullable(0))).orThrow("The element is either null or based on the parameter of the given type.");
        
        this.element = element;
        this.subject = subject;
        this.time = (subject == null ? null : Time.getCurrent());
        this.audit = audit;
        this.verified = true;
    }
    
    /**
     * Creates a new signature wrapper from the given block.
     * 
     * @param block the block that contains the signed element.
     * @param verified whether the signature is already verified.
     */
    SignatureWrapper(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, boolean verified) throws InvalidEncodingException, InternalException {
        super(block.getType());
        
        this.cache = Block.get(IMPLEMENTATION, block);
        final @Nonnull Block content = TupleWrapper.decode(cache).getNonNullableElement(0);
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(content);
        this.subject = InternalIdentifier.XDF_CONVERTER.decodeNullable(None.OBJECT, tuple.getNullableElement(0));
        if (isSigned() && subject == null) { throw InvalidParameterValueCombinationException.get("The subject may not be null if the element is signed."); }
        this.time = tuple.isElementNull(1) ? null : Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(1));
        if (hasSubject() && time == null) { throw InvalidParameterValueCombinationException.get("The signature time may not be null if this signature has a subject."); }
        if (time != null && !time.isPositive()) { throw InvalidParameterValueException.get("time", time); }
        this.element = tuple.getNullableElement(2);
        if (element != null) { element.setType(block.getType().getParameters().getNonNullable(0)); }
        this.audit = tuple.isElementNull(3) ? null : Audit.get(tuple.getNonNullableElement(3));
        this.verified = verified;
    }
    
    /* -------------------------------------------------- XDF Utility -------------------------------------------------- */
    
    /**
     * Encodes the given element with a new signature wrapper without signing.
     * 
     * @param type the semantic type of the new signature wrapper.
     * @param element the element of the new signature wrapper.
     * @param subject the identifier of the identity about which a statement is made or null if not required.
     * 
     * @return a new signature wrapper with the given arguments.
     * 
     * @require element.getFactory().getType().isBasedOn(type.getParameters().getNonNullable(0)) : "The element is based on the parameter of the given type.";
     * 
     * @ensure return.isVerified() : "The returned signature is verified.";
     */
    @Pure
    public static @Nonnull <V extends XDF<V, ?>> SignatureWrapper encodeWithoutSigning(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type, @Nonnull V element, @Nullable InternalIdentifier subject) {
        return new SignatureWrapper(type, Encode.nonNullable(element), subject, null);
    }
    
    /**
     * Decodes the given block with a new signature wrapper with verifying the signature.
     * 
     * @param block the block that contains the signed element.
     * @param entity the entity that decodes the signature.
     * 
     * @return the signature wrapper of the appropriate subclass.
     * 
     * @ensure return.isVerified() : "The returned signature is verified.";
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull SignatureWrapper decodeWithVerifying(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, @Nullable Entity entity) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull SignatureWrapper signatureWrapper = decodeWithoutVerifying(block, false, entity);
        signatureWrapper.verify();
        return signatureWrapper;
    }
    
    /**
     * Decodes the given block with a new signature wrapper without verifying the signature.
     * 
     * @param block the block that contains the signed element.
     * @param verified whether the signature is already verified.
     * @param entity the entity that decodes the signature.
     * 
     * @return the signature wrapper of the appropriate subclass.
     */
    @Pure
    @Locked
    @NonCommitting
    public static @Nonnull SignatureWrapper decodeWithoutVerifying(@Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block, boolean verified, @Nullable Entity entity) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(Block.get(IMPLEMENTATION, block)).getNullableElements(4);
        final @Nullable Block hostSignature = elements.getNullable(1);
        final @Nullable Block clientSignature = elements.getNullable(2);
        final @Nullable Block credentialsSignature = elements.getNullable(3);
        
        if (hostSignature != null && clientSignature == null && credentialsSignature == null) { return new HostSignatureWrapper(block, hostSignature, verified); }
        if (hostSignature == null && clientSignature != null && credentialsSignature == null) { return new ClientSignatureWrapper(block, clientSignature, verified); }
        if (hostSignature == null && clientSignature == null && credentialsSignature != null) { return new CredentialsSignatureWrapper(block, credentialsSignature, verified, entity); }
        if (hostSignature == null && clientSignature == null && credentialsSignature == null) { return new SignatureWrapper(block, verified); }
        throw InvalidParameterValueCombinationException.get("The element may only be signed either by a host, by a client, with credentials or not at all.");
    }
    
    /* -------------------------------------------------- Checks -------------------------------------------------- */
    
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
    public void checkRecency() throws InactiveAuthenticationException {
        if (time == null || time.isLessThan(Time.HALF_HOUR.ago())) { throw InactiveSignatureException.get(this); }
    }
    
    /* -------------------------------------------------- Signing -------------------------------------------------- */
    
    /**
     * Signs the element. (This method should be overridden in the subclasses.)
     * 
     * @param elements the elements of the wrapped block with the indexes 1 to 3 reserved for the signatures.
     * 
     * @require !elements.isNull(0) : "The first element is not null.";
     */
    void sign(@Nonnull @NullableElements @NonFrozen FreezableArray<Block> elements) {}
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
    /**
     * Stores the signed element.
     */
    private @Nullable @BasedOn("implementation.signature@core.digitalid.net") Block cache;
    
    /**
     * Returns the signed element.
     * 
     * @return the signed element.
     */
    @Pure
    final @Nonnull Block getCache() {
        if (cache == null) {
            final @Nonnull FreezableArray<Block> subelements = FreezableArray.get(4);
            subelements.set(0, Encode.<Identifier>nullable(subject, SUBJECT));
            subelements.set(1, Encode.nullable(time));
            subelements.set(2, element);
            subelements.set(3, Encode.nullable(audit));
            
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(4);
            elements.set(0, TupleWrapper.encode(CONTENT, subelements.freeze()));
            sign(elements);
            cache = TupleWrapper.encode(IMPLEMENTATION, elements.freeze());
        }
        return cache;
    }
    
    @Pure
    @Override
    public final int determineLength() {
        return getCache().getLength();
    }
    
    @Pure
    @Override
    public void encode(@Nonnull @Encoding Block block) {
        Require.that(block.getLength() == determineLength()).orThrow("The block's length has to match the determined length.");
        Require.that(block.getType().isBasedOn(getSyntacticType())).orThrow("The block is based on the indicated syntactic type.");
        
        getCache().writeTo(block);
    }
    
    /* -------------------------------------------------- Casting -------------------------------------------------- */
    
    /**
     * Returns this signature wrapper as a {@link HostSignatureWrapper}.
     * 
     * @return this signature wrapper as a {@link HostSignatureWrapper}.
     * 
     * @throws RequestException if this signature wrapper is not an instance of {@link HostSignatureWrapper}.
     */
    @Pure
    public final @Nonnull HostSignatureWrapper toHostSignatureWrapper() throws RequestException {
        if (this instanceof HostSignatureWrapper) { return (HostSignatureWrapper) this; }
        throw RequestException.get(RequestErrorCode.SIGNATURE, "The element was not signed by a host.");
    }
    
    /**
     * Returns this signature wrapper as a {@link ClientSignatureWrapper}.
     * 
     * @return this signature wrapper as a {@link ClientSignatureWrapper}.
     * 
     * @throws RequestException if this signature wrapper is not an instance of {@link ClientSignatureWrapper}.
     */
    @Pure
    public final @Nonnull ClientSignatureWrapper toClientSignatureWrapper() throws RequestException {
        if (this instanceof ClientSignatureWrapper) { return (ClientSignatureWrapper) this; }
        throw RequestException.get(RequestErrorCode.SIGNATURE, "The element was not signed by a client.");
    }
    
    /**
     * Returns this signature wrapper as a {@link CredentialsSignatureWrapper}.
     * 
     * @return this signature wrapper as a {@link CredentialsSignatureWrapper}.
     * 
     * @throws RequestException if this signature wrapper is not an instance of {@link CredentialsSignatureWrapper}.
     */
    @Pure
    public final @Nonnull CredentialsSignatureWrapper toCredentialsSignatureWrapper() throws RequestException {
        if (this instanceof CredentialsSignatureWrapper) { return (CredentialsSignatureWrapper) this; }
        throw RequestException.get(RequestErrorCode.SIGNATURE, "The element was not signed with credentials.");
    }
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    /**
     * Returns the agent that signed the element or null if no such agent is found.
     * 
     * @param entity the entity whose agent is to be returned.
     * 
     * @return the agent that signed the element or null if no such agent is found.
     * 
     * @see ClientSignatureWrapper#getAgent(net.digitalid.service.core.entity.NonHostEntity) 
     * @see CredentialsSignatureWrapper#getAgent(net.digitalid.service.core.entity.NonHostEntity)
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nullable Agent getAgent(@Nonnull NonHostEntity entity) throws DatabaseException {
        return null;
    }
    
    /**
     * Returns the restricted agent that signed the element.
     * 
     * @param entity the entity whose agent is to be returned.
     * @param publicKey the active public key of the recipient.
     * 
     * @return the restricted agent that signed the wrapped element.
     * 
     * @throws RequestException if no such agent is found or the check failed.
     * 
     * @see ClientSignatureWrapper#getAgentCheckedAndRestricted(net.digitalid.service.core.entity.NonHostEntity, net.digitalid.service.core.cryptography.PublicKey)
     * @see CredentialsSignatureWrapper#getAgentCheckedAndRestricted(net.digitalid.service.core.entity.NonHostEntity, net.digitalid.service.core.cryptography.PublicKey)
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull Agent getAgentCheckedAndRestricted(@Nonnull NonHostEntity entity, @Nullable PublicKey publicKey) throws DatabaseException, RequestException {
        throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The element was not signed by an authorized agent.");
    }
    
    /* -------------------------------------------------- Syntactic Type -------------------------------------------------- */
    
    /**
     * Stores the syntactic type {@code signature@core.digitalid.net}.
     */
    public static final @Nonnull SyntacticType XDF_TYPE = SyntacticType.map("signature@core.digitalid.net").load(1);
    
    @Pure
    @Override
    public final @Nonnull SyntacticType getSyntacticType() {
        return SignatureWrapper.XDF_TYPE;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractWrapper.XDFConverter<SignatureWrapper> {
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        private XDFConverter(@Nonnull @Loaded @BasedOn("signature@core.digitalid.net") SemanticType type) {
            super(type);
        }
        
        @Pure
        @Override
        public @Nonnull SignatureWrapper decodeNonNullable(@Nonnull Object none, @Nonnull @NonEncoding @BasedOn("signature@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
            return new SignatureWrapper(block, false);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull SignatureWrapper.XDFConverter getXDFConverter() {
        return new SignatureWrapper.XDFConverter(getSemanticType());
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull BlockBasedWrapper.SQLConverter<SignatureWrapper> getSQLConverter() {
        return new BlockBasedWrapper.SQLConverter<>(getXDFConverter());
    }
    
}
