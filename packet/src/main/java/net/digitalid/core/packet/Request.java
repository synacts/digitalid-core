package net.digitalid.core.packet;

import java.io.InputStream;
import java.net.Socket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.reference.RawRecipient;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.index.Index;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.audit.Audit;
import net.digitalid.core.audit.RequestAudit;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.CompressionWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.IdentifierImplementation;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.packet.exceptions.InvalidDeclarationException;
import net.digitalid.core.resolution.IdentityQuery;
import net.digitalid.core.resolution.Mapper;
import net.digitalid.core.resolution.Successor;
import net.digitalid.core.server.Server;
import net.digitalid.core.service.CoreService;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.cryptography.SymmetricKey;

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
public class Request extends Packet {
    
    /**
     * Stores the methods of this request.
     * 
     * @invariant Method.areSimilar(methods) : "All methods are similar and not null.";
     */
    private @Nonnull @Frozen @NonNullableElements @NonEmpty FreezableList<Method> methods;
    
    /**
     * Stores the recipient of this request.
     */
    private final @Nonnull HostIdentifier recipient;
    
    /**
     * Stores the subject of this request.
     */
    private final @Nonnull InternalIdentifier subject;
    
    /**
     * Stores how many times this request was resent.
     */
    private final int iteration;
    
    /**
     * Creates a new request with a query for the public key chain of the given host that is neither encrypted nor signed.
     * 
     * @param identifier the identifier of the host whose public key chain is to be retrieved.
     * 
     * @ensure getSize() == 1 : "The size of this request is 1.";
     */
    @NonCommitting
    public Request(@Nonnull HostIdentifier identifier) throws ExternalException {
        this(new FreezableArrayList<Method>(new AttributesQuery(null, identifier, new FreezableAttributeTypeSet(PublicKeyChain.TYPE).freeze(), true)).freeze(), identifier, null, identifier, null, null, 0);
    }
    
    /**
     * Packs the given methods with the given arguments with encrypting but without signing.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * 
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     */
    @NonCommitting
    public Request(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject) throws ExternalException {
        this(methods, recipient, SymmetricKey.getRandom(), subject, null, null, 0);
    }
    
    /**
     * Packs the given methods with the given arguments for encrypting and signing.
     * 
     * @param methods a list of methods whose blocks are to be packed as a request.
     * @param recipient the identifier of the host for which the content is to be encrypted.
     * @param symmetricKey the symmetric key used for encryption or null if the content is not encrypted.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit with the time of the last retrieval or null in case of external requests.
     * @param field an object that contains the signing parameter and is passed back with {@link #setField(java.lang.Object)}.
     * @param iteration how many times this request was resent.
     */
    @NonCommitting
    Request(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nullable Object field, int iteration) throws ExternalException {
        super(methods, methods.size(), field, recipient, symmetricKey, subject, audit);
        
        Require.that(methods.isFrozen()).orThrow("The list of methods is frozen.");
        Require.that(!methods.isEmpty()).orThrow("The list of methods is not empty.");
        Require.that(!methods.containsNull()).orThrow("The list of methods does not contain null.");
        Require.that(Method.areSimilar(methods)).orThrow("The methods are similar to each other.");
        
        if (iteration == 5) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The resending of a request was triggered five times."); }
        
        this.recipient = recipient;
        this.subject = subject;
        this.iteration = iteration + 1;
    }
    
    
    /**
     * Reads and unpacks the request from the given input stream on hosts.
     * 
     * @param inputStream the input stream to read the request from.
     */
    @NonCommitting
    public Request(@Nonnull InputStream inputStream) throws ExternalException {
        super(inputStream, null, true);
        
        final @Nullable HostIdentifier recipient = getEncryption().getRecipient();
        Require.that(recipient != null).orThrow("The recipient of the request is not null.");
        this.recipient = recipient;
        this.subject = getMethod(0).getSubject();
        this.iteration = 0;
    }
    
    
    @Override
    @RawRecipient
    @SuppressWarnings("unchecked")
    final void setList(@Nonnull Object object) {
        this.methods = (FreezableList<Method>) object;
    }
    
    @Override
    @RawRecipient
    void setField(@Nullable Object field) {}
    
    @Pure
    @Override
    @RawRecipient
    final @Nonnull Block getBlock(int index) {
        return methods.get(index).toBlock();
    }
    
