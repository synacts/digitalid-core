package net.digitalid.core.packet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.functional.failable.FailableUnaryFunction;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.conversion.exceptions.NetworkException;
import net.digitalid.core.conversion.exceptions.NetworkExceptionBuilder;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.encryption.RequestEncryption;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;

/**
 * This class compresses, signs and encrypts requests.
 * The subject of a request is given as identifier and not as an identity in order to be able to retrieve identity information as well as creating new accounts.
 * 
 * @invariant getSize() == methods.size() : "The number of elements equals the number of methods.";
 * 
 * @see HostRequest
 * @see ClientRequest
 * @see CredentialsRequest
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
// @GenerateConverter // TODO: Support generics.
public abstract class Request extends Packet {
    
//    /**
//     * Stores the methods of this request.
//     * 
//     * @invariant Method.areSimilar(methods) : "All methods are similar and not null.";
//     */
//    private @Nonnull @Frozen @NonNullableElements @NonEmpty FreezableList<Method> methods;
//    
//    /**
//     * Stores the recipient of this request.
//     */
//    private final @Nonnull HostIdentifier recipient;
//    
//    /**
//     * Stores the subject of this request.
//     */
//    private final @Nonnull InternalIdentifier subject;
//    
//    /**
//     * Stores how many times this request was resent.
//     */
//    private final int iteration;
//    
//    /**
//     * Creates a new request with a query for the public key chain of the given host that is neither encrypted nor signed.
//     * 
//     * @param identifier the identifier of the host whose public key chain is to be retrieved.
//     * 
//     * @ensure getSize() == 1 : "The size of this request is 1.";
//     */
//    @NonCommitting
//    public Request(@Nonnull HostIdentifier identifier) throws ExternalException {
//        this(new FreezableArrayList<Method>(new AttributesQuery(null, identifier, new FreezableAttributeTypeSet(PublicKeyChain.TYPE).freeze(), true)).freeze(), identifier, null, identifier, null, null, 0);
//    }
//    
//    /**
//     * Packs the given methods with the given arguments with encrypting but without signing.
//     * 
//     * @param methods the methods of this request.
//     * @param recipient the recipient of this request.
//     * @param subject the subject of this request.
//     * 
//     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
//     */
//    @NonCommitting
//    public Request(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject) throws ExternalException {
//        this(methods, recipient, SymmetricKey.getRandom(), subject, null, null, 0);
//    }
//    
//    /**
//     * Packs the given methods with the given arguments for encrypting and signing.
//     * 
//     * @param methods a list of methods whose blocks are to be packed as a request.
//     * @param recipient the identifier of the host for which the content is to be encrypted.
//     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
//     * @param subject the identifier of the identity about which a statement is made.
//     * @param audit the audit with the time of the last retrieval or null in case of external requests.
//     * @param field an object that contains the signing parameter and is passed back with {@link #setField(java.lang.Object)}.
//     * @param iteration how many times this request was resent.
//     */
//    @NonCommitting
//    Request(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nullable Object field, int iteration) throws ExternalException {
//        super(methods, methods.size(), field, recipient, symmetricKey, subject, audit);
//        
//        Require.that(methods.isFrozen()).orThrow("The list of methods is frozen.");
//        Require.that(!methods.isEmpty()).orThrow("The list of methods is not empty.");
//        Require.that(!methods.containsNull()).orThrow("The list of methods does not contain null.");
//        Require.that(Method.areSimilar(methods)).orThrow("The methods are similar to each other.");
//        
//        if (iteration == 5) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The resending of a request was triggered five times."); }
//        
//        this.recipient = recipient;
//        this.subject = subject;
//        this.iteration = iteration + 1;
//    }
//    
//    
//    /**
//     * Reads and unpacks the request from the given input stream on hosts.
//     * 
//     * @param inputStream the input stream to read the request from.
//     */
//    @NonCommitting
//    public Request(@Nonnull InputStream inputStream) throws ExternalException {
//        super(inputStream, null, true);
//        
//        final @Nullable HostIdentifier recipient = getEncryption().getRecipient();
//        Require.that(recipient != null).orThrow("The recipient of the request is not null.");
//        this.recipient = recipient;
//        this.subject = getMethod(0).getSubject();
//        this.iteration = 0;
//    }
//    
//    
//    @Override
//    @RawRecipient
//    @SuppressWarnings("unchecked")
//    final void setList(@Nonnull Object object) {
//        this.methods = (FreezableList<Method>) object;
//    }
//    
//    @Override
//    @RawRecipient
//    void setField(@Nullable Object field) {}
//    
//    @Pure
//    @Override
//    @RawRecipient
//    final @Nonnull Block getBlock(int index) {
//        return methods.get(index).toBlock();
//    }
//    
//    @Pure
//    @Override
//    @RawRecipient
//    @NonCommitting
//    @Nonnull SignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) throws ExternalException {
//        return SignatureWrapper.encodeWithoutSigning(Packet.SIGNATURE, compression, subject);
//    }
//    
//    
//    @Override
//    @RawRecipient
//    final void initialize(int size) {
//        this.methods = FreezableArrayList.getWithCapacity(size);
//        for (int i = 0; i < size; i++) { methods.add(null); }
//    }
//    
//    @Override
//    @RawRecipient
//    final void freeze() {
//        methods.freeze();
//    }
//    
//    
//    /**
//     * Returns the method at the given position in this request.
//     * 
//     * @param index the index of the method which is to be returned.
//     * 
//     * @return the method at the given position in this request.
//     */
//    @Pure
//    public final @Nonnull Method getMethod(@Index int index) {
//        return methods.getNonNullable(index);
//    }
//    
//    /**
//     * Sets the method at the given position during the packet constructor.
//     * 
//     * @param index the index of the method which is to be set.
//     * @param method the method which is to set at the index.
//     */
//    @RawRecipient
//    final void setMethod(@Index int index, @Nonnull Method method) {
//        methods.set(index, method);
//    }
//    
//    
//    @Pure
//    @Override
//    public final @Nullable RequestAudit getAudit() {
//        final @Nullable Audit audit = super.getAudit();
//        return audit != null ? (RequestAudit) audit : null;
//    }
//    
//    /**
//     * Returns the recipient of this request.
//     * 
//     * @return the recipient of this request.
//     */
//    @Pure
//    public final @Nonnull HostIdentifier getRecipient() {
//        return recipient;
//    }
//    
//    /**
//     * Returns the subject of this request.
//     * 
//     * @return the subject of this request.
//     */
//    @Pure
//    public final @Nonnull InternalIdentifier getSubject() {
//        return subject;
//    }
//    
//    
//    /**
//     * Returns whether this request is signed.
//     * 
//     * @return whether this request is signed.
//     */
//    @Pure
//    public boolean isSigned() {
//        return false;
//    }
//    
//    /**
//     * Resends this request and returns the response, optionally verifying the signature.
//     * 
//     * @param methods the methods of this request.
//     * @param recipient the recipient of this request.
//     * @param subject the subject of this request.
//     * @param iteration how many times this request was resent.
//     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
//     * 
//     * @return the response to the resent request.
//     */
//    @NonCommitting
//    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws ExternalException {
//        return new Request(methods, recipient, SymmetricKey.getRandom(), subject, null, null, iteration).send(verified);
//    }
//    
//    /**
//     * Sends this request and returns the response with verifying the signature.
//     * 
//     * @return the response to this request.
//     * 
//     * @throws RequestException if the recipient responded with a packet error.
//     * 
//     * @ensure response.getSize() == getSize() : "The response has the same number of elements (otherwise a {@link PacketException} is thrown).";
//     */
//    @NonCommitting
//    public final @Nonnull Response send() throws ExternalException {
//        return send(true);
//    }
//    
//    /**
//     * Sends this request and returns the response, optionally verifying the signature.
//     * 
//     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
//     * 
//     * @return the response to this request.
//     * 
//     * @throws RequestException if the recipient responded with a packet error.
//     * 
//     * @ensure response.getSize() == getSize() : "The response has the same number of elements (otherwise a {@link PacketException} is thrown).";
//     */
//    @NonCommitting
//    public final @Nonnull Response send(boolean verified) throws ExternalException {
//        // TODO: Use the specific NetworkExceptions.
//        try (@Nonnull Socket socket = new Socket("id." + getRecipient().getString(), Server.PORT)) {
//            socket.setSoTimeout(1000000); // TODO: Remove two zeroes!
//            this.write(socket.getOutputStream());
//            return new Response(this, socket.getInputStream(), verified);
//        } catch (@Nonnull RequestException exception) {
//            if (exception.getCode() == RequestErrorCode.KEYROTATION && this instanceof ClientRequest) {
//                return ((ClientRequest) this).recommit(methods, iteration, verified);
//            } else if (exception.getCode() == RequestErrorCode.RELOCATION && subject instanceof InternalNonHostIdentifier) {
//                if (getMethod(0).getType().equals(IdentityQuery.TYPE)) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The response to an identity query may not be a relocation exception."); }
//                @Nullable InternalNonHostIdentifier address = Successor.get((InternalNonHostIdentifier) subject);
//                if (address == null) {
//                    address = Successor.getReloaded((InternalNonHostIdentifier) subject);
//                    if (!address.getIdentity().equals(subject.getIdentity())) { throw InvalidDeclarationException.get("The claimed successor " + address + " of " + subject + " does not link back.", subject); }
//                } else if (subject.isMapped()) {
//                    Mapper.unmap(subject.getMappedIdentity());
//                }
//                final @Nonnull HostIdentifier recipient = getMethod(0).getService().equals(CoreService.SERVICE) ? address.getHostIdentifier() : getRecipient();
//                return resend(methods, recipient, address, iteration, verified);
//            } else if (exception.getCode() == RequestErrorCode.SERVICE && !getMethod(0).isOnHost()) {
//                final @Nonnull HostIdentifier recipient = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getReloadedAttributeContent(subject.getIdentity(), (Role) getMethod(0).getEntity(), getMethod(0).getService().getType(), false)).castTo(HostIdentifier.class);
//                if (this.recipient.equals(recipient)) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The recipient after a service error is still the same.", exception); }
//                return resend(methods, recipient, subject, iteration, verified);
//            } else {
//                throw exception;
//            }
//        }
//    }
    
    /**
     * Stores the function which resolves a host identifier into an internet address.
     * This function is configurable in order that tests can provide a loopback address.
     */
    public static final @Nonnull Configuration<FailableUnaryFunction<@Nonnull HostIdentifier, @Nonnull InetAddress, UnknownHostException>> ADDRESS = Configuration.with(identifier -> InetAddress.getByName("id." + identifier.getString()));
    
    /**
     * Stores the port number on which a Digital ID server listens by default.
     */
    @TODO(task = "Figure out how to run the server on port 494 with Maven.", date = "2017-02-14", author = Author.KASPAR_ETTER)
    public static final @Nonnull Configuration<Integer> PORT = Configuration.with(1494);
    
    /**
     * Stores the timeout in milliseconds for which reading from the socket's input stream blocks before raising a {@link SocketTimeoutException}.
     * This value is configurable in order that tests can set a higher timeout for debugging.
     */
    public static final @Nonnull Configuration<Integer> TIMEOUT = Configuration.with(10000);
    
    /**
     * Maps the converter of this class.
     */
    @PureWithSideEffects
    @Initialize(target = Request.class, dependencies = IdentifierResolver.class)
    public static void mapConverter() {
        SemanticType.map(RequestConverter.INSTANCE);
        SemanticType.map(ResponseConverter.INSTANCE);
    }
    
    /**
     * Sends this request and returns the response.
     */
    @NonCommitting
    @PureWithSideEffects
    public @Nonnull Response send() throws NetworkException, RecoveryException {
        try (@Nonnull Socket socket = new Socket(ADDRESS.get().evaluate(getEncryption().getRecipient()), PORT.get())) {
            socket.setSoTimeout(TIMEOUT.get());
            pack().storeTo(socket);
            final @Nonnull Pack pack = Pack.loadFrom(socket);
            final @Nonnull Encryption<Signature<Compression<Pack>>> encryption = getEncryption();
            return pack.unpack(ResponseConverter.INSTANCE, encryption instanceof RequestEncryption ? ((RequestEncryption) encryption).getSymmetricKey() : null);
//        } catch (@Nonnull RequestException exception) {
//            if (exception.getCode() == RequestErrorCode.KEYROTATION && this instanceof ClientRequest) {
//                return ((ClientRequest) this).recommit(methods, iteration, verified);
//            } else if (exception.getCode() == RequestErrorCode.RELOCATION && subject instanceof InternalNonHostIdentifier) {
//                if (getMethod(0).getType().equals(IdentityQuery.TYPE)) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The response to an identity query may not be a relocation exception."); }
//                @Nullable InternalNonHostIdentifier address = Successor.get((InternalNonHostIdentifier) subject);
//                if (address == null) {
//                    address = Successor.getReloaded((InternalNonHostIdentifier) subject);
//                    if (!address.getIdentity().equals(subject.getIdentity())) { throw InvalidDeclarationException.get("The claimed successor " + address + " of " + subject + " does not link back.", subject); }
//                } else if (subject.isMapped()) {
//                    Mapper.unmap(subject.getMappedIdentity());
//                }
//                final @Nonnull HostIdentifier recipient = getMethod(0).getService().equals(CoreService.SERVICE) ? address.getHostIdentifier() : getRecipient();
//                return resend(methods, recipient, address, iteration, verified);
//            } else if (exception.getCode() == RequestErrorCode.SERVICE && !getMethod(0).isOnHost()) {
//                final @Nonnull HostIdentifier recipient = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getReloadedAttributeContent(subject.getIdentity(), (Role) getMethod(0).getEntity(), getMethod(0).getService().getType(), false)).castTo(HostIdentifier.class);
//                if (this.recipient.equals(recipient)) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The recipient after a service error is still the same.", exception); }
//                return resend(methods, recipient, subject, iteration, verified);
//            } else {
//                throw exception;
//            }
        } catch (@Nonnull IOException exception) {
            throw NetworkExceptionBuilder.withCause(exception).build();
        }
    }
    
}
