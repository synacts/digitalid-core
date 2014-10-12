package ch.virtualid.handler.reply.query;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.external.IdentityQuery;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Predecessor;
import ch.virtualid.identity.Predecessors;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Replies the identity of the given subject.
 * 
 * @see IdentityQuery
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.1
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
     * @param account the account to which this query reply belongs.
     * 
     * @require account.getIdentity() instanceof NonHostIdentity : "The account belongs to a non-host identity.";
     */
    public IdentityReply(@Nonnull Account account, @Nonnull NonHostIdentifier identifier) throws SQLException {
        super(account);
        
        assert account.getIdentity() instanceof NonHostIdentity : "The account belongs to a non-host identity.";
        
        final @Nonnull NonHostIdentity identity = (NonHostIdentity) account.getIdentity();
        this.category = identity.getCategory();
        this.predecessors = new Predecessor(identifier).getPredecessors();
        this.successor = Mapper.getSuccessor(identifier);
    }
    
    
    /**
     * Creates a new reply with the given connection, entity, signature and block.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected IdentityReply(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(connection, entity, signature, block);
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