    @Pure
    @Override
    @RawRecipient
    @NonCommitting
    @Nonnull SignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) throws ExternalException {
        return SignatureWrapper.encodeWithoutSigning(Packet.SIGNATURE, compression, subject);
    }
    
    
    @Override
    @RawRecipient
    final void initialize(int size) {
        this.methods = FreezableArrayList.getWithCapacity(size);
        for (int i = 0; i < size; i++) { methods.add(null); }
    }
    
    @Override
    @RawRecipient
    final void freeze() {
        methods.freeze();
    }
    
    
    /**
     * Returns the method at the given position in this request.
     * 
     * @param index the index of the method which is to be returned.
     * 
     * @return the method at the given position in this request.
     */
    @Pure
    public final @Nonnull Method getMethod(@Index int index) {
        return methods.getNonNullable(index);
    }
    
    /**
     * Sets the method at the given position during the packet constructor.
     * 
     * @param index the index of the method which is to be set.
     * @param method the method which is to set at the index.
     */
    @RawRecipient
    final void setMethod(@Index int index, @Nonnull Method method) {
        methods.set(index, method);
    }
    
    
    @Pure
    @Override
    public final @Nullable RequestAudit getAudit() {
        final @Nullable Audit audit = super.getAudit();
        return audit != null ? (RequestAudit) audit : null;
    }
    
    /**
     * Returns the recipient of this request.
     * 
     * @return the recipient of this request.
     */
    @Pure
    public final @Nonnull HostIdentifier getRecipient() {
        return recipient;
    }
    
    /**
     * Returns the subject of this request.
     * 
     * @return the subject of this request.
     */
    @Pure
    public final @Nonnull InternalIdentifier getSubject() {
        return subject;
    }
    
    
    /**
     * Returns whether this request is signed.
     * 
     * @return whether this request is signed.
     */
    @Pure
    public boolean isSigned() {
        return false;
    }
    
    /**
     * Resends this request and returns the response, optionally verifying the signature.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * @param iteration how many times this request was resent.
     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
     * 
     * @return the response to the resent request.
     */
    @NonCommitting
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws ExternalException {
        return new Request(methods, recipient, SymmetricKey.getRandom(), subject, null, null, iteration).send(verified);
    }
    
    /**
     * Sends this request and returns the response with verifying the signature.
     * 
     * @return the response to this request.
     * 
     * @throws RequestException if the recipient responded with a packet error.
     * 
     * @ensure response.getSize() == getSize() : "The response has the same number of elements (otherwise a {@link PacketException} is thrown).";
     */
    @NonCommitting
    public final @Nonnull Response send() throws ExternalException {
        return send(true);
    }
    
    /**
     * Sends this request and returns the response, optionally verifying the signature.
     * 
     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
     * 
     * @return the response to this request.
     * 
     * @throws RequestException if the recipient responded with a packet error.
     * 
     * @ensure response.getSize() == getSize() : "The response has the same number of elements (otherwise a {@link PacketException} is thrown).";
     */
    @NonCommitting
    public final @Nonnull Response send(boolean verified) throws ExternalException {
        // TODO: Use the specific NetworkExceptions.
        try (@Nonnull Socket socket = new Socket("id." + getRecipient().getString(), Server.PORT)) {
            socket.setSoTimeout(1000000); // TODO: Remove two zeroes!
            this.write(socket.getOutputStream());
            return new Response(this, socket.getInputStream(), verified);
        } catch (@Nonnull RequestException exception) {
            if (exception.getCode() == RequestErrorCode.KEYROTATION && this instanceof ClientRequest) {
                return ((ClientRequest) this).recommit(methods, iteration, verified);
            } else if (exception.getCode() == RequestErrorCode.RELOCATION && subject instanceof InternalNonHostIdentifier) {
                if (getMethod(0).getType().equals(IdentityQuery.TYPE)) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The response to an identity query may not be a relocation exception."); }
                @Nullable InternalNonHostIdentifier address = Successor.get((InternalNonHostIdentifier) subject);
                if (address == null) {
                    address = Successor.getReloaded((InternalNonHostIdentifier) subject);
                    if (!address.getIdentity().equals(subject.getIdentity())) { throw InvalidDeclarationException.get("The claimed successor " + address + " of " + subject + " does not link back.", subject); }
                } else if (subject.isMapped()) {
                    Mapper.unmap(subject.getMappedIdentity());
                }
                final @Nonnull HostIdentifier recipient = getMethod(0).getService().equals(CoreService.SERVICE) ? address.getHostIdentifier() : getRecipient();
                return resend(methods, recipient, address, iteration, verified);
            } else if (exception.getCode() == RequestErrorCode.SERVICE && !getMethod(0).isOnHost()) {
                final @Nonnull HostIdentifier recipient = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getReloadedAttributeContent(subject.getIdentity(), (Role) getMethod(0).getEntity(), getMethod(0).getService().getType(), false)).castTo(HostIdentifier.class);
                if (this.recipient.equals(recipient)) { throw RequestException.get(/* RequestErrorCode.EXTERNAL */, "The recipient after a service error is still the same.", exception); }
                return resend(methods, recipient, subject, iteration, verified);
            } else {
                throw exception;
            }
        }
    }
    
    
    /**
     * Stores a cached symmetric key for every recipient.
     */
    private static final @Nonnull ConcurrentMap<HostIdentifier, ReadOnlyPair<Time, SymmetricKey>> symmetricKeys = new ConcurrentHashMap<>();
    
    /**
     * Stores whether the symmetric keys are cached.
     */
    private static boolean cachingKeys = true;
    
    /**
     * Returns whether the symmetric keys are cached.
     * 
     * @return whether the symmetric keys are cached.
     */
    @Pure
    public static boolean isCachingKeys() {
        return cachingKeys;
    }
    
    /**
     * Sets whether the symmetric keys are cached.
     * 
     * @param cachingKeys whether the symmetric keys are cached.
     */
    public static void setCachingKeys(boolean cachingKeys) {
        Request.cachingKeys = cachingKeys;
    }
    
    /**
     * Returns a new or cached symmetric key for the given recipient.
     * 
     * @param recipient the recipient for which a symmetric key is to be returned.
     * @param rotation determines how often the cached symmetric keys are rotated.
     * 
     * @return a new or cached symmetric key for the given recipient.
     */
    @Pure
    protected static @Nonnull SymmetricKey getSymmetricKey(@Nonnull HostIdentifier recipient, @Nonnull Time rotation) {
        if (cachingKeys) {
            final @Nonnull Time time = Time.getCurrent();
            @Nullable ReadOnlyPair<Time, SymmetricKey> value = symmetricKeys.get(recipient);
            if (value == null || value.getElement0().isLessThan(time.subtract(rotation))) {
                value = new FreezablePair<>(time, new SymmetricKey()).freeze();
                symmetricKeys.put(recipient, value);
            }
            return value.getElement1();
        } else {
            return new SymmetricKey();
        }
    }
    
}
