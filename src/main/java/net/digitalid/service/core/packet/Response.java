package net.digitalid.service.core.packet;

import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.action.synchronizer.Audit;
import net.digitalid.service.core.action.synchronizer.ResponseAudit;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.CompressionWrapper;
import net.digitalid.service.core.block.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.utility.annotations.reference.RawRecipient;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.index.ValidIndex;
import net.digitalid.utility.collections.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * This class decrypts, verifies and decompresses responses on the client-side.
 * 
 * @invariant getSize() == replies.size() && getSize() == exceptions.size() : "The number of elements equals the number of replies and the number of exceptions.";
 */
@Immutable
public final class Response extends Packet {
    
    /**
     * Stores the replies of this response.
     */
    private @Nonnull @Frozen @NonEmpty FreezableList<Reply> replies;
    
    /**
     * Stores the exceptions of this response.
     */
    private @Nonnull @Frozen @NonEmpty FreezableList<RequestException> exceptions;
    
    /**
     * Stores the signer of this response.
     */
    private @Nullable HostIdentifier signer;
    
    /**
     * Stores the request that caused this response.
     */
    private final @Nullable Request request;
    
    /**
     * Packs the given packet exception as a response without signing.
     * 
     * @param request the corresponding request or null if not yet decoded.
     * @param exception the packet exception that is to be packed as an unsigned response.
     * 
     * @ensure getSize() == 1 : "The size of this response is one.";
     */
    @NonCommitting
    public Response(@Nullable Request request, @Nonnull RequestException exception) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(new FreezablePair<>(FreezableArrayList.<Reply>getWithCapacity(1).freeze(), FreezableArrayList.get(exception).freeze()).freeze(), 1, null, null, request == null ? null : request.getEncryption().getSymmetricKey(), null, null);
        
