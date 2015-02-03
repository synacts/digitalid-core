package ch.virtualid.packet;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.annotations.RawRecipient;
import ch.virtualid.cache.AttributesQuery;
import ch.virtualid.cache.AttributesReply;
import ch.virtualid.certificate.CertificateIssue;
import ch.virtualid.client.AccountInitialize;
import ch.virtualid.client.AccountOpen;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.HostAccount;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InactiveSignatureException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.IdentityQuery;
import ch.virtualid.identity.Predecessors;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.Successor;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.server.Server;
import ch.virtualid.synchronizer.Audit;
import ch.virtualid.synchronizer.RequestAudit;
import ch.virtualid.synchronizer.ResponseAudit;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.CompressionWrapper;
import ch.xdf.EncryptionWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A packet compresses, signs and encrypts requests and responses.
 * 
 * @see Request
 * @see Response
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Packet implements Immutable {
    
    /**
     * Stores the semantic type {@code content.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType CONTENT = SemanticType.create("content.packet@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code compression.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType COMPRESSION = SemanticType.create("compression.packet@virtualid.ch").load(CompressionWrapper.TYPE, CONTENT);
    
    /**
     * Stores the semantic type {@code signature.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SIGNATURE = SemanticType.create("signature.packet@virtualid.ch").load(SignatureWrapper.TYPE, COMPRESSION);
    
    /**
     * Stores the semantic type {@code list.signature.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SIGNATURES = SemanticType.create("list.signature.packet@virtualid.ch").load(ListWrapper.TYPE, SIGNATURE);
    
    /**
     * Stores the semantic type {@code encryption.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType ENCRYPTION = SemanticType.create("encryption.packet@virtualid.ch").load(EncryptionWrapper.TYPE, SIGNATURES);
    
    /**
     * Stores the semantic type {@code packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("packet@virtualid.ch").load(SelfcontainedWrapper.SELFCONTAINED);
    
    
    /**
     * Stores the wrapper of this packet.
     * 
     * @invariant wrapper.getType().isBasedOn(SelfcontainedWrapper.SELFCONTAINED) : "The wrapper is based on the selfcontained type.";
     */
    private final @Nonnull SelfcontainedWrapper wrapper;
    
    /**
     * Stores the encryption of this packet.
     * 
     * @invariant encryption.getType().equals(Packet.ENCRYPTION) : "The encryption has the encryption type.";
     */
    private final @Nonnull EncryptionWrapper encryption;
    
    /**
     * Stores the audit of this packet.
     * 
     * @invariant !(this instanceof Request) || audit == null || audit instanceof RequestAudit : "If this is a request, the audit is either null or a request audit.";
     * @invariant !(this instanceof Response) || audit == null || audit instanceof ResponseAudit : "If this is a response, the audit is either null or a response audit.";
     */
    private final @Nullable Audit audit;
    
    /**
     * Stores the number of elements.
     */
    private final int size;
    
    /**
     * Packs the handlers in the given object with the given arguments for encrypting and signing.
     * 
     * @param list an object that contains the handlers and is passed back with {@link #setList(java.lang.Object)}.
     * @param size the number of handlers in the given object, which has to be positive (that means greater than zero).
     * @param field an object that contains the signing parameter and is passed back with {@link #setField(java.lang.Object)}.
     * @param recipient the identifier of the host for which the content is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
     * @param subject the identifier of the identity about which a statement is made in a method or a reply.
     * @param audit the audit with the time of the last retrieval or null in case of external requests.
     * 
     * @require !(this instanceof Request) || audit == null || audit instanceof RequestAudit : "If this is a request, the audit is either null or a request audit.";
     * @require !(this instanceof Response) || audit == null || audit instanceof ResponseAudit : "If this is a response, the audit is either null or a response audit.";
     */
    @NonCommitting
    @SuppressWarnings("AssignmentToMethodParameter")
    Packet(@Nonnull Object list, int size, @Nullable Object field, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nullable InternalIdentifier subject, @Nullable Audit audit) throws SQLException, IOException, PacketException, ExternalException {
        assert !(this instanceof Request) || audit == null || audit instanceof RequestAudit : "If this is a request, the audit is either null or a request audit.";
        assert !(this instanceof Response) || audit == null || audit instanceof ResponseAudit : "If this is a response, the audit is either null or a response audit.";
        
        setList(list);
        setField(field);
        this.size = size;
        this.audit = audit;
        
        final @Nonnull FreezableList<Block> signatures = new FreezableArrayList<Block>(size);
        for (int i = 0; i < size; i++) {
            final @Nullable Block block = getBlock(i);
            final @Nullable SelfcontainedWrapper content = block == null ? null : new SelfcontainedWrapper(CONTENT, block);
            final @Nullable CompressionWrapper compression = content == null ? null : new CompressionWrapper(COMPRESSION, content, CompressionWrapper.ZLIB);
            if (compression != null || audit != null) {
                if (subject == null || audit == null && block != null && block.getType().equals(PacketException.TYPE))
                    signatures.add(new SignatureWrapper(Packet.SIGNATURE, compression, subject).toBlock());
                else signatures.add(getSignature(compression, subject, audit).toBlock());
                audit = null;
            } else {
                signatures.add(null);
            }
        }
        
        this.encryption = new EncryptionWrapper(ENCRYPTION, new ListWrapper(SIGNATURES, signatures.freeze()), recipient, symmetricKey);
        this.wrapper = new SelfcontainedWrapper(TYPE, encryption);
    }
    
    /**
     * Reads and unpacks the packet from the given input stream.
     * 
     * @param inputStream the input stream to read the packet from.
     * @param request the corresponding request in case of a response or null if a request is unpacked on the host-side.
     * @param verified determines whether the signature is verified (if not, it needs to be checked by the caller).
     * 
     * @require (request == null) == (this instanceof Request) : "If the request is null, this packet is itself a request.";
     * @require (request != null) == (this instanceof Response) : "If the request is not null, this packet is a response.";
     */
    @NonCommitting
    Packet(@Nonnull InputStream inputStream, @Nullable Request request, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        assert (request == null) == (this instanceof Request) : "If the request is null, this packet is itself a request.";
        assert (request != null) == (this instanceof Response) : "If the request is not null, this packet is a response.";
        
        final boolean isResponse = (this instanceof Response);
        final @Nullable Response response = isResponse ? (Response) this : null;
        try { this.wrapper = new SelfcontainedWrapper(inputStream, false); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.PACKET, "The packet could not be decoded.", exception, isResponse); }
        
        try { this.encryption = new EncryptionWrapper(wrapper.getElement().checkType(ENCRYPTION), isResponse ? request.getEncryption().getSymmetricKey() : null); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.ENCRYPTION, "The encryption could not be decoded.", exception, isResponse); }
        Replay.check(encryption);
        
        final @Nullable HostIdentifier recipient = encryption.getRecipient();
        if (isResponse != (recipient == null)) throw new PacketException(PacketError.ENCRYPTION, "The recipient of a request may not be null.", null, isResponse);
        final @Nullable HostAccount account = recipient == null ? null : Server.getHost(recipient).getAccount();
        
        final @Nonnull ReadonlyList<Block> elements;
        try { elements = new ListWrapper(encryption.getElementNotNull()).getElements(); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.ELEMENTS, "The elements could not be decoded.", exception, isResponse); }
        
        this.size = elements.size();
        if (size == 0) throw new PacketException(PacketError.ELEMENTS, "The encryption of a packet must contain at least one element.", null, isResponse);
        if (isResponse && size > request.getSize()) throw new PacketException(PacketError.ELEMENTS, "The response contains more elements than the request.", null, isResponse);
        
        initialize(size);
        
        @Nullable Audit audit = null;
        @Nullable SignatureWrapper reference = null;
        for (int i = 0; i < size; i++) {
            if (elements.isNotNull(i)) {
                final @Nonnull SignatureWrapper signature;
                try { signature = verified ? SignatureWrapper.decode(elements.getNotNull(i), account) : SignatureWrapper.decodeWithoutVerifying(elements.getNotNull(i), false, account); } catch (InvalidEncodingException | InvalidSignatureException exception) { throw new PacketException(PacketError.SIGNATURE, "A signature is invalid.", exception, isResponse); }
                try { signature.checkRecency(); } catch (InactiveSignatureException exception) { throw new PacketException(PacketError.SIGNATURE, "One of the signatures is no longer active.", exception, isResponse); }
                
                final @Nullable Audit _audit = signature.getAudit();
                if (_audit != null) {
                    audit = isResponse ? _audit.toResponseAudit() : _audit.toRequestAudit();
                    if (signature.isNotSigned()) throw new PacketException(PacketError.SIGNATURE, "A packet that contains an audit has to be signed.");
                }
                
                final @Nullable Block element = signature.getElement();
                if (element != null) {
                    final @Nonnull CompressionWrapper compression;
                    try { compression = new CompressionWrapper(element); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.COMPRESSION, "The compression could not be decoded.", exception, isResponse); }
                    
                    final @Nonnull SelfcontainedWrapper content;
                    try { content = new SelfcontainedWrapper(compression.getElementNotNull()); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.CONTENT, "The content could not be decoded.", exception, isResponse); }
                    
                    final @Nonnull Block block = content.getElement();
                    final @Nonnull SemanticType type = block.getType();
                    if (response != null) {
                        if (signature.hasSubject() && !signature.getSubjectNotNull().equals(request.getSubject())) throw new PacketException(PacketError.IDENTIFIER, "The subject of the request was " + request.getSubject() + ", the response from " + request.getRecipient() + " was about " + signature.getSubjectNotNull() + " though.", null, isResponse);
                        
                        if (signature.isSigned()) {
                            if (reference == null) reference = signature;
                            else if (!signature.isSignedLike(reference)) throw new PacketException(PacketError.SIGNATURE, "All the signed signatures of a response have to be signed alike.", null, isResponse);
                            
                            if (signature instanceof HostSignatureWrapper) {
                                final @Nonnull Identifier signer = ((HostSignatureWrapper) signature).getSigner();
                                if (!signer.equals(request.getRecipient())) throw new PacketException(PacketError.SIGNATURE, "The response from the host " + request.getRecipient() + " was signed by " + signer + ".", null, isResponse);
                                
                                if (type.equals(PacketException.TYPE)) {
                                    response.setException(i, PacketException.create(block));
                                } else {
                                    final @Nonnull Method method = request.getMethod(i);
                                    final @Nonnull Reply reply = Reply.get(method.hasEntity() ? method.getNonHostEntity() : null, (HostSignatureWrapper) signature, block);
                                    if (!method.matches(reply)) throw new PacketException(PacketError.REPLY, "A reply does not match its corresponding method.", null, isResponse);
                                    response.setReply(i, reply);
                                }
                            } else throw new PacketException(PacketError.SIGNATURE, "A reply from the host " + request.getRecipient() + " was not signed by a host.", null, isResponse);
                        } else {
                            if (type.equals(PacketException.TYPE)) response.setException(i, PacketException.create(block));
                            else throw new PacketException(PacketError.SIGNATURE, "A reply from the host " + request.getRecipient() + " was not signed.", null, isResponse);
                        }
                    } else {
                        if (!signature.hasSubject()) throw new PacketException(PacketError.SIGNATURE, "Each signature in a request must have a subject.", null, isResponse);
                        final @Nonnull InternalIdentifier subject = signature.getSubjectNotNull();
                        if (subject instanceof HostIdentifier && !type.equals(AttributesQuery.TYPE) && !type.equals(CertificateIssue.TYPE)) throw new PacketException(PacketError.METHOD, "A host can only be the subject of an attributes query and a certificate issuance but not " + type.getAddress() + ".", null, isResponse);
                        
                        if (reference == null) reference = signature;
                        else if (!signature.isSignedLike(reference)) throw new PacketException(PacketError.SIGNATURE, "All the signatures of a request have to be signed alike.", null, isResponse);
                        
                        final @Nonnull Entity entity;
                        assert recipient != null && account != null : "In case of requests, both the recipient and the account are set (see the code above).";
                        if (type.equals(IdentityQuery.TYPE) || type.equals(AccountOpen.TYPE)) {
                            entity = account;
                        } else {
                            entity = Account.get(account.getHost(), subject.getIdentity());
                            if (subject instanceof InternalNonHostIdentifier) {
                                final @Nonnull InternalNonHostIdentifier internalNonHostIdentifier = (InternalNonHostIdentifier) subject;
                                if (!type.equals(AccountInitialize.TYPE) && !Predecessors.exist(internalNonHostIdentifier)) throw new PacketException(PacketError.IDENTIFIER, "The subject " + subject + " is not yet initialized.");
                                final @Nullable InternalNonHostIdentifier successor = Successor.get(internalNonHostIdentifier);
                                if (successor != null) throw new PacketException(PacketError.RELOCATION, "The subject " + subject + " has been relocated to " + successor + ".", null, isResponse);
                            }
                        }
                        final @Nonnull Method method = Method.get(entity, signature, recipient, block);
                        if (!account.getHost().supports(method.getService())) throw new PacketException(PacketError.METHOD, "The host " + recipient + " does not support the service '" + method.getService().getName() + "'.", null, isResponse);
                        ((Request) this).setMethod(i, method);
                    }
                    continue;
                }
            }
            
            if (response == null) throw new PacketException(PacketError.ELEMENTS, "None of the elements may be null in requests.", null, isResponse);
            else if (!request.getMethod(i).matches(null)) throw new PacketException(PacketError.REPLY, "A reply was expected but none was received.", null, isResponse);
        }
        
        if (response != null && size < request.getSize()) {
            response.getReply(0); // If the first element encodes a packet error, it is thrown by retrieving the reply.
            throw new PacketException(PacketError.ELEMENTS, "The response contains fewer elements than the request.", null, isResponse);
        }
        
        if (!encryption.isEncrypted()) {
            if (size > 1) throw new PacketException(PacketError.ELEMENTS, "If the packet is not encrypted, only one element may be provided.", null, isResponse);
            
            if (response != null) {
                final @Nullable Reply reply = response.getReply(0); // If the only element encodes a packet error, it is thrown by retrieving the reply.
                if (!(reply instanceof AttributesReply && reply.getSubject() instanceof HostIdentifier)) throw new PacketException(PacketError.ENCRYPTION, "The response should be encrypted but is not.", null, isResponse);
            } else {
                final @Nonnull Method method = ((Request) this).getMethod(0);
                if (!(method instanceof AttributesQuery && method.getSubject() instanceof HostIdentifier)) throw new PacketException(PacketError.ENCRYPTION, "The request should be encrypted but is not.", null, isResponse);
            }
        }
        
        if (isResponse) {
            if (audit == null && request.getAudit() != null) throw new PacketException(PacketError.AUDIT, "An audit was requested but none was received.", null, isResponse);
            if (audit != null && request.getAudit() == null) throw new PacketException(PacketError.AUDIT, "No audit was requested but one was received.", null, isResponse);
        }
        this.audit = audit;
        
        freeze();
    }
    
    
    /**
     * Returns the encryption of this packet.
     * 
     * @return the encryption of this packet.
     * 
     * @ensure encryption.getType().equals(Packet.ENCRYPTION) : "The encryption has the encryption type.";
     */
    @Pure
    public final @Nonnull EncryptionWrapper getEncryption() {
        return encryption;
    }
    
    /**
     * Returns the audit of this packet.
     * 
     * @return the audit of this packet.
     * 
     * @ensure !(this instanceof Request) || return == null || return instanceof RequestAudit : "If this is a request, the returned audit is either null or a request audit.";
     * @ensure !(this instanceof Response) || return == null || return instanceof ResponseAudit : "If this is a response, the returned audit is either null or a response audit.";
     */
    @Pure
    public @Nullable Audit getAudit() {
        return audit;
    }
    
    /**
     * Returns the number of handlers and exceptions.
     * 
     * @return the number of handlers and exceptions.
     * 
     * @ensure return > 0 : "The size is always positive.";
     */
    @Pure
    public final int getSize() {
        return size;
    }
    
    /**
     * Writes the packet to the given output stream.
     * 
     * @param outputStream the output stream to write to.
     */
    public final void write(@Nonnull OutputStream outputStream) throws IOException {
        wrapper.write(outputStream, false);
    }
    
    
    /**
     * Sets the list of the request or response.
     * 
     * @param list the object containing the list.
     */
    @RawRecipient
    abstract void setList(@Nonnull Object list);
    
    /**
     * Sets the field of the request or response.
     * 
     * @param field the object containing the field.
     */
    @RawRecipient
    abstract void setField(@Nullable Object field);
    
    /**
     * Returns the handler or exception at the given position as a block.
     * 
     * @param index the index of the block which is to be returned.
     * 
     * @return the handler or exception at the given position as a block.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @Pure
    @RawRecipient
    abstract @Nullable Block getBlock(int index);
    
    /**
     * Returns the signature of the given compression and audit.
     * 
     * @param compression the compression of the element to be signed.
     * @param subject the subject about which the returned signature is.
     * @param audit the audit which is to be included in the signature.
     * 
     * @return the signature of the given compression and audit.
     * 
     * @require compression != null || audit != null : "The compression or the audit is not null.";
     */
    @Pure
    @RawRecipient
    @NonCommitting
    abstract @Nonnull SignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Initializes the required lists with the given size.
     * 
     * @param size the number of elements in this packet.
     */
    @RawRecipient
    abstract void initialize(int size);
    
    /**
     * Freezes the populated lists at the end of the constructor.
     */
    @RawRecipient
    abstract void freeze();
    
}
