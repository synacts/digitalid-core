package ch.virtualid.identity;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.service.CoreServiceQueryReply;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the identity of the given subject.
 * 
 * @see IdentityQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class IdentityReply extends CoreServiceQueryReply {
    
    /**
     * Stores the semantic type {@code successor.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SUCCESSOR = SemanticType.create("successor.identity@virtualid.ch").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code reply.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("reply.identity@virtualid.ch").load(TupleWrapper.TYPE, Category.TYPE, Predecessors.TYPE, SUCCESSOR);
    
    
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
    private final @Nonnull ReadonlyPredecessors predecessors;
    
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
    IdentityReply(@Nonnull InternalNonHostIdentifier subject) throws SQLException, PacketException {
        super(subject);
        
        if (!subject.isMapped()) throw new PacketException(PacketError.IDENTIFIER, "The identity with the identifier " + subject + " does not exist on this host.");
        this.category = subject.getMappedIdentity().getCategory();
        if (category.isInternalNonHostIdentity()) throw new SQLException("The category is " + category.name() + " instead of an internal non-host identitiy.");
        if (!Predecessors.exist(subject)) throw new PacketException(PacketError.IDENTIFIER, "The identity with the identifier " + subject + " is not yet initialized.");
        this.predecessors = Predecessors.get(subject);
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
        this.category = Category.get(tuple.getElementNotNull(0));
        if (category.isInternalNonHostIdentity()) throw new InvalidEncodingException("The category is " + category.name() + " instead of an internal non-host identitiy.");
        this.predecessors = new Predecessors(tuple.getElementNotNull(1)).freeze();
        this.successor = tuple.isElementNull(2) ? null : IdentifierClass.create(tuple.getElementNotNull(2)).toInternalNonHostIdentifier();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
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
    @Nonnull ReadonlyPredecessors getPredecessors() {
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
        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new IdentityReply(entity, signature, number, block);
        }
        
    }
    
}
