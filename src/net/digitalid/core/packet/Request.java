package net.digitalid.core.packet;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.RawRecipient;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.AttributesQuery;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.ConcurrentMap;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.contact.FreezableAttributeTypeSet;
import net.digitalid.core.cryptography.PublicKeyChain;
import net.digitalid.core.cryptography.SymmetricKey;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidDeclarationException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identity.IdentityQuery;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.Successor;
import net.digitalid.core.server.Server;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.synchronizer.Audit;
import net.digitalid.core.synchronizer.RequestAudit;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.CompressionWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class compresses, signs and encrypts requests.
 * The subject of a request is given as identifier and not as an identity in order to be able to retrieve identity information as well as creating new accounts.
 * 
 * @invariant getSize() == methods.size() : "The number of elements equals the number of methods.";
 * 
 * @see HostRequest
 * @see ClientRequest
 * @see CredentialsRequest
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class Request extends Packet {
    
    /**
     * Stores the methods of this request.
     * 
     * @invariant methods.isFrozen() : "The list of methods is frozen.";
     * @invariant methods.isNotEmpty() : "The list of methods is not empty.";
     * @invariant Method.areSimilar(methods) : "All methods are similar and not null.";
     */
    private @Nonnull FreezableList<Method> methods;
    
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
    public Request(@Nonnull HostIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        this(new FreezableArrayList<Method>(new AttributesQuery(null, identifier, new FreezableAttributeTypeSet(PublicKeyChain.TYPE).freeze(), true)).freeze(), identifier, null, identifier, null, null, 0);
    }
    
    /**
     * Packs the given methods with the given arguments with encrypting but without signing.
     * 
     * @param methods the methods of this request.
     * @param recipient the recipient of this request.
     * @param subject the subject of this request.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     */
    @NonCommitting
    public Request(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject) throws SQLException, IOException, PacketException, ExternalException {
        this(methods, recipient, new SymmetricKey(), subject, null, null, 0);
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
    Request(@Nonnull ReadOnlyList<Method> methods, @Nonnull HostIdentifier recipient, @Nullable SymmetricKey symmetricKey, @Nonnull InternalIdentifier subject, @Nullable RequestAudit audit, @Nullable Object field, int iteration) throws SQLException, IOException, PacketException, ExternalException {
        super(methods, methods.size(), field, recipient, symmetricKey, subject, audit);
        
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        assert methods.doesNotContainNull() : "The list of methods does not contain null.";
        assert Method.areSimilar(methods) : "The methods are similar to each other.";
        
        if (iteration == 5) throw new PacketException(PacketError.EXTERNAL, "The resending of a request was triggered five times.");
        
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
    public Request(@Nonnull InputStream inputStream) throws SQLException, IOException, PacketException, ExternalException {
        super(inputStream, null, true);
        
        final @Nullable HostIdentifier recipient = getEncryption().getRecipient();
        assert recipient != null : "The recipient of the request is not null.";
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
    @Nonnull SignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) throws SQLException, IOException, PacketException, ExternalException {
        return new SignatureWrapper(Packet.SIGNATURE, compression, subject);
    }
    
    
    @Override
    @RawRecipient
    final void initialize(int size) {
        this.methods = new FreezableArrayList<>(size);
        for (int i = 0; i < size; i++) methods.add(null);
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
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @Pure
    public final @Nonnull Method getMethod(int index) {
        return methods.getNotNull(index);
    }
    
    /**
     * Sets the method at the given position during the packet constructor.
     * 
     * @param index the index of the method which is to be set.
     * @param method the method which is to set at the index.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @RawRecipient
    final void setMethod(int index, @Nonnull Method method) {
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
    @Nonnull Response resend(@Nonnull FreezableList<Method> methods, @Nonnull HostIdentifier recipient, @Nonnull InternalIdentifier subject, int iteration, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        return new Request(methods, recipient, new SymmetricKey(), subject, null, null, iteration).send(verified);
    }
    
    /**
     * Sends this request and returns the response with verifying the signature.
     * 
     * @return the response to this request.
     * 
     * @throws PacketException if the recipient responded with a packet error.
     * 
     * @ensure response.getSize() == getSize() : "The response has the same number of elements (otherwise a {@link PacketException} is thrown).";
     */
    @NonCommitting
    public final @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        return send(true);
    }
    
    /**
     * Sends this request and returns the response, optionally verifying the signature.
     * 
     * @param verified determines whether the signature of the response is verified (if not, it needs to be checked by the caller).
     * 
     * @return the response to this request.
     * 
     * @throws PacketException if the recipient responded with a packet error.
     * 
     * @ensure response.getSize() == getSize() : "The response has the same number of elements (otherwise a {@link PacketException} is thrown).";
     */
    @NonCommitting
    public final @Nonnull Response send(boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        try (@Nonnull Socket socket = new Socket("id." + getRecipient().getString(), Server.PORT)) {
            socket.setSoTimeout(1000000); // TODO: Remove two zeroes!
            this.write(socket.getOutputStream());
            return new Response(this, socket.getInputStream(), verified);
        } catch (@Nonnull PacketException exception) {
            if (exception.getError() == PacketError.KEYROTATION && this instanceof ClientRequest) {
                return ((ClientRequest) this).recommit(methods, iteration, verified);
            } else if (exception.getError() == PacketError.RELOCATION && subject instanceof InternalNonHostIdentifier) {
                if (getMethod(0).getType().equals(IdentityQuery.TYPE)) throw new PacketException(PacketError.EXTERNAL, "The response to an identity query may not be a relocation exception.");
                @Nullable InternalNonHostIdentifier address = Successor.get((InternalNonHostIdentifier) subject);
                if (address == null) {
                    address = Successor.getReloaded((InternalNonHostIdentifier) subject);
                    if (!address.getIdentity().equals(subject.getIdentity())) throw new InvalidDeclarationException("The claimed successor " + address + " of " + subject + " does not link back.", subject, null);
                } else if (subject.isMapped()) {
                    Mapper.unmap(subject.getMappedIdentity());
                }
                final @Nonnull HostIdentifier recipient = getMethod(0).getService().equals(CoreService.SERVICE) ? address.getHostIdentifier() : getRecipient();
                return resend(methods, recipient, address, iteration, verified);
            } else if (exception.getError() == PacketError.SERVICE && !getMethod(0).isOnHost()) {
                final @Nonnull HostIdentifier recipient = IdentifierClass.create(Cache.getReloadedAttributeContent(subject.getIdentity(), (Role) getMethod(0).getEntity(), getMethod(0).getService().getType(), false)).toHostIdentifier();
                if (this.recipient.equals(recipient)) throw new PacketException(PacketError.EXTERNAL, "The recipient after a service error is still the same.", exception);
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
            final @Nonnull Time time = new Time();
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
