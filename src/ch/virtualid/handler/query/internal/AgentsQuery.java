package ch.virtualid.handler.query.internal;

import ch.virtualid.entity.Role;
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
    
}
