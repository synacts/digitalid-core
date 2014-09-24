package ch.virtualid.handler.reply.query;

import ch.virtualid.entity.Account;
import ch.virtualid.handler.query.external.IdentityQuery;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.sql.Connection;
import javax.annotation.Nonnull;

/**
 * Description.
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
     * Stores the semantic type {@code predecessor.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PREDECESSOR = SemanticType.create("predecessor.identity@virtualid.ch");
    
    /**
     * Stores the semantic type {@code list.predecessor.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PREDECESSORS = SemanticType.create("list.predecessor.identity@virtualid.ch").load(ListWrapper.TYPE, PREDECESSOR);
    
    /**
     * Stores the semantic type {@code response.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("response.identity@virtualid.ch").load(TupleWrapper.TYPE, Category.TYPE, PREDECESSORS, SUCCESSOR);
    
    // Load the recursive declaration of the predecessor type.
    static { PREDECESSOR.load(TupleWrapper.TYPE, NonHostIdentity.IDENTIFIER, IdentityReply.PREDECESSORS); }
    
    
    public IdentityReply(@Nonnull Account account) {
        super(account);
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
    
}
