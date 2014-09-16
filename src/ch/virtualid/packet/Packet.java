package ch.virtualid.packet;

import ch.virtualid.client.Commitment;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CompressionWrapper;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.EncryptionWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.Int8Wrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.FailedEncodingException;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.exceptions.InvalidSignatureException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A packet compresses, signs and encrypts requests and responses.
 * 
 * @invariant getSize() == getSignatures().size() && getSize() == getCompressions().size() && getSize() == getContents().size() : "The number of signatures, compressions and contents is the same.";
 * 
 * @see Request
 * @see Response
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
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
    public static final @Nonnull SemanticType TYPE = SemanticType.create("packet@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    
    /**
     * Stores the wrapper of the packet.
     */
    private final @Nonnull SelfcontainedWrapper wrapper;
    
    /**
     * Stores the wrapper of the encryption.
     */
    private final @Nonnull EncryptionWrapper encryption;
    
    /**
     * Stores the wrappers of the signatures.
     */
    private final @Nonnull List<SignatureWrapper> signatures;
    
    /**
     * Stores the wrappers of the compressions.
     */
    private final @Nonnull List<CompressionWrapper> compressions;
    
    /**
     * Stores the wrappers of the contents.
     */
    private final @Nonnull List<SelfcontainedWrapper> contents;
    
    /**
     * Stores the number of signatures, compressions and contents.
     */
    private final int size;
    
    
    /**
     * Packs the given content as a response without signing (only for packet errors).
     * 
     * @param content a selfcontained wrapper whose block is to be packed as a response.
     * @param symmetricKey the symmetric key used for encryption or null if the response is not encrypted.
     * 
     * @ensure getSize() == 1 : "The size of this packet is 1.";
     * 
     * @throws FailedEncodingException This exception is never thrown by this constructor but is listed nonetheless.
     */
    public Packet(@Nonnull SelfcontainedWrapper content, @Nullable SymmetricKey symmetricKey) throws FailedEncodingException {
        this(Arrays.asList(content), null, symmetricKey, null, null, null, null, null, null, false, null);
    }
    
    /**
     * Packs the given contents with the given arguments as a response signed by the given host.
     * 
     * @param contents a list of selfcontained wrappers whose blocks are to be packed as a response.
     * @param symmetricKey the symmetric key used for encryption or null if the response is not encrypted.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit since the last audit or null if no audit is appended.
     * @param signer the identifier of the signing host.
     * 
     * @require !contents.isEmpty() : "The list of contents is not empty.";
     * @require Server.hasHost(signer.getHostIdentifier()) : "The host of the signer is running on this server.";
     * 
     * @ensure getSize() == contents.size() : "The size of this packet equals the size of the contents.";
     * 
     * @throws FailedEncodingException This exception is never thrown by this constructor but is listed nonetheless.
     */
    public Packet(@Nonnull List<SelfcontainedWrapper> contents, @Nullable SymmetricKey symmetricKey, @Nonnull Identifier subject, @Nullable Audit audit, @Nonnull HostIdentifier signer) throws FailedEncodingException {
        this(contents, null, symmetricKey, subject, audit, signer, null, null, null, false, null);
    }
    
    /**
     * Packs the given contents with the given arguments for encrypting and signing.
     * 
     * @param contents a list of selfcontained wrappers whose blocks are to be packed (either as a request or a response).
     * @param recipient the identifier of the host for which the content is encrypted or null if the recipient is not known.
     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
     * @param subject the identifier of the identity about which a statement is made (either in a request or a response).
     * @param audit the audit since the last audit or null in case of external requests or responses.
     * @param signer the identifier of the signing host or null if the element is not signed by a host.
     * @param commitment the commitment containing the client secret or null if the element is not signed by a client.
     * @param credentials the credentials with which the content is signed or null if the content is not signed with credentials.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not shortened.
     * 
     * @require !contents.isEmpty() : "The list of contents is not empty.";
     * @require subject != null || recipient == null && signer == null && commitment == null && credentials == null : "The subject may only be null if the contents of a response are not signed (because the host could not decode the subject).";
     * 
     * @ensure getSize() == contents.size() : "The size of this packet equals the size of the contents.";
     */
    protected Packet(@Nonnull List<SelfcontainedWrapper> contents, @Nullable HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nullable Identifier subject, @Nullable Audit audit, @Nullable HostIdentifier signer, @Nullable Commitment commitment, @Nullable List<Credential> credentials, @Nullable List<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws FailedEncodingException {
        assert !contents.isEmpty() : "The list of contents is not empty.";
        assert subject != null || recipient == null && signer == null && commitment == null && credentials == null : "The subject may only be null if the contents of a response are not signed (because the host could not decode the subject).";
        
        this.contents = contents;
        size = contents.size();
        compressions = new ArrayList<CompressionWrapper>(size);
        signatures = new ArrayList<SignatureWrapper>(size);
        
        for (int i = 0; i < size; i++) {
            @Nonnull SelfcontainedWrapper content = contents.get(i);
            @Nonnull CompressionWrapper compression = new CompressionWrapper(content, CompressionWrapper.ZLIB);
            @Nullable Audit _audit = (i == size - 1 ? audit : null);
            @Nonnull SignatureWrapper signature;
            if (signer != null && (_audit != null || !content.getIdentifier().equals(NonHostIdentifier.NOREPLY) && !content.getIdentifier().equals(NonHostIdentifier.PACKET_ERROR))) {
                signature = new HostSignatureWrapper(compression, subject, _audit, signer); // TODO: Only require the signer to be of type 'Identifier'?
            } else if (commitment != null) {
                signature = new ClientSignatureWrapper(compression, subject, _audit, commitment);
            } else if (credentials != null) {
                signature = new CredentialsSignatureWrapper(compression, subject, _audit, credentials, certificates, lodged, value);
            } else {
                signature = new SignatureWrapper(compression, subject);
            }
            compressions.set(i, compression);
            signatures.set(i, signature);
        }
        
        encryption = new EncryptionWrapper(new ListWrapper(signatures, true), recipient, symmetricKey);
        wrapper = new SelfcontainedWrapper(NonHostIdentifier.PACKET_ENCRYPTION, encryption);
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
    @SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
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
        try { encryption = new EncryptionWrapper(wrapper.getElement(), symmetricKey); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.ENCRYPTION, exception); }
        
        @Nonnull List<Block> elements;
        try { elements = new ListWrapper(encryption.getElement()).getElements(); } catch (InvalidEncodingException exception) { throw new PacketException(PacketError.SIGNATURE, exception); }
        size = elements.size();
        if (size == 0) throw new PacketException(PacketError.PACKET, new InvalidEncodingException("The encryption of a packet must contain at least one signature."));
        signatures = new ArrayList<SignatureWrapper>(size);
        compressions = new ArrayList<CompressionWrapper>(size);
        contents = new ArrayList<SelfcontainedWrapper>(size);
        
        @Nullable SignatureWrapper reference = null;
        for (int i = 0; i < size; i++) {
            @Nonnull SignatureWrapper signature;
            @Nonnull CompressionWrapper compression;
            @Nonnull SelfcontainedWrapper content;
            
            try { signature = verification ? SignatureWrapper.decode(encryption.getElement()) : SignatureWrapper.decodeUnverified(encryption.getElement()); } catch (InvalidEncodingException | InvalidSignatureException | FailedIdentityExceptionexception) { throw new PacketException(PacketError.SIGNATURE, exception); }
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
            } catch (InvalidEncodingException | FailedIdentityExceptionexception) { throw new PacketException(PacketError.SIGNATURE, exception); }
            
            signatures.set(i, signature);
            compressions.set(i, compression);
            contents.set(i, content);
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
     * Returns the signatures of this packet.
     * 
     * @return the signatures of this packet.
     */
    public final @Nonnull List<SignatureWrapper> getSignatures() {
        return signatures;
    }
    
    /**
     * Returns the signature at the given position in this packet.
     * 
     * @param index the index of the signature to be returned.
     * @return the signature at the given position in this packet.
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    public final @Nonnull SignatureWrapper getSignature(int index) {
        assert index >= 0 && index < getSize() : "The index is valid.";
        
        return signatures.get(index);
    }
    
    /**
     * Returns the first and only signature in this packet.
     * 
     * @return the first and only signature in this packet.
     * @require getSize() == 1 : "There is only one signature.";
     */
    public final @Nonnull SignatureWrapper getSignature() {
        assert getSize() == 1 : "There is only one signature.";
        
        return signatures.get(0);
    }
    
    
    /**
     * Returns the compressions of this packet.
     * 
     * @return the compressions of this packet.
     */
    public final @Nonnull List<CompressionWrapper> getCompressions() {
        return compressions;
    }
    
    /**
     * Returns the compression at the given position in this packet.
     * 
     * @param index the index of the compression to be returned.
     * @return the compression at the given position in this packet.
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    public final @Nonnull CompressionWrapper getCompression(int index) {
        assert index >= 0 && index < getSize() : "The index is valid.";
        
        return compressions.get(index);
    }
    
    /**
     * Returns the first and only compression in this packet.
     * 
     * @return the first and only compression in this packet.
     * @require getSize() == 1 : "There is only one compression.";
     */
    public final @Nonnull CompressionWrapper getCompression() {
        assert getSize() == 1 : "There is only one compression.";
        
        return compressions.get(0);
    }
    
    
    /**
     * Returns the contents of this packet.
     * 
     * @return the contents of this packet.
     */
    public final @Nonnull List<SelfcontainedWrapper> getContents() {
        return contents;
    }
    
    /**
     * Returns the content at the given position in this packet or null if there was no reply.
     * 
     * @param index the index of the content to be returned.
     * @return the content at the given position in this packet or null if there was no reply.
     * @throws PacketException if the responding host encountered a packet error.
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    // TODO: Rather getReply?
    public final @Nullable SelfcontainedWrapper getContent(int index) throws PacketException, FailedIdentityException, InvalidEncodingException {
        assert index >= 0 && index < getSize() : "The index is valid.";
        
        @Nonnull SelfcontainedWrapper content = contents.get(index);
        @Nonnull SemanticType type = content.getIdentifier().getIdentity().toSemanticType();
        if (type.equals(SemanticType.PACKET_ERROR)) throw new PacketException(PacketError.get(new Int8Wrapper(content.getElement()).getValue()));
        if (type.equals(SemanticType.NOREPLY)) return null;
        return content;
    }
    
    /**
     * Returns the first and only content in this packet without checking anything.
     * (The checks on the first content are performed when the response is retrieved.)
     * 
     * @return the first and only content in this packet without checking anything.
     * @require getSize() == 1 : "There is only one content.";
     */
    public final @Nonnull SelfcontainedWrapper getContent() {
        assert getSize() == 1 : "There is only one content.";
        
        return contents.get(0);
    }
    
    
    /**
     * Returns the number of signatures, compressions and contents.
     * 
     * @return the number of signatures, compressions and contents.
     * @ensure getSize() > 0 : "The size is always > 0.";
     */
    public final int getSize() {
        return size;
    }
    
    // TODO: getAudit()?
    
    /**
     * Writes the packet to the given output stream.
     * 
     * @param outputStream the output stream to write to.
     */
    public final void write(@Nonnull OutputStream outputStream) throws IOException {
        wrapper.write(outputStream);
    }
    
}
