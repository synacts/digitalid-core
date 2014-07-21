package ch.virtualid.handler;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import javax.annotation.Nonnull;

/**
 * Description. -> reply to external action
 * 
 * - Only action replies can be executed on the client (and are added to audits on services). -> introduce another abstract method executeOnService! Probably not necessary, could also be added by the pusher to the audit.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class ActionReply extends Reply {
    
    public ActionReply() {
        
    }
    
    // TODO:
    public abstract @Nonnull AgentPermissions getAuditPermissions();
    
    public abstract @Nonnull Restrictions getAuditRestrictions();
    
}
