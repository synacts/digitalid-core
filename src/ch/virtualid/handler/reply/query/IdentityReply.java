package ch.virtualid.handler.reply.query;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.external.IdentityQuery;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Predecessors;
import ch.virtualid.identity.ReadonlyPredecessors;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.Successor;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the identity of the given subject.
 * 
 * @see IdentityQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
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
    public IdentityReply(@Nonnull InternalNonHostIdentifier subject) throws SQLException, PacketException {
        super(subject);
        
        if (!subject.isMapped()) throw new PacketException(PacketError.IDENTIFIER, "The identity with the identifier " + subject + " does not exist on this host.");
        this.category = subject.getMappedIdentity().getCategory();
        if (category.isInternalNonHostIdentity()) throw new SQLException("The category is " + category.name() + " instead of an internal non-host identitiy.");
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
    private IdentityReply(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException {
        super(entity, signature, number);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
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
    public @Nonnull String toString() {
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
    public @Nonnull Category getCategory() {
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
    public @Nonnull ReadonlyPredecessors getPredecessors() {
        return predecessors;
    }
    
    /**
     * Returns the successor of the subject.
     * 
     * @return the successor of the subject.
     */
    @Pure
    public @Nullable InternalNonHostIdentifier getSuccessor() {
        return successor;
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Reply.Factory {
        
        static { Reply.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Reply create(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new IdentityReply(entity, signature, number, block);
        }
        
    }
    
}