        this.request = request;
    }
    
    /**
     * Packs the given replies and exceptions with the given arguments as a response to the given request.
     * 
     * @param request the corresponding request.
     * @param replies the replies to the methods of the corresponding request.
     * @param exceptions the exceptions to the methods of the corresponding request.
     * @param audit the audit since the last audit or null if no audit is appended.
     * 
     * @require replies.size() == exceptions.size() : "The number of replies and exceptions are the same.";
     * 
     * @ensure hasRequest() : "This response has a request.";
     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the request.";
     */
    @NonCommitting
    public Response(@Nonnull Request request, @Nonnull @Frozen @NonEmpty ReadOnlyList<Reply> replies, @Nonnull @Frozen @NonEmpty ReadOnlyList<RequestException> exceptions, @Nullable ResponseAudit audit) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(new FreezablePair<>(replies, exceptions).freeze(), replies.size(), request.getRecipient(), null, request.getEncryption().getSymmetricKey(), request.getSubject(), audit);
        
        assert replies.isFrozen() : "The list of replies is frozen.";
        assert !replies.isEmpty() : "The list of replies is not empty.";
        assert exceptions.isFrozen() : "The list of exceptions is frozen.";
        assert replies.size() == exceptions.size() : "The number of replies and exceptions are the same.";
        
        this.request = request;
    }
    
    
    /**
     * Reads and unpacks the response from the given input stream.
     * 
     * @param request the corresponding request.
     * @param inputStream the input stream to read the response from.
     * @param verified determines whether the signature is verified (if not, it needs to be checked by the caller).
     * 
     * @ensure hasRequest() : "This response has a request.";
     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the given request.";
     */
    @NonCommitting
    public Response(@Nonnull Request request, @Nonnull InputStream inputStream, boolean verified) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(inputStream, request, verified);
        
        this.request = request;
    }
    
    
    @Pure
    @Override
    public @Nullable ResponseAudit getAudit() {
        final @Nullable Audit audit = super.getAudit();
        return audit != null ? (ResponseAudit) audit : null;
    }
    
    /**
     * Returns the audit of this packet.
     * 
     * @return the audit of this packet.
     * 
     * @require getAudit() != null : "The audit is not null.";
     */
    @Pure
    public @Nonnull ResponseAudit getAuditNotNull() {
        final @Nullable ResponseAudit audit = getAudit();
        assert audit != null : "The audit is not null.";
        return audit;
    }
    
    /**
     * Returns whether this response has a request.
     * 
     * @return whether this response has a request.
     */
    public boolean hasRequest() {
        return request != null;
    }
    
    /**
     * Returns the request that caused this response.
     * 
     * @return the request that caused this response.
     * 
     * @require hasRequest() : "This response has a request.";
     */
    @Pure
    public @Nonnull Request getRequest() {
        assert request != null : "This response has a request.";
        
        return request;
    }
    
    
    @Override
    @RawRecipient
    @SuppressWarnings("unchecked")
    void setList(@Nonnull Object object) {
        final @Nonnull ReadOnlyPair<ReadOnlyList<Reply>, ReadOnlyList<RequestException>> pair = (ReadOnlyPair<ReadOnlyList<Reply>, ReadOnlyList<RequestException>>) object;
        this.replies = (FreezableList<Reply>) pair.getElement0();
        this.exceptions = (FreezableList<RequestException>) pair.getElement1();
    }
    
    @Override
    @RawRecipient
    void setField(@Nullable Object field) {
        this.signer = (HostIdentifier) field;
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nullable Block getBlock(int index) {
        if (!exceptions.isNull(index)) { return exceptions.getNonNullable(index).toBlock(); }
        if (!replies.isNull(index)) { return replies.getNonNullable(index).toBlock(); }
        return null;
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nonnull HostSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
        assert signer != null : "This method is only called if the element is not an exception.";
        return HostSignatureWrapper.sign(Packet.SIGNATURE, compression, subject, audit, signer);
    }
    
    
    @Override
    @RawRecipient
    void initialize(int size) {
        this.replies = FreezableArrayList.getWithCapacity(size);
        this.exceptions = FreezableArrayList.getWithCapacity(size);
        for (int i = 0; i < size; i++) {
            replies.add(null);
            exceptions.add(null);
        }
    }
    
    @Override
    @RawRecipient
    void freeze() {
        replies.freeze();
        exceptions.freeze();
    }
    
    
    /**
     * Checks whether the reply at the given position in this response is a packet exception.
     * 
     * @param index the index of the reply which is to be checked.
     * 
     * @throws RequestException if the responding host encountered a packet error.
     */
    @Pure
    public void checkReply(@ValidIndex int index) throws RequestException {
        if (!exceptions.isNull(index)) { throw exceptions.getNonNullable(index); }
    }
    
    /**
     * Returns the reply at the given position in this response or null if there was none.
     * 
     * @param index the index of the reply which is to be returned.
     * 
     * @return the reply at the given position in this response or null if there was none.
     * 
     * @throws RequestException if the responding host encountered a packet error.
     */
    @Pure
    public @Nullable Reply getReply(@ValidIndex int index) throws RequestException {
        checkReply(index);
        return replies.get(index);
    }
    
    /**
     * Returns the reply at the given position in this response.
     * 
     * @param index the index of the reply which is to be returned.
     * 
     * @return the reply at the given position in this response.
     * 
     * @throws RequestException if the responding host encountered a packet error.
     * 
     * @require getReply(index) != null : "The reply at the given position is not null.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    public @Nonnull <T extends Reply> T getReplyNotNull(@ValidIndex int index) throws RequestException {
        final @Nullable Reply reply = getReply(index);
        assert reply != null : "The reply is not null.";
        return (T) reply;
    }
    
    /**
     * Sets the reply at the given position during the packet constructor.
     * 
     * @param index the index of the reply which is to be set.
     * @param reply the reply which is to set at the index.
     */
    @RawRecipient
    void setReply(@ValidIndex int index, @Nonnull Reply reply) {
        replies.set(index, reply);
    }
    
    /**
     * Sets the exception at the given position during the packet constructor.
     * 
     * @param index the index of the exception which is to be set.
     * @param exception the exception which is to set at the index.
     */
    @RawRecipient
    void setException(@ValidIndex int index, @Nonnull RequestException exception) {
        exceptions.set(index, exception);
    }
    
}
