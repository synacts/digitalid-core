package ch.virtualid.handler.query.external;

import ch.virtualid.handler.ExternalQuery;
import ch.virtualid.handler.Handler;
import ch.virtualid.handler.reply.query.GetIdentityReply;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.DataWrapper;
import ch.xdf.Int8Wrapper;
import ch.xdf.SignatureWrapper;
import java.sql.Connection;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @see GetIdentityReply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class GetIdentityQuery extends ExternalQuery {
    
    /**
     * Stores the semantic type {@code request.identity@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("request.identity@virtualid.ch").load(DataWrapper.TYPE);
    
    
    /**
     * Creates a new query with the given connection, entity, signature and block.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected GetIdentityQuery(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(connection, entity, signature, block);
    }
    
    /**
     * The handler for requests of type {@code request.get.category@virtualid.ch}.
     */
    private static class GetCategory extends Handler {
        
        private GetCategory() throws Exception { super("request.get.category@virtualid.ch", "response.get.category@virtualid.ch", false); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            return new Int8Wrapper(Mapper.getCategory(vid)).toBlock();
        }
        
    }
   
}
