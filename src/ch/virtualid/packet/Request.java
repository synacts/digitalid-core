package ch.virtualid.packet;

import ch.virtualid.client.Commitment;
import ch.virtualid.credential.Credential;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import static ch.virtualid.packet.PacketError.KEYROTATION;
import ch.virtualid.server.Server;
import ch.xdf.Block;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.FailedEncodingException;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.exceptions.InvalidSignatureException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class compresses, signs and encrypts requests.
 * The subject of a request is given as identifier and not as an identity in order to be able to retrieve identity information as well as creating new accounts.
 * 
 * @invariant for (SignatureWrapper signature : getSignatures()) signature.getSubject() != null : "The subjects of the signatures are never null.";
 * @invariant getEncryption().getRecipient() != null : "The recipient of the request is never null.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public class Request extends Packet {
    
    /**
     * Asserts that the class invariant still holds.
     */
    private boolean invariant() {
        for (@Nonnull SignatureWrapper signature : getSignatures()) {
            assert signature.getSubject() != null : "The subjects of the signatures are never null.";
        }
        assert getEncryption().getRecipient() != null : "The recipient of the request is never null.";
        return true;
    }
    
    
    /**
     * Packs the given content with the given arguments without encrypting or signing.
     * This constructor is only to be used to retrieve the public key of hosts.
     * 
     * @param content the content of this request.
     * @param subject the subject of this request.
     * @ensure getSize() == 1 : "The size of this request is 1.";
     */
    public Request(@Nonnull SelfcontainedWrapper content, @Nonnull HostIdentifier subject) throws FailedEncodingException {
        this(Arrays.asList(content), subject, null, subject, null, null, null, null, null, false, null);
    }
    
    /**
     * Packs the given contents with the given arguments with encrypting but without signing.
     * 
     * @param contents the contents of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @require !contents.isEmpty() : "The list of contents is not empty.";
     * @require recipient.exists() : "The recipient has to exist.";
     * @ensure getSize() == contents.size() : "The size of this request equals the size of the contents.";
     */
    public Request(@Nonnull List<SelfcontainedWrapper> contents, @Nonnull HostIdentifier recipient, @Nonnull Identifier subject) throws FailedEncodingException {
        this(contents, recipient, new SymmetricKey(), subject, null, null, null, null, null, false, null);
    }
    
    /**
     * Packs the given content with the given arguments for encrypting and signing.
     * 
     * @param contents a list of selfcontained wrappers whose blocks are to be packed as a request.
     * @param recipient the identifier of the host for which the content is to be encrypted.
     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit with the time of the last retrieval or null in case of external requests.
     * @param signer the identifier of the signing host or null if the element is not signed by a host.
     * @param commitment the commitment containing the client secret or null if the element is not signed by a client.
     * @param credentials the credentials with which the content is signed or null if the content is not signed with credentials.
     * @param certificates the certificates that are appended to an identity-based authentication or null.
     * @param lodged whether the hidden content of the credentials is verifiably encrypted to achieve liability.
     * @param value the value b' or null if the credentials are not shortened.
     */
    protected Request(@Nonnull List<SelfcontainedWrapper> contents, @Nonnull HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nonnull Identifier subject, @Nullable Audit audit, @Nullable HostIdentifier signer, @Nullable Commitment commitment, @Nullable List<Credential> credentials, @Nullable List<HostSignatureWrapper> certificates, boolean lodged, @Nullable BigInteger value) throws FailedEncodingException {
        super(contents, recipient, symmetricKey, subject, audit, signer, commitment, credentials, certificates, lodged, value);
        
        assert invariant();
    }
    
    
    /**
     * Sends this request and returns the response with verifying the signature.
     * 
     * @return the response to this request.
     */
    public @Nonnull Response send() throws FailedRequestException, PacketException {
        return send(true);
    }
    
    /**
     * Sends this request and returns the response, optionally verifying the signature.
     * 
     * @param verification determines whether the signature of the response is verified.
     * 
     * @return the response to this request.
     * 
     * @throws FailedRequestException if the request could not be sent or the response could not be decoded.
     * @throws PacketException if the sending and the receiving of the packet went smooth but the recipient responded with a packet error.
     * 
     * @ensure response.getSize() == getSize() : "The response has the same number of signed contents (otherwise a {@link FailedRequestException} is thrown).";
     */
    public @Nonnull Response send(boolean verification) throws FailedRequestException, PacketException {
        @Nullable HostIdentifier recipient = getEncryption().getRecipient();
        assert recipient != null : "The recipient of the request is never null (see class invariant).";
        
        // Send the request and retrieve the response.
        @Nonnull Response response;
        try (Socket socket = new Socket(recipient.getString(), Server.PORT)) {
            write(socket.getOutputStream());
            response = new Response(SelfcontainedWrapper.read(socket.getInputStream()), getEncryption().getSymmetricKey(), verification);
        } catch (@Nonnull PacketException | InvalidEncodingException exception) {
            throw new FailedRequestException("Could not unpack the invalid response from the host " + recipient + ".", exception);
        } catch (@Nonnull UnknownHostException exception) {
            throw new FailedRequestException("Could not find the host " + recipient + ".", exception);
        } catch (@Nonnull IOException exception) {
            throw new FailedRequestException("Could not connect to the host " + recipient + ".", exception);
        }
        
        try {
            // Verify that the response was signed by the right host.
            @Nonnull List<SignatureWrapper> responseSignatures = response.getSignatures();
            for (@Nonnull SignatureWrapper responseSignature : responseSignatures) {
                if (responseSignature.isSigned()) {
                    assert responseSignature instanceof HostSignatureWrapper : "Responses can only be signed by hosts (see the postcondition of the response constructor).";
                    @Nonnull Identifier signer = ((HostSignatureWrapper) responseSignature).getSigner();
                    if (!signer.equals(recipient)) throw new InvalidSignatureException("The response from the host " + recipient + " was signed by " + signer + ".");
                    break; // Only one signature needs to be verified since all signatures are signed alike.
                }
            }
            
            // Verify that the response was about the right subject.
            @Nullable Identifier requestSubject = getSignature(0).getSubject();
            @Nullable Identifier responseSubject = response.getSignature(0).getSubject();
            assert requestSubject != null && responseSubject != null : "Neither the request nor the response subject may be null (see the invariant of the packet class).";
            if (!responseSubject.equals(requestSubject)) throw new InvalidSignatureException("The subject of the request was " + requestSubject + ", the response from " + recipient + " was about " + responseSubject + " though.");
            @Nonnull Identifier subject = requestSubject;
            
            boolean resend = false;
            @Nullable SelfcontainedWrapper content = null;
            
            try {
                // The following statement throws a PacketException if the responding host encountered a (general) packet error.
                content = response.getContent(0);
            } catch (@Nonnull PacketException exception) {
                if (exception.getError() == KEYROTATION) {
                    // TODO: Rotate the key of the client.
                    resend = true;
                } else {
                    throw exception;
                }
            }
            
            if (content != null) {
                // Check whether the subject has been relocated as indicated by an unexpected identity response.
                @Nonnull SemanticType requestType = getContents().get(0).getIdentifier().getIdentity().toSemanticType();
                @Nonnull SemanticType responseType = content.getIdentifier().getIdentity().toSemanticType();
                if (!requestType.equals(SemanticType.IDENTITY_REQUEST) && responseType.equals(SemanticType.IDENTITY_RESPONSE)) {
                    @Nonnull Block element = content.getElement();
                    // TODO: Rewrite with the corresponding handler.
                    @Nonnull Block[] elements = new TupleWrapper(element).getElementsNotNull(3);
                    if (elements[2].isEmpty()) throw new InvalidEncodingException("The successor of the unexpected identity response to a request for " + subject + " is not null.");
                    @Nonnull NonHostIdentifier successor = new NonHostIdentifier(elements[2]);
                    if (!(subject instanceof NonHostIdentifier)) throw new InvalidEncodingException("An unexpected identity response may only be returned for non-host identities and not for " + subject + ".");
                    Mapper.setSuccessor((NonHostIdentifier) subject, successor); // TODO: Also pass the reference to the stored response.
                    if (!successor.getIdentity().equals(subject.getIdentity())) throw new InvalidDeclarationException("The indicated successor " + successor + " is not an identifier of the identity denoted by " + subject + ".");
                    subject = successor;
                    recipient = subject.getHostIdentifier();
                    resend = true;
                }
            }
            
            if (resend) {
                // Resend the request to the subject's successor or with the rotated key.
                @Nonnull SignatureWrapper signature = getSignature(getSize() - 1);
                if (signature instanceof HostSignatureWrapper) {
                    return new HostRequest(getContents(), recipient, subject, (HostIdentifier) ((HostSignatureWrapper) signature).getSigner()).send(verification);
                } else if (signature instanceof ClientSignatureWrapper) {
                    if (!subject.equals(requestSubject)) {
                        // TODO: Recommit to the (potentially) new host with the same client secret.
                    }
                    return new ClientRequest(getContents(), subject, signature.getAudit(), ((ClientSignatureWrapper) signature).getCommitment()).send(verification);
                } else if (signature instanceof CredentialsSignatureWrapper) {
                    @Nonnull CredentialsSignatureWrapper credentialsSignature = (CredentialsSignatureWrapper) signature;
                    return new CredentialsRequest(getContents(), recipient, subject, signature.getAudit(), credentialsSignature.getCredentials(), credentialsSignature.getCertificates(), credentialsSignature.isLodged(), credentialsSignature.getValue()).send(verification);
                } else {
                    return new Request(getContents(), recipient, subject).send(verification);
                }
            }
            
            if (response.getSize() != getSize()) throw new InvalidEncodingException("The response contains fewer (or more) contents than the request.");
            
        } catch (@Nonnull FailedIdentityException exception) {
            throw new FailedRequestException("Could not find the identity " + exception.getIdentifier() + ".", exception);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new FailedRequestException("Could not decode the response from the host " + recipient + ".", exception);
        } catch (@Nonnull InvalidSignatureException exception) {
            throw new FailedRequestException("The signature of the response was missing or invalid.", exception);
        } catch (@Nonnull SQLException exception) {
            throw new FailedRequestException("The successor could not be written to the database.", exception);
        } catch (@Nonnull InvalidDeclarationException exception) {
            throw new FailedRequestException("The declaration of the successor is invalid.", exception);
        } catch (@Nonnull FailedEncodingException exception) {
            throw new FailedRequestException("The request could not be re-encoded for the successor or with the rotated key.", exception);
        }
        
        return response;
    }
    
    
    /**
     * Stores a cached symmetric key for every recipient of host and client requests.
     */
    private static final @Nonnull HashMap<HostIdentifier, Pair<Long, SymmetricKey>> symmetricKeys = new HashMap<HostIdentifier, Pair<Long, SymmetricKey>>();
    
    /**
     * Stores whether the symmetric keys are cached for host and client requests.
     */
    private static boolean cachingKeys = true;
    
    /**
     * Returns whether the symmetric keys are cached for host and client requests.
     * 
     * @return whether the symmetric keys are cached for host and client requests.
     */
    public static boolean isCachingKeys() {
        return cachingKeys;
    }
    
    /**
     * Sets whether the symmetric keys are cached for host and client requests.
     * 
     * @param cachingKeys whether the symmetric keys are cached for host and client requests.
     */
    public static void setCachingKeys(boolean cachingKeys) {
        Request.cachingKeys = cachingKeys;
    }
    
    /**
     * Returns a new or cached symmetric key for the given recipient.
     * 
     * @param recipient the recipient for which a symmetric key is to be returned.
     * @param rotation determines how often the cached symmetric keys are rotated.
     * @return a new or cached symmetric key for the given recipient.
     */
    protected static @Nonnull SymmetricKey getSymmetricKey(@Nonnull HostIdentifier recipient, long rotation) {
        if (cachingKeys) {
            @Nullable Pair<Long, SymmetricKey> value = symmetricKeys.get(recipient);
            long currentTime = System.currentTimeMillis();
            if (value == null || value.getValue0() < currentTime - rotation) {
                value = new Pair<Long, SymmetricKey>(currentTime, new SymmetricKey());
                symmetricKeys.put(recipient, value);
            }
            return value.getValue1();
        } else {
            return new SymmetricKey();
        }
    }
    
}
