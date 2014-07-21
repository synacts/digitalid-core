package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.handler.Handler;
import ch.virtualid.packet.PacketException;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.SignatureWrapper;
import java.math.BigInteger;
import java.sql.Connection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class AgentRemove {
    
    public AgentRemove() {
        
    }
    
    /**
     * The handler for requests of type {@code request.remove.client@virtualid.ch}.
     */
    private static class RemoveClient extends Handler {

        private RemoveClient() throws Exception { super("request.remove.client@virtualid.ch", "response.remove.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            BigInteger commitmentOfRemover = signature.getClient();
            BigInteger commitmentOfRemovee = new IntegerWrapper(element).getValue();
            
            Restrictions restrictionsOfRemover = host.getRestrictions(connection, vid, commitmentOfRemover);
            Restrictions restrictionsOfRemovee = host.getRestrictions(connection, vid, commitmentOfRemovee);
            if (restrictionsOfRemover == null) throw new PacketException(PacketException.AUTHORIZATION);
            restrictionsOfRemover.checkCoverage(restrictionsOfRemovee);
            
            AgentPermissions authorizationOfRemover = host.getAuthorization(connection, vid, commitmentOfRemover);
            AgentPermissions authorizationOfRemovee = host.getAuthorization(connection, vid, commitmentOfRemovee);
            authorizationOfRemover.checkDoesCover(authorizationOfRemovee);
            
            host.removeClient(connection, vid, commitmentOfRemovee);
            
            return Block.EMPTY;
        }
        
    }
    
}
