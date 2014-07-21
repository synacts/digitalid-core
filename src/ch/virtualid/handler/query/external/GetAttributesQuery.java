package ch.virtualid.handler.query.external;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.credential.Credential;
import ch.virtualid.expression.Expression;
import ch.virtualid.handler.ExternalQuery;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Mapper;
import ch.virtualid.concept.Entity;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.javatuples.Pair;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class GetAttributesQuery extends ExternalQuery {
    
    /**
     * Creates a new query with the given connection, entity, signature and block.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected GetAttributesQuery(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(connection, entity, signature, block);
    }
    
    /**
     * The handler for requests of type {@code request.get.attribute@virtualid.ch}.
     */
    private static class GetAttribute extends Handler {
        
        private GetAttribute() throws Exception { super("request.get.attribute@virtualid.ch", "response.get.attribute@virtualid.ch", false); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            AgentPermissions authorization = null;
            if (signature.getClient() != null) authorization = host.getAuthorization(connection, vid, signature.getClient());
            
            List<Block> types = new ListWrapper(element).getElements();
            List<Block> declarations = new ArrayList<Block>(types.size());
            
            for (Block type : types) {
                String identifier = new StringWrapper(type).getString();
                if (!Identifier.isValid(identifier)) throw new InvalidEncodingException("The identifier of a requested type is invalid.");
                long semanticType = Mapper.getVid(identifier);
                if (!Category.isSemanticType(semanticType)) throw new InvalidEncodingException("The identifiers of the requested types have to denote semantic types.");
                Pair<Block, String> pair = host.getAttribute(connection, vid, semanticType);
                if (pair == null) {
                    declarations.add(Block.EMPTY);
                } else {
                    Block attribute = pair.getValue0();
                    String visibility = pair.getValue1();
                    
                    if (visibility == null) {
                        declarations.add(new TupleWrapper(new Block[]{attribute, Block.EMPTY}).toBlock());
                    } else {
                        if (authorization != null) {
                            authorization.checkRead(semanticType);
                            declarations.add(new TupleWrapper(new Block[]{attribute, new StringWrapper(visibility).toBlock()}).toBlock());
                        } else {
                            Credential.checkRead(signature.getCredentials(), semanticType);
                            if (Expression.parse(visibility, connection, host, vid).matches(signature.getCredentials())) {
                                declarations.add(new TupleWrapper(new Block[]{attribute, Block.EMPTY}).toBlock());
                            } else {
                                declarations.add(Block.EMPTY);
                            }
                        }
                    }
                }
            }
            
            return new ListWrapper(declarations).toBlock();
        }
        
    }
    
}
