package ch.virtualid.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.annotations.RawRecipient;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.entity.Account;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.server.Server;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CompressionWrapper;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.EncryptionWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
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
 * @version 2.0
 */
abstract class Packet implements Immutable {
    
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
     */
    private final @Nullable Audit audit;
    
    /**
     * Stores the number of elements.
     */
    private final int size;
    
    /**
     * Packs the given handlers with the given arguments for encrypting and signing.
     * 
     * @param handlers the handlers that are to be packed as either a request or a response, where they can also be null.
     * @param exceptions the packet exceptions that are sent instead of the handlers at positions where they are not null.
     * @param recipient the identifier of the host for which the content is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
     * @param subject the identifier of the identity about which a statement is made in a method or a reply.
     * @param audit the audit with the time of the last retrieval or null in case of external requests.
     * @param signer the identifier of the signing host or null if the element is not signed by a host.
     * @param commitment the commitment containing the client secret or null if the element is not signed by a client.
     * @param credentials the credentials with which the content is signed or null if the content is not signed with credentials.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not shortened.
     * 
     * @require subject != null || recipient == null && signer == null && commitment == null && credentials == null : "The subject may only be null if the contents of a response are not signed (because the host could not decode the subject).";
     * @require ... : "This list of preconditions is not complete but the public constructors make sure that all requirements for packing the given handlers are met.";
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    Packet(@Nonnull Object object, int size, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nullable Identifier subject, @Nullable Audit audit, @Nullable HostIdentifier signer, @Nullable SecretCommitment commitment, @Nullable ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws SQLException, IOException, PacketException, ExternalException {
        assert subject != null || recipient == null && signer == null && commitment == null && credentials == null : "The subject may only be null if the contents of a response are not signed (because the host could not decode the subject).";
        
        setLists(object);
        this.audit = audit;
        this.size = size;
        
        final @Nonnull FreezableList<Block> signatures = new FreezableArrayList<Block>(size);
        for (int i = 0; i < size; i++) {
            final @Nullable Block block = getBlock(i);
            final @Nullable SelfcontainedWrapper content = block == null ? null : new SelfcontainedWrapper(CONTENT, block);
            final @Nullable CompressionWrapper compression = content == null ? null : new CompressionWrapper(COMPRESSION, content, CompressionWrapper.ZLIB);
            if (compression != null || audit != null) {
                if (signer != null && exceptions.isNull(i)) {
                    signatures.set(i, new HostSignatureWrapper(SIGNATURE, compression, subject, audit, signer).toBlock());
                } else if (commitment != null) {
                    signatures.set(i, new ClientSignatureWrapper(SIGNATURE, compression, subject, audit, commitment).toBlock());
                } else if (credentials != null) {
                    signatures.set(i, new CredentialsSignatureWrapper(SIGNATURE, compression, subject, audit, credentials, certificates, lodged, value).toBlock());
                } else {
                    signatures.set(i, new SignatureWrapper(SIGNATURE, compression, subject).toBlock());
                }
                audit = null;
            } else {
                signatures.set(i, null);
            }
        }
        
        this.encryption = new EncryptionWrapper(ENCRYPTION, new ListWrapper(SIGNATURES, signatures.freeze()), recipient, symmetricKey);
        this.wrapper = new SelfcontainedWrapper(TYPE, encryption);
    }
    
    
    /**
     * Reads and unpacks the packet from the given input stream.
     * 
     * @param inputStream the input stream to read the packet from.
     * @param request the corresponding request in case of a response or null if the packet is unpacked on the host-side.
     * @param verification determines whether the signature is verified (if not, it needs to be checked explicitly).
     */
    Packet(@Nonnull InputStream inputStream, @Nullable Request request, boolean verification) throws SQLException, IOException, PacketException, ExternalException {
        final boolean response = (request != null || this instanceof Response);
        assert this != null;
        try { this.wrapper = new SelfcontainedWrapper(inputStream, false); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.PACKET, "The packet could not be decoded.", exception, response); }
        try { this.encryption = new EncryptionWrapper(wrapper.getElement().checkType(ENCRYPTION), request == null ? null : request.getEncryption().getSymmetricKey()); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.ENCRYPTION, "The encryption could not be decoded.", exception, response); }
        
        final @Nullable HostIdentifier recipient = encryption.getRecipient();
        final @Nullable Account account = recipient == null ? null : Server.getHost(recipient).getAccount();
        
        final @Nonnull ReadonlyList<Block> elements;
        try { elements = new ListWrapper(encryption.getElement()).getElements(); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.ELEMENTS, "The elements could not be decoded.", exception, response); }
        
        this.size = elements.size();
        if (size == 0) throw new PacketException(PacketError.PACKET, "The encryption of a packet must contain at least one element.", null, response);
        
        initialize(size);
        
        @Nullable Audit audit = null;
        @Nullable SignatureWrapper reference = null;
        for (int i = 0; i < size; i++) {
            if (elements.isNotNull(i)) {
                final @Nonnull SignatureWrapper signature;
                try { signature = verification ? SignatureWrapper.decode(elements.getNotNull(i), account) : SignatureWrapper.decodeUnverified(elements.getNotNull(i), account); } catch (InvalidEncodingException | InvalidSignatureException exception) { throw new PacketException(PacketError.SIGNATURE, "A signature could not be decoded.", exception, response); }
                if (signature.getSubject() == null) throw new PacketException(PacketError.SIGNATURE, "The subject of a signature may not be null.", null, response); // This exception is also thrown (intentionally) on the requester if the responding host could not decode the subject.
                if (signature.getAudit() != null) audit = signature.getAudit();
                
                final @Nonnull CompressionWrapper compression;
                try { compression = new CompressionWrapper(signature.getElementNotNull()); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.COMPRESSION, "The compression could not be decoded.", exception, response); }
                
                final @Nonnull SelfcontainedWrapper content;
                try { content = new SelfcontainedWrapper(compression.getElementNotNull()); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.CONTENT, "The content could not be decoded.", exception, response); }
                
                if (signature.isSigned()) {
                    if (reference == null) reference = signature;
                    else if (!signature.isSignedLike(reference)) throw new PacketException(PacketError.SIGNATURE, "All the (signed) signatures of a packet have to be signed alike.", null, response);
                    if (request != null) {
                        if (signature instanceof HostSignatureWrapper) {
                            final @Nonnull Reply reply = Reply.get(account, (HostSignatureWrapper) signature, content.getElement());
                            final @Nullable Class<? extends Reply> replyClass = ((Method) request.getHandler(i)).getReplyClass();
                            if () // TODO
                            handlers.set(i, reply);
                        }
                        else throw new PacketException(PacketError.SIGNATURE, "A reply from the host " + encryption.getRecipient() + " was not signed by a host.", null, response);
                    }
                } else {
                    if (response && !content.getElement().getType().isBasedOn(PacketException.TYPE)) throw new PacketException(PacketError.SIGNATURE, "A reply from the host " + encryption.getRecipient() + " was not signed.", null, response);
                }
                
                handlers.set(i, content);
            } else {
                if (!response) throw new PacketException(PacketError.ELEMENTS, "None of the elements may be null in requests.", null, response);
            }
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
     */
    @Pure
    public final @Nullable Audit getAudit() {
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
     * Sets the list(s) of the request or response.
     * 
     * @param object the object containing the list(s).
     */
    @RawRecipient
    abstract void setLists(@Nonnull Object object);
    
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
