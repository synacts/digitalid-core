package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Permissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.handler.Handler;
import ch.virtualid.packet.PacketException;
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
            Permissions authorizationOfAuthorizer = host.getAuthorization(connection, vid, commitmentOfAuthorizer);
            if (restrictionsOfAuthorizer == null) throw new PacketException(PacketException.AUTHORIZATION);
            
            BigInteger commitmentOfAuthorizee = new IntegerWrapper(elements[0]).getValue();
            Restrictions newRestrictionsOfAuthorizee = new Restrictions(elements[1]);
            Permissions newAuthorizationOfAuthorizee = new Permissions(elements[2]);
            Restrictions restrictionsOfAuthorizee = host.getRestrictions(connection, vid, commitmentOfAuthorizee);
            Permissions authorizationOfAuthorizee = host.getAuthorization(connection, vid, commitmentOfAuthorizee);
            
            restrictionsOfAuthorizer.checkCoverage(restrictionsOfAuthorizee);
            restrictionsOfAuthorizer.checkCoverage(newRestrictionsOfAuthorizee);
            
            authorizationOfAuthorizer.checkDoesCover(authorizationOfAuthorizee);
            authorizationOfAuthorizer.checkDoesCover(newAuthorizationOfAuthorizee);
            
            host.authorizeClient(connection, vid, commitmentOfAuthorizee, newRestrictionsOfAuthorizee, newAuthorizationOfAuthorizee);
            
            return Block.EMPTY;
        }
        
    }
    
}
