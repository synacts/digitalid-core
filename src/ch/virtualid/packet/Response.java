package ch.virtualid.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.annotations.RawRecipient;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InactiveSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identity.Identifier;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.xdf.Block;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class decrypts, verifies and decompresses responses on the client-side.
 * 
 * @invariant getSize() == replies.size() && getSize() == exceptions.size() : "The number of elements equals the number of replies and the number of exceptions.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
     * Packs the given packet exception as a response without signing.
     * 
     * @param exception the packet exception that is to be packed as an unsigned response.
     * @param symmetricKey the symmetric key used for encryption or null if the response is not encrypted.
     * 
     * @ensure getSize() == 1 : "The size of this response is one.";
     */
    public Response(@Nonnull PacketException exception, @Nullable SymmetricKey symmetricKey) throws SQLException, IOException, PacketException, ExternalException {
        super(new Pair<FreezableList<Reply>, FreezableList<PacketException>>((FreezableList<Reply>) new FreezableArrayList<Reply>(1).freeze(), (FreezableList<PacketException>) new FreezableArrayList<PacketException>(exception).freeze()), 1, null, symmetricKey, null, null, null, null, null, null, false, null);
    }
    
    /**
     * Packs the given replies and exceptions with the given arguments as a response to the given request.
     * 
     * @param request the corresponding request.
     * @param replies the replies to the methods of the corresponding request.
     * @param exceptions the exceptions to the methods of the corresponding request.
     * @param subject the identifier of the identity about which a statement is made.
     * @param audit the audit since the last audit or null if no audit is appended.
     * 
     * @require replies.isFrozen() : "The list of replies is frozen.";
     * @require replies.isNotEmpty() : "The list of replies is not empty.";
     * @require exceptions.isFrozen() : "The list of exceptions is frozen.";
     * @require replies.size() == exceptions.size() : "The number of replies and exceptions are the same.";
     * 
     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the request.";
     */
    public Response(@Nonnull Request request, @Nonnull FreezableList<Reply> replies, @Nonnull FreezableList<PacketException> exceptions, @Nonnull Identifier subject, @Nullable Audit audit) throws SQLException, IOException, PacketException, ExternalException {
        super(new Pair<FreezableList<Reply>, FreezableList<PacketException>>(replies, exceptions), replies.size(), null, request.getEncryption().getSymmetricKey(), subject, audit, request.getRecipient(), null, null, null, false, null);
        
        assert replies.isFrozen() : "The list of replies is frozen.";
        assert replies.isNotEmpty() : "The list of replies is not empty.";
        assert exceptions.isFrozen() : "The list of exceptions is frozen.";
        assert replies.size() == exceptions.size() : "The number of replies and exceptions are the same.";
    }
    
    
    /**
     * Reads and unpacks the response from the given input stream.
     * 
     * @param request the corresponding request.
     * @param inputStream the input stream to read the response from.
     * @param verified determines whether the signature is verified (if not, it needs to be checked by the caller).
     * 
     * @ensure getSize() == request.getSize() : "The size of this response equals the size of the given request.";
     */
    public Response(@Nonnull Request request, @Nonnull InputStream inputStream, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        super(inputStream, request, verified);
    }
    
    
    @Pure
    @Override
    void checkRecency() throws InactiveSignatureException {
        for (final @Nullable Reply reply : replies) {
            if (reply != null) reply.getSignatureNotNull().checkRecency();
        }
    }
    
    
    @Override
    @RawRecipient
    @SuppressWarnings("unchecked")
    void setLists(@Nonnull Object object) {
        final @Nonnull Pair<FreezableList<Reply>, FreezableList<PacketException>> pair = (Pair<FreezableList<Reply>, FreezableList<PacketException>>) object;
        this.replies = pair.getValue0();
        this.exceptions = pair.getValue1();
    }
    
    @Pure
    @Override
    @RawRecipient
    @Nullable Block getBlock(int index) {
        if (exceptions.isNotNull(index)) return exceptions.getNotNull(index).toBlock();
        if (replies.isNotNull(index)) return replies.getNotNull(index).toBlock();
        return null;
    }
    
    @Override
    @RawRecipient
    void initialize(int size) {
        this.replies = new FreezableArrayList<Reply>(size);
        this.exceptions = new FreezableArrayList<PacketException>(size);
    }
    
    @Override
    @RawRecipient
    void freeze() {
        replies.freeze();
        exceptions.freeze();
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
        if (exceptions.isNotNull(index)) throw exceptions.getNotNull(index);
        else return replies.get(index);
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
