package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Handler;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class ClientAccredit extends CoreServiceInternalAction {
    
    public ClientAccredit() {
        
    }
    
    
    @Override
    public void executeOnHostInternalAction() throws PacketException, SQLException {
        assert isOnHost() : "This method is called on a host.";
        assert hasSignature() : "This handler has a signature.";
        
        // TODO: Check that it's a client signature and that the password is correct.
        
        final @Nonnull Agent agent = getSignatureNotNull().getAgentCheckedAndRestricted(getEntityNotNull());
        
        final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions();
        if (!permissions.equals(AgentPermissions.NONE)) agent.getPermissions().checkCover(permissions);
        
        final @Nonnull Restrictions restrictions = getRequiredRestrictions();
        if (!restrictions.equals(Restrictions.NONE)) agent.getRestrictions().checkCover(restrictions);
        
        final @Nullable Agent other = getRequiredAgent();
        if (other != null) agent.checkCovers(other);
        
        executeOnBoth();
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
