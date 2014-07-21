package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.handler.Handler;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.Connection;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class ClientAccredit {
    
    public ClientAccredit() {
        
    }
    
    /**
     * The handler for requests of type {@code request.accredit.client@virtualid.ch}.
     */
    private static class AccreditClient extends Handler {

        private AccreditClient() throws Exception { super("request.accredit.client@virtualid.ch", "response.accredit.client@virtualid.ch", true); }
        
        @Override
        public Block handle(Connection connection, Host host, long vid, Block element, SignatureWrapper signature) throws Exception {
            Block[] elements = new TupleWrapper(element).getElementsNotNull(2);
            String name = new StringWrapper(elements[0]).getString();
            if (name.length() > 255) throw new InvalidEncodingException("The name of the new client may be at most 255 bytes.");
            AgentPermissions preference = new AgentPermissions(elements[1]);
            
            host.accreditClient(connection, vid, signature.getClient(), name, preference);
            
            return Block.EMPTY;
        }
        
    }
    
}
