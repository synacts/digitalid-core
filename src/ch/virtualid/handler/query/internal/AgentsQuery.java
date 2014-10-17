package ch.virtualid.handler.query.internal;

import ch.virtualid.agent.Agent;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.QueryReply;
import ch.virtualid.handler.reply.query.CoreServiceQueryReply;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AgentsQuery extends CoreServiceInternalQuery {
    
    /**
     * Creates an internal query to retrieve the agents of the given role.
     * 
     * @param role the role to which this handler belongs.
     */
    public AgentsQuery(@Nonnull Role role) {
        super(role);
    }

    @Override
    public Class<? extends CoreServiceQueryReply> getReplyClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected QueryReply executeOnHost(Agent agent) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SemanticType getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Block toBlock() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
