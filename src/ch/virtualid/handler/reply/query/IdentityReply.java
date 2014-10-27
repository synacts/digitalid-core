package ch.virtualid.handler.reply.query;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.external.IdentityQuery;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Predecessor;
import ch.virtualid.identity.Predecessors;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.TupleWrapper;
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
     * Stores the semantic type {@code response.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("response.identity@virtualid.ch").load(TupleWrapper.TYPE, Category.TYPE, Predecessors.TYPE, SUCCESSOR);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the category of the subject.
     */
    private final @Nonnull Category category;
    
    /**
     * Stores the predecessors of the subject.
     */
    private final @Nonnull Predecessors predecessors;
    
    /**
     * Stores the successor of the subject.
     */
    private final @Nullable NonHostIdentifier successor;
    
    /**
     * Creates a query reply for the identity of given subject.
     * 
     * @param subject the subject of this handler.
     */
    public IdentityReply(@Nonnull NonHostIdentifier subject) throws SQLException, PacketException {
        super(subject);
        
        if (!Mapper.isMapped(subject)) throw new PacketException(PacketError.IDENTIFIER, "The identity with the identifier " + subject + " does not exist on this host.");
        this.category = Mapper.getMappedIdentity(subject).getCategory();
        this.predecessors = new Predecessor(subject).getPredecessors();
        this.successor = Mapper.getSuccessor(subject);
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
        this.predecessors = new Predecessors(tuple.getElementNotNull(1));
        this.successor = tuple.isElementNull(2) ? null : new NonHostIdentifier(tuple.getElementNotNull(2));
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
     */
    @Pure
    public @Nonnull Category getCategory() {
        return category;
    }
    
    /**
     * Returns the predecessors of the subject.
     * 
     * @return the predecessors of the subject.
     */
    @Pure
    public @Nonnull Predecessors getPredecessors() {
        return predecessors;
    }
    
    /**
     * Returns the successor of the subject.
     * 
     * @return the successor of the subject.
     */
    @Pure
    public @Nullable NonHostIdentifier getSuccessor() {
        return successor;
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Reply.Factory {
        
        static { Reply.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Reply create(@Nullable Entity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException, SQLException, IdentityNotFoundException, InvalidDeclarationException {
            return new IdentityReply(entity, signature, number, block);
        }
        
    }
    
}
