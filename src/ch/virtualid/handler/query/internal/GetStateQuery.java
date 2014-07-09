package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.Permissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.handler.Handler;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.identity.Mapper;
import ch.virtualid.concept.Entity;
import ch.xdf.Block;
import ch.xdf.Int32Wrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class GetStateQuery extends InternalQuery {
    
    /**
     * Creates a new query with the given connection, entity, signature and block.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected GetStateQuery(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(connection, entity, signature, block);
    }
    
    /**
     * The handler for requests of type {@code request.get.contact@virtualid.ch}.
     */
    private static class GetContact extends Handler {

        private GetContact() throws Exception { super("request.get.contact@virtualid.ch", "response.get.contact@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            int context = new Int32Wrapper(element).getValue();
            
            Restrictions restrictions = host.getRestrictions(connection, vid, signature.getClient());
            restrictions.checkCoverage(context);
            
            List<Long> contacts = host.getContacts(connection, vid, context);
            List<Block> elements = new LinkedList<Block>();
            
            for (long contact : contacts) elements.add(new StringWrapper(Mapper.getIdentifier(contact)).toBlock());
            
            return new ListWrapper(elements).toBlock();
        }
        
    }
    
    /**
     * The handler for requests of type {@code request.get.restrictions.client@virtualid.ch}.
     */
    private static class GetRestrictions extends Handler {

        private GetRestrictions() throws Exception { super("request.get.restrictions.client@virtualid.ch", "response.get.restrictions.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Restrictions restrictions = host.getRestrictions(connection, vid, signature.getClient());
            return restrictions == null ? Block.EMPTY: restrictions.toBlock();
        }
        
    }
    
    /**
     * The handler for requests of type {@code request.get.authorization.client@virtualid.ch}.
     */
    private static class GetAuthorization extends Handler {

        private GetAuthorization() throws Exception { super("request.get.authorization.client@virtualid.ch", "response.get.authorization.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Permissions authorization = host.getAuthorization(connection, vid, signature.getClient());
            return authorization.toBlock();
        }
        
    }
    
    /**
     * The handler for requests of type {@code request.list.client@virtualid.ch}.
     */
    private static class GetClient extends Handler {

        private GetClient() throws Exception { super("request.list.client@virtualid.ch", "response.list.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Restrictions restrictions = host.getRestrictions(connection, vid, signature.getClient());
            Permissions authorization = host.getAuthorization(connection, vid, signature.getClient());
            
            return restrictions == null ? Block.EMPTY: host.getClients(connection, vid, restrictions, authorization);
        }
        
    }
    
}
