package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.handler.Handler;
import ch.virtualid.exceptions.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.math.BigInteger;
import java.sql.Connection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class AgentAuthorize {
    
    public AgentAuthorize() {
        
    }
    
    /**
     * The handler for requests of type {@code request.authorize.client@virtualid.ch}.
     */
    private static class AuthorizeClient extends Handler {

        private AuthorizeClient() throws Exception { super("request.authorize.client@virtualid.ch", "response.authorize.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Block[] elements = new TupleWrapper(element).getElementsNotNull(3);
            
            BigInteger commitmentOfAuthorizer = signature.getClient();
            Restrictions restrictionsOfAuthorizer = host.getRestrictions(connection, vid, commitmentOfAuthorizer);
            AgentPermissions authorizationOfAuthorizer = host.getAuthorization(connection, vid, commitmentOfAuthorizer);
            if (restrictionsOfAuthorizer == null) throw new PacketException(PacketException.AUTHORIZATION);
            
            BigInteger commitmentOfAuthorizee = new IntegerWrapper(elements[0]).getValue();
            Restrictions newRestrictionsOfAuthorizee = new Restrictions(elements[1]);
            AgentPermissions newAuthorizationOfAuthorizee = new AgentPermissions(elements[2]);
            Restrictions restrictionsOfAuthorizee = host.getRestrictions(connection, vid, commitmentOfAuthorizee);
            AgentPermissions authorizationOfAuthorizee = host.getAuthorization(connection, vid, commitmentOfAuthorizee);
            
            restrictionsOfAuthorizer.checkCover(restrictionsOfAuthorizee);
            restrictionsOfAuthorizer.checkCover(newRestrictionsOfAuthorizee);
            
            authorizationOfAuthorizer.checkCover(authorizationOfAuthorizee);
            authorizationOfAuthorizer.checkCover(newAuthorizationOfAuthorizee);
            
            host.authorizeClient(connection, vid, commitmentOfAuthorizee, newRestrictionsOfAuthorizee, newAuthorizationOfAuthorizee);
            
            return Block.EMPTY;
        }
        
    }
    
}
