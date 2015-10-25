package net.digitalid.service.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.IdentifierClass;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.service.CoreServiceQueryReply;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.HostSignatureWrapper;
import net.digitalid.service.core.wrappers.TupleWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Replies the identity of the given subject.
 * 
 * @see IdentityQuery
 */
@Immutable
public final class IdentityReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code successor.identity@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SUCCESSOR = SemanticType.map("successor.identity@core.digitalid.net").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code reply.identity@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("reply.identity@core.digitalid.net").load(TupleWrapper.TYPE, Category.TYPE, FreezablePredecessors.TYPE, SUCCESSOR);
    
    
    /**
     * Stores the category of the subject.
     * 
     * @invariant category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    private final @Nonnull Category category;
    
    /**
     * Stores the predecessors of the subject.
     * 
     * @invariant predecessors.isFrozen() : "The predecessors are frozen.";
     */
    private final @Nonnull ReadOnlyPredecessors predecessors;
    
    /**
     * Stores the successor of the subject.
     */
    private final @Nullable InternalNonHostIdentifier successor;
    
    /**
     * Creates a query reply for the identity of given subject.
     * 
     * @param subject the subject of this handler.
     */
    @NonCommitting
    IdentityReply(@Nonnull InternalNonHostIdentifier subject) throws AbortException, PacketException {
        super(subject);
        
        if (!subject.isMapped()) throw new PacketException(PacketErrorCode.IDENTIFIER, "The identity with the identifier " + subject + " does not exist on this host.");
        this.category = subject.getMappedIdentity().getCategory();
        if (!category.isInternalNonHostIdentity()) throw new SQLException("The category is " + category.name() + " instead of an internal non-host identity.");
        if (!FreezablePredecessors.exist(subject)) throw new PacketException(PacketErrorCode.IDENTIFIER, "The identity with the identifier " + subject + " is not yet initialized.");
        this.predecessors = FreezablePredecessors.get(subject);
        this.successor = Successor.get(subject);
    }
    
    /**
     * Creates a query reply that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the host signature of this handler.
     * @param number the number that references this reply.
     * @param block the content which is to be decoded.
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
     */
    private IdentityReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, number);
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.category = Category.get(tuple.getNonNullableElement(0));
        if (!category.isInternalNonHostIdentity()) throw new InvalidEncodingException("The category is " + category.name() + " instead of an internal non-host identity.");
        this.predecessors = new FreezablePredecessors(tuple.getNonNullableElement(1)).freeze();
        this.successor = tuple.isElementNull(2) ? null : IdentifierClass.create(tuple.getNonNullableElement(2)).toInternalNonHostIdentifier();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(3);
        elements.set(0, category.toBlock());
        elements.set(1, predecessors.toBlock());
        elements.set(2, Block.toBlock(SUCCESSOR, successor));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replies the identity.";
    }
    
    
    /**
     * Returns the category of the subject.
     * 
     * @return the category of the subject.
     * 
     * @ensure return.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    @Pure
    @Nonnull Category getCategory() {
        return category;
    }
    
    /**
     * Returns the predecessors of the subject.
     * 
     * @return the predecessors of the subject.
     * 
     * @ensure return.isFrozen() : "The predecessors are frozen.";
     */
    @Pure
    @Nonnull ReadOnlyPredecessors getPredecessors() {
        return predecessors;
    }
    
    /**
     * Returns the successor of the subject.
     * 
     * @return the successor of the subject.
     */
    @Pure
    @Nullable InternalNonHostIdentifier getSuccessor() {
        return successor;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof IdentityReply) {
            final @Nonnull IdentityReply other = (IdentityReply) object;
            return this.category == other.category && this.predecessors.equals(other.predecessors) && Objects.equals(this.successor, other.successor);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + category.hashCode();
        hash = 89 * hash + predecessors.hashCode();
        hash = 89 * hash + Objects.hashCode(successor);
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Reply.Factory {
        
        static { Reply.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
            return new IdentityReply(entity, signature, number, block);
        }
        
    }
    
}
