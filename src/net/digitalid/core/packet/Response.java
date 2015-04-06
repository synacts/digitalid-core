package net.digitalid.core.packet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.RawRecipient;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.synchronizer.Audit;
import net.digitalid.core.synchronizer.ResponseAudit;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadonlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.CompressionWrapper;
import net.digitalid.core.wrappers.HostSignatureWrapper;

/**
 * This class decrypts, verifies and decompresses responses on the client-side.
 * 
 * @invariant getSize() == replies.size() && getSize() == exceptions.size() : "The number of elements equals the number of replies and the number of exceptions.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Response extends Packet {
    
    /**
     * Stores the replies of this response.
     * 
     * @invariant replies.isFrozen() : "The replies are frozen.";
     * @invariant replies.isNotEmpty() : "The replies are not empty.";
     */
    private @Nonnull FreezableList<Reply> replies;
    
    /**
     * Stores the exceptions of this response.
     * 
     * @invariant exceptions.isFrozen() : "The exceptions are frozen.";
     * @invariant exceptions.isNotEmpty() : "The exceptions are not empty.";
     */
    private @Nonnull FreezableList<PacketException> exceptions;
    
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
    public Response(@Nullable Request request, @Nonnull PacketException exception) throws SQLException, IOException, PacketException, ExternalException {
        super(new FreezablePair<>(new FreezableArrayList<Reply>(1).freeze(), new FreezableArrayList<>(exception).freeze()).freeze(), 1, null, null, request == null ? null : request.getEncryption().getSymmetricKey(), null, null);
        
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
     * @require replies.isFrozen() : "The list of replies is frozen.";
     * @require replies.isNotEmpty() : "The list of replies is not empty.";
     * @require exceptions.isFrozen() : "The list of exceptions is frozen.";
     * @require replies.size() == exceptions.size() : "The number of replies and exceptions are the same.";
     * 
     * @ensure hasRequest() : "This response has a request.";
     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the request.";
     */
    @NonCommitting
    public Response(@Nonnull Request request, @Nonnull ReadonlyList<Reply> replies, @Nonnull ReadonlyList<PacketException> exceptions, @Nullable ResponseAudit audit) throws SQLException, IOException, PacketException, ExternalException {
        super(new FreezablePair<>(replies, exceptions).freeze(), replies.size(), request.getRecipient(), null, request.getEncryption().getSymmetricKey(), request.getSubject(), audit);
        
        assert replies.isFrozen() : "The list of replies is frozen.";
        assert replies.isNotEmpty() : "The list of replies is not empty.";
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
    public Response(@Nonnull Request request, @Nonnull InputStream inputStream, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
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
        final @Nonnull ReadonlyPair<ReadonlyList<Reply>, ReadonlyList<PacketException>> pair = (ReadonlyPair<ReadonlyList<Reply>, ReadonlyList<PacketException>>) object;
        this.replies = (FreezableList<Reply>) pair.getElement0();
        this.exceptions = (FreezableList<PacketException>) pair.getElement1();
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
        if (exceptions.isNotNull(index)) return exceptions.getNotNull(index).toBlock();
        if (replies.isNotNull(index)) return replies.getNotNull(index).toBlock();
        return null;
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nonnull HostSignatureWrapper getSignature(@Nullable CompressionWrapper compression, @Nonnull InternalIdentifier subject, @Nullable Audit audit) {
        assert signer != null : "This method is only called if the element is not an exception.";
        return new HostSignatureWrapper(Packet.SIGNATURE, compression, subject, audit, signer);
    }
    
    
    @Override
    @RawRecipient
    void initialize(int size) {
        this.replies = new FreezableArrayList<>(size);
        this.exceptions = new FreezableArrayList<>(size);
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
     * @throws PacketException if the responding host encountered a packet error.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @Pure
    public void checkReply(int index) throws PacketException {
        if (exceptions.isNotNull(index)) throw exceptions.getNotNull(index);
    }
    
    /**
     * Returns the reply at the given position in this response or null if there was none.
     * 
     * @param index the index of the reply which is to be returned.
     * 
     * @return the reply at the given position in this response or null if there was none.
     * 
     * @throws PacketException if the responding host encountered a packet error.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @Pure
    public @Nullable Reply getReply(int index) throws PacketException {
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
     * @throws PacketException if the responding host encountered a packet error.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     * @require getReply(index) != null : "The reply at the given position is not null.";
     */
    @Pure
    @SuppressWarnings("unchecked")
    public @Nonnull <T extends Reply> T getReplyNotNull(int index) throws PacketException {
        final @Nullable Reply reply = getReply(index);
        assert reply != null : "The reply is not null.";
        return (T) reply;
    }
    
    /**
     * Sets the reply at the given position during the packet constructor.
     * 
     * @param index the index of the reply which is to be set.
     * @param reply the reply which is to set at the index.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @RawRecipient
    void setReply(int index, @Nonnull Reply reply) {
        replies.set(index, reply);
    }
    
    /**
     * Sets the exception at the given position during the packet constructor.
     * 
     * @param index the index of the exception which is to be set.
     * @param exception the exception which is to set at the index.
     * 
     * @require index >= 0 && index < getSize() : "The index is valid.";
     */
    @RawRecipient
    void setException(int index, @Nonnull PacketException exception) {
        exceptions.set(index, exception);
    }
    
}
