package ch.virtualid.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.SecretCommitment;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.entity.Account;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Handler;
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
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A packet compresses, signs and encrypts requests and responses.
 * 
 * @invariant getSize() == handlers.size() && getSize() == exceptions.size() : "The number of handlers and exceptions are the same.";
 * 
 * @see Request
 * @see Response
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class Packet implements Immutable {
    
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
     */
    private final @Nonnull SelfcontainedWrapper wrapper;
    
    /**
     * Stores the encryption of this packet.
     */
    private final @Nonnull EncryptionWrapper encryption;
    
    /**
     * Stores the audit of this packet.
     */
    private final @Nullable Audit audit;
    
    /**
     * Stores the number of handlers and exceptions.
     */
    private final int size;
    
    /**
     * Stores the handlers of this packet.
     * 
     * @invariant handlers.isFrozen() : "The handlers are frozen.";
     */
    private final @Nonnull ReadonlyList<? extends Handler> handlers;
    
    /**
     * Stores the exceptions of this packet.
     * 
     * @invariant exceptions.isFrozen() : "The exceptions are frozen.";
     */
    private final @Nonnull ReadonlyList<PacketException> exceptions;
    
    /**
     * Packs the given packet exception as a response without signing.
     * 
     * @param exception the packet exception that is to be packed as an unsigned response.
     * @param symmetricKey the symmetric key used for encryption or null if the response is not encrypted.
     * 
     * @ensure getSize() == 1 : "The size of this packet is one.";
     */
    public Packet(@Nonnull PacketException exception, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        this(new FreezableArrayList<Handler>(1).freeze(), new FreezableArrayList<PacketException>(exception).freeze(), null, symmetricKey, null, null, null, null, null, null, false, null);
    }
    
    /**
     * Packs the given contents with the given arguments as a response signed by the given host.
     * 
     * @param handlers a list of selfcontained wrappers whose blocks are to be packed as a response.
     * @param symmetricKey the symmetric key used for encryption or null if the response is not encrypted.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit since the last audit or null if no audit is appended.
     * @param signer the identifier of the signing host.
     * 
     * @require handlers.isFrozen() : "The list of handlers is frozen.";
     * @require exceptions.isFrozen() : "The list of exceptions is frozen.";
     * @require handlers.isNotEmpty() : "The list of handlers is not empty.";
     * @require handlers.size() == exceptions.size() : "The number of handlers and exceptions are the same.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * 
     * @ensure getSize() == handlers.size() : "The size of this packet equals the size of the handlers.";
     */
    public Packet(@Nonnull ReadonlyList<? extends Handler> handlers, @Nonnull ReadonlyList<PacketException> exceptions, @Nullable SymmetricKey symmetricKey, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull HostIdentifier signer) throws SQLException, IOException, PacketException, ExternalException {
        this(handlers, exceptions, null, symmetricKey, subject, audit, signer, null, null, null, false, null);
    }
    
    /**
     * Packs the given handlers with the given arguments for encrypting and signing.
     * 
     * @param handlers the handlers that are to be packed as either a request or a response, where they can also be null.
     * @param exceptions the packet exceptions that are sent instead of the handlers at positions where they are not null.
     * @param recipient the identifier of the host for which the content is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
     * @param subject the identifier of the identity about which a statement is made in a method or a reply.
     * @param audit the audit since the last audit or null in case of external methods or replies.
     * @param signer the identifier of the signing host or null if the element is not signed by a host.
     * @param commitment the commitment containing the client secret or null if the element is not signed by a client.
     * @param credentials the credentials with which the content is signed or null if the content is not signed with credentials.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not shortened.
     * 
     * @require handlers.isFrozen() : "The list of handlers is frozen.";
     * @require exceptions.isFrozen() : "The list of exceptions is frozen.";
     * @require handlers.isNotEmpty() : "The list of handlers is not empty.";
     * @require handlers.size() == exceptions.size() : "The number of handlers and exceptions are the same.";
     * @require subject != null || recipient == null && signer == null && commitment == null && credentials == null : "The subject may only be null if the contents of a response are not signed (because the host could not decode the subject).";
     * @require ... : "This list of preconditions is not complete but the public constructors make sure that all requirements for packing the given handlers are met.";
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    protected Packet(@Nonnull ReadonlyList<? extends Handler> handlers, @Nonnull ReadonlyList<PacketException> exceptions, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nullable Identifier subject, @Nullable Audit audit, @Nullable HostIdentifier signer, @Nullable SecretCommitment commitment, @Nullable ReadonlyList<Credential> credentials, @Nullable ReadonlyList<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws SQLException, IOException, PacketException, ExternalException {
        assert handlers.isFrozen() : "The list of handlers is frozen.";
        assert exceptions.isFrozen() : "The list of exceptions is frozen.";
        assert handlers.isNotEmpty() : "The list of handlers is not empty.";
        assert handlers.size() == exceptions.size() : "The number of handlers and exceptions are the same.";
        assert subject != null || recipient == null && signer == null && commitment == null && credentials == null : "The subject may only be null if the contents of a response are not signed (because the host could not decode the subject).";
        
        this.audit = audit;
        this.size = handlers.size();
        this.handlers = handlers;
        this.exceptions = exceptions;
        
        final @Nonnull FreezableList<Block> signatures = new FreezableArrayList<Block>(handlers.size());
        for (int i = 0; i < size; i++) {
            @Nullable Block block = null;
            if (exceptions.isNotNull(i)) block = exceptions.getNotNull(i).toBlock();
            else if (handlers.isNotNull(i)) block = handlers.getNotNull(i).toBlock();
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
     * Unpacks the given request on the host-side.
     * 
     * @param wrapper the request to unpack with the selfcontained wrapper containing an element of type {@code packet@virtualid.ch}.
     * 
     * @ensure for (@Nonnull SignatureWrapper signature : getSignatures()) signature.getSubject() != null : "The subjects of the signatures are not null.";
     * @ensure for (@Nonnull SignatureWrapper signature : getSignatures()) signature.isSignedLike(reference) : "All signatures that are signed are signed alike.";
     * @ensure getEncryption().getRecipient() != null : "The recipient of the request is not null.";
     */
    public Packet(@Nonnull SelfcontainedWrapper wrapper) throws PacketException {
        this(wrapper, null, true, false);
        if (encryption.getRecipient() == null) throw new PacketException(PacketError.ENCRYPTION, new InvalidEncodingException("The recipient of a request is not null."));
    }
    
    /**
     * Unpacks the given packet with the given symmetric key.
     * 
     * @param wrapper the wrapper to unpack containing an element of type {@code packet@virtualid.ch}.
     * @param symmetricKey the symmetric key to decrypt the content or null, if the content is encrypted for a host or not at all.
     * @param verification determines whether the signature is verified (if not, it needs to be checked explicitly).
     * @param response whether the packet is a response, in which case all contents that are neither a packet error nor a no-reply need to be signed.
     * 
     * @ensure for (SignatureWrapper signature : getSignatures()) signature.getSubject() != null : "The subjects of the signatures are not null.";
     * @ensure for (SignatureWrapper signature : getSignatures()) signature.isSignedLike(reference) : "All signatures that are signed are signed alike.";
     * @ensure for (SignatureWrapper signature : getSignatures()) !response || signature instanceof HostSignatureWrapper : "All signatures of a response that are signed are signed by a host.";
     */
    protected Packet(@Nonnull SelfcontainedWrapper wrapper, @Nullable SymmetricKey symmetricKey, boolean verification, boolean response) throws PacketException {
        this.wrapper = wrapper;
        try { this.encryption = new EncryptionWrapper(wrapper.getElement(), symmetricKey); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.ENCRYPTION, exception); }
        
        // TODO: If this constructor is called from the Response class, then set the remote flag of the packet exceptions!
        
        final @Nullable HostIdentifier recipient = encryption.getRecipient();
        final @Nullable Account account = recipient == null ? null : Server.getHost(recipient).getAccount();
        
        @Nonnull List<Block> elements;
        try { elements = new ListWrapper(encryption.getElement()).getElements(); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.SIGNATURE, exception); }
        size = elements.size();
        if (size == 0) throw new PacketException(PacketError.PACKET, new InvalidEncodingException("The encryption of a packet must contain at least one signature."));
        signatures = new ArrayList<SignatureWrapper>(size);
        compressions = new ArrayList<CompressionWrapper>(size);
        handlers = new ArrayList<SelfcontainedWrapper>(size);
        
        @Nullable SignatureWrapper reference = null;
        for (int i = 0; i < size; i++) {
            @Nonnull SignatureWrapper signature;
            @Nonnull CompressionWrapper compression;
            @Nonnull SelfcontainedWrapper content;
            
            try { signature = verification ? SignatureWrapper.decode(encryption.getElement(), account) : SignatureWrapper.decodeUnverified(encryption.getElement(), account); } catch (InvalidEncodingException | InvalidSignatureException | IdentityNotFoundExceptionexception) { throw new PacketException(PacketError.SIGNATURE, exception); }
            if (signature.getSubject() == null) throw new PacketException(PacketError.SIGNATURE, new InvalidEncodingException("The subject of a signature is not null.")); // This exception is also thrown (intentionally) on the requester if the responding host could not decode the subject.
            try { compression = new CompressionWrapper(signature.getElement()); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.COMPRESSION, exception); }
            try { content = new SelfcontainedWrapper(compression.getElement()); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.REQUEST, exception); }
            
            if (signature.isSigned()) {
                if (reference == null) reference = signature;
                else if (!signature.isSignedLike(reference)) throw new PacketException(PacketError.SIGNATURE, new InvalidEncodingException("All the (signed) signatures of a packet have to be signed in the same way."));
                if (response && !(signature instanceof HostSignatureWrapper)) throw new PacketException(PacketError.SIGNATURE, new InvalidSignatureException("The response from the host " + encryption.getRecipient() + " was not signed by a host."));
            } else if (response) try {
                @Nonnull SemanticType type = content.getIdentifier().getIdentity().toSemanticType();
                if (!type.equals(SemanticType.PACKET_ERROR) && !type.equals(SemanticType.NOREPLY)) throw new PacketException(PacketError.SIGNATURE, new InvalidEncodingException("The response from the host " + encryption.getRecipient() + " was not signed."));
            } catch (InvalidEncodingException | IdentityNotFoundExceptionexception) { throw new PacketException(PacketError.SIGNATURE, exception); }
            
            signatures.set(i, signature);
            compressions.set(i, compression);
            handlers.set(i, content);
        }
    }
    
    
    /**
     * Returns the wrapper of this packet.
     * 
     * @return the wrapper of this packet.
     */
    public final @Nonnull SelfcontainedWrapper getWrapper() {
        return wrapper;
    }
    
    /**
     * Returns the encryption of this packet.
     * 
     * @return the encryption of this packet.
     */
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
     * Returns the handler at the given position in this packet or null if there was no reply.
     * 
     * @param index the index of the handler which is to be returned.
     * 
     * @return the handler at the given position in this packet or null if there was no reply.
     * 
     * @throws PacketException if the responding host encountered a packet error.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    public final @Nullable Handler getHandler(int index) throws PacketException, IdentityNotFoundException, InvalidEncodingException {
        assert index >= 0 && index < getSize() : "The index is valid.";
        
        if (exceptions.isNotNull(index)) throw exceptions.getNotNull(index);
        else return handlers.get(index);
    }
    
    /**
     * Writes the packet to the given output stream.
     * 
     * @param outputStream the output stream to write to.
     */
    public final void write(@Nonnull OutputStream outputStream) throws IOException {
        wrapper.write(outputStream, false);
    }
    
}
